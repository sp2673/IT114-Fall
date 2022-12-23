package Project.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Project.common.BGPayload;
import Project.common.ClientPayload;
import Project.common.Constants;
import Project.common.MyLogger;
import Project.common.Payload;
import Project.common.PayloadType;
import Project.common.Phase;
import Project.common.PhasePayload;
import Project.common.RoomResultPayload;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private String clientName;
    private String formattedName;
    private boolean isRunning = false;
    private ObjectOutputStream out;// exposed here for send()
    // private Server server;// ref to our server so we can call methods on it
    // more easily
    private Room currentRoom;
    private static MyLogger logger = MyLogger.getLogger(ServerThread.class.getName());
    private long myId;

    public void setClientId(long id) {
        myId = id;
    }

    public long getClientId() {
        return myId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void info(String message) {
        System.out.println(String.format("Thread[%s]: %s", getId(), message));
    }

    public ServerThread(Socket myClient, Room room) {
        info("Thread created");
        // get communication channels to single client
        this.client = myClient;
        this.currentRoom = room;

    }

    public void setFormattedName(String name) {
        formattedName = name;
    }

    public String getFormattedName() {
        return formattedName;
    }

    protected void setClientName(String name) {
        if (name == null || name.isBlank()) {
            System.err.println("Invalid client name being set");
            return;
        }
        clientName = name;
    }

    public String getClientName() {
        return clientName;
    }

    protected synchronized Room getCurrentRoom() {
        return currentRoom;
    }

    protected synchronized void setCurrentRoom(Room room) {
        if (room != null) {
            currentRoom = room;
        } else {
            info("Passed in room was null, this shouldn't happen");
        }
    }

    public void disconnect() {
        sendConnectionStatus(myId, getClientName(), null, false);
        info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    // send methods
    public boolean sendCurrentPhase(Phase phase){
        PhasePayload p = new PhasePayload();
        p.setPhase(phase);
        return send(p);
    }
    public boolean sendRestartNotice() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESTART);
        return send(p);
    }

    public boolean sendWinner(long clientId) {
        Payload p = new Payload();
        p.setClientId(clientId);
        p.setPayloadType(PayloadType.GAME_OVER);
        return send(p);
    }

    public boolean sendTurn(long clientId, long maxGuess) {
        BGPayload p = new BGPayload();
        p.setPayloadType(PayloadType.TURN);
        p.setClientId(clientId == Constants.DEFAULT_CLIENT_ID ? myId : clientId);
        p.setGuess(maxGuess);
        return send(p);
    }

    public boolean sendCurrentMatter(long clientId, long matter) {
        BGPayload p = new BGPayload();
        p.setPayloadType(PayloadType.MATTER);
        p.setBet(matter);
        p.setClientId(clientId == Constants.DEFAULT_CLIENT_ID ? myId : clientId);
        return send(p);
    }

    public boolean sendReadyStatus(long clientId) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        p.setClientId(clientId);
        return send(p);
    }

    public boolean sendRoomName(String name) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(name);
        return send(p);
    }

    public boolean sendRoomsList(String[] rooms, String message) {
        RoomResultPayload payload = new RoomResultPayload();
        payload.setRooms(rooms);
        // Fixed in Module7.Part9
        if (message != null) {
            payload.setMessage(message);
        }
        return send(payload);
    }

    public boolean sendExistingClient(long clientId, String clientName, String formattedName) {
        ClientPayload p = new ClientPayload();
        p.setPayloadType(PayloadType.SYNC_CLIENT);
        p.setClientId(clientId);
        p.setFormattedName(formattedName);
        p.setClientName(clientName);
        return send(p);
    }

    public boolean sendResetUserList() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_USER_LIST);
        return send(p);
    }

    public boolean sendClientId(long id) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CLIENT_ID);
        p.setClientId(id);
        return send(p);
    }

    public boolean sendMessage(long clientId, String message) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setClientId(clientId);
        p.setMessage(message);
        return send(p);
    }

    public boolean sendConnectionStatus(long clientId, String who, String formattedName, boolean isConnected) {
        ClientPayload p = new ClientPayload();
        p.setPayloadType(isConnected ? PayloadType.CONNECT : PayloadType.DISCONNECT);
        p.setClientId(clientId);
        p.setFormattedName(formattedName);
        p.setClientName(who);
        p.setMessage(isConnected ? "connected" : "disconnected");
        return send(p);
    }

    private boolean send(Payload payload) {
        // added a boolean so we can see if the send was successful
        try {
            // TODO add logger
            logger.fine("Outgoing payload: " + payload);
            out.writeObject(payload);
            logger.info("Sent payload: " + payload);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        } catch (NullPointerException ne) {
            info("Message was attempted to be sent before outbound stream was opened: " + payload);
            return true;// true since it's likely pending being opened
        }
    }

    // end send methods
    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            Payload fromClient;
            while (isRunning && // flag to let us easily control the loop
                    (fromClient = (Payload) in.readObject()) != null // reads an object from inputStream (null would
                                                                     // likely mean a disconnect)
            ) {

                info("Received from client: " + fromClient);
                processPayload(fromClient);

            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            e.printStackTrace();
            info("Client disconnected");
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
                setClientName(p.getClientName());
                break;
            case DISCONNECT:
                Room.disconnectClient(this, getCurrentRoom());
                break;
            case MESSAGE:
                if (currentRoom != null) {
                    currentRoom.sendMessage(this, p.getMessage());
                } else {
                    // TODO migrate to lobby
                    logger.info("Migrating to lobby on message with null room");
                    Room.joinRoom(Constants.LOBBY, this);
                }
                break;
            case GET_ROOMS:
                Room.getRooms(p.getMessage().trim(), this);
                break;
            case CREATE_ROOM:
                Room.createRoom(p.getMessage().trim(), this);
                break;
            case JOIN_ROOM:
                Room.joinRoom(p.getMessage().trim(), this);
                break;
            case READY:
                ((GameRoom) currentRoom).setReady(myId);
                break;
            case MATTER:// guess and bet
                ((GameRoom) currentRoom).setAnteAndGuess(myId, ((BGPayload) p).getBet(), ((BGPayload) p).getGuess());
                break;
            case RESTART:
                ((GameRoom) currentRoom).restartSession();
                break;
            default:
                break;

        }

    }

    private void cleanup() {
        info("Thread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
    }
}