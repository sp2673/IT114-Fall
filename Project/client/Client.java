package Project.client;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import Project.common.BGPayload;
import Project.common.MyLogger;
import Project.common.Payload;
import Project.common.PayloadType;
import Project.common.RoomResultPayload;

//Enum Singleton: https://www.geeksforgeeks.org/advantages-and-disadvantages-of-using-enum-as-singleton-in-java/
public enum Client {
    INSTANCE;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    boolean isRunning = false;
    private Thread fromServerThread;
    private String clientName = "";
    private static MyLogger logger = MyLogger.getLogger(Client.class.getName());
    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine
        // if the server had a problem
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        // TODO validate
        this.clientName = username;
        addCallback(callback);
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerMessage();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    // Send methods TODO add other utility methods for sending here
    // NOTE: Can change this to protected or public if you plan to separate the
    // sendConnect action and the socket handshake
    public void sendRestartRequest() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESTART);
        send(p);
    }

    public void sendBetAndGuess(long bet, long guess) throws IOException, NullPointerException {
        BGPayload p = new BGPayload();
        p.setPayloadType(PayloadType.MATTER);
        p.setBet(bet);
        p.setGuess(guess);
        send(p);

    }

    public void sendReady() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        send(p);
    }

    public void sendCreateRoom(String room) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(room);
        send(p);
    }

    public void sendJoinRoom(String room) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(room);
        send(p);
    }

    public void sendGetRooms(String query) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GET_ROOMS);
        p.setMessage(query);
        send(p);
    }

    private void sendConnect() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        send(p);
    }

    public void sendDisconnect() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.DISCONNECT);
        send(p);
    }

    public void sendMessage(String message) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        send(p);
    }

    // keep this private as utility methods should be the only Payload creators
    private void send(Payload p) throws IOException, NullPointerException {
        logger.fine("Sending Payload: " + p);
        out.writeObject(p);// TODO force throw each
        logger.info("Sent Payload: " + p);
    }

    // end send methods

    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    logger.info("Listening for server messages");
                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        System.out.println("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    System.out.println("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        System.out.println("Server closed connection");
                    } else {
                        System.out.println("Connection closed");
                    }
                } finally {
                    close();
                    System.out.println("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void processPayload(Payload p) {
        logger.fine("Received Payload: " + p);
        if (events == null && events.size() == 0) {
            logger.fine("Events not initialize/set" + p);
            return;
        }
        // TODO handle NPE
        switch (p.getPayloadType()) {
            case CONNECT:
                events.forEach(e -> e.onClientConnect(p.getClientId(), p.getClientName(), p.getMessage()));
                break;
            case DISCONNECT:
                events.forEach(e -> e.onClientDisconnect(p.getClientId(), p.getClientName(), p.getMessage()));
                break;
            case MESSAGE:
                events.forEach(e -> e.onMessageReceive(p.getClientId(), p.getMessage()));
                break;
            case CLIENT_ID:
                events.forEach(e -> e.onReceiveClientId(p.getClientId()));
                break;
            case RESET_USER_LIST:
                events.forEach(e -> e.onResetUserList());
                break;
            case SYNC_CLIENT:
                events.forEach(e -> e.onSyncClient(p.getClientId(), p.getClientName()));
                break;
            case GET_ROOMS:
                events.forEach(e -> e.onReceiveRoomList(((RoomResultPayload) p).getRooms(), p.getMessage()));
                break;
            case JOIN_ROOM:
                events.forEach(e -> e.onRoomJoin(p.getMessage()));
                break;
            case READY:
                events.forEach(e -> e.onReceiveReady(p.getClientId()));
                break;
            case MATTER:
                events.forEach(e -> e.onReceiveMatterUpdate(p.getClientId(), ((BGPayload) p).getBet()));
                break;
            case TURN:
                events.forEach(e -> e.onReceiveTurn(p.getClientId(), ((BGPayload) p).getGuess()));
                break;
            case GAME_OVER:
                events.forEach(e -> e.onReceiveWinner(p.getClientId()));
                break;
            case RESTART:
                events.forEach(e -> e.onReceiveRestart());
                break;
            default:
                logger.warning("Unhandled payload type");
                break;

        }
    }

    private void close() {
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
            System.out.println("Error interrupting listener");
            e.printStackTrace();
        }
        try {
            System.out.println("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing connection");
            server.close();
            System.out.println("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        }
    }
}