package Project.server;

import java.util.*;

import java.util.Map.Entry;

import Project.common.Constants;
import Project.common.GeneralUtils;
import Project.common.MyLogger;

public class Room implements AutoCloseable {
   private String name;
   private List<ServerThread> clients = Collections.synchronizedList(new ArrayList<ServerThread>());
   private boolean isRunning = false;
   // Commands
   private final static String COMMAND_TRIGGER = "/";
   private final static String CREATE_ROOM = "createroom";
   private final static String JOIN_ROOM = "joinroom";
   private final static String DISCONNECT = "disconnect";
   private final static String LOGOUT = "logout";
   private final static String LOGOFF = "logoff";
   private final static String FLIP = "flip";
   private final static String ROLL = "roll";
   private final static String MUTE = "mute";
   private final static String UNMUTE = "unmute";
   private static MyLogger logger = MyLogger.getLogger(Room.class.getName());
   private HashMap<String, String> converter = null;
   private HashMap<String, ArrayList<String>> muted = new HashMap<>();

   public Room(String name) {
      this.name = name;
      isRunning = true;
   }

   private void info(String message) {
      logger.info(String.format("Room[%s]: %s", name, message));
   }

   public String getName() {
      return name;
   }

   public boolean isRunning() {
      return isRunning;
   }

   protected synchronized void addClient(ServerThread client) {
      if (!isRunning) {
         return;
      }
      client.setCurrentRoom(this);
      if (clients.contains(client)) {
         info("Attempting to add a client that already exists");
      } else {
         client.setFormattedName(String.format("<font color=\"%s\">%s</font>", GeneralUtils.getRandomHexColor(),
               client.getClientName()));
         clients.add(client);
         sendConnectionStatus(client, true);
         sendRoomJoined(client);
         sendUserListToClient(client);
      }
   }

   protected synchronized void removeClient(ServerThread client) {
      if (!isRunning) {
         return;
      }
      clients.remove(client);
      // we don't need to broadcast it to the server
      // only to our own Room
      if (clients.size() > 0) {
         // sendMessage(client, "left the room");
         sendConnectionStatus(client, false);
      }
      checkClients();
   }

   /***
    * Checks the number of clients.
    * If zero, begins the cleanup process to dispose of the room
    */
   protected void checkClients() {
      // Cleanup if room is empty and not lobby
      if (!name.equalsIgnoreCase(Constants.LOBBY) && clients.size() == 0) {
         close();
      }
   }

   /***
    * Helper function to process messages to trigger different functionality.
    * 
    * @param message The original message being sent
    * @param client  The sender of the message (since they'll be the ones
    *                triggering the actions)
    */
   private boolean processCommands(String message, ServerThread client) {
      boolean wasCommand = false;
      try {
         if (message.startsWith(COMMAND_TRIGGER)) {
            String[] comm = message.split(COMMAND_TRIGGER);
            String part1 = comm[1];
            String[] comm2 = part1.split(" ");
            String command = comm2[0];
            String roomName;
            wasCommand = true;
            switch (command) {
               case CREATE_ROOM:
                  roomName = comm2[1];
                  Room.createRoom(roomName, client);
                  break;
               case JOIN_ROOM:
                  roomName = comm2[1];
                  Room.joinRoom(roomName, client);
                  break;
               case DISCONNECT:
               case LOGOUT:
               case LOGOFF:
                  Room.disconnectClient(client, this);
                  break;
               // Coin Flip sp2673 
                case FLIP:
                    Random f = new Random();
                    int num = f.nextInt(2);
                    if (num == 0) {
                        sendMessage(client, "flipped heads");
                    } else {
                        sendMessage(client, "Flipped tails");
                    }
                    break;
                // Roll a dice sp2673 
                case ROLL:
                    Random a = new Random();
                    logger.info("Check here");
                    int sum = a.nextInt(8) + 1;
                    sendMessage(client, "Rolled a " + sum);

                  break;
               case MUTE: //sp2673
                  String sender = client.getClientName();
                  String victim = message.substring(message.indexOf(' ') + 1);
                  String printMe = String.format("Print s: %s v:%s", sender,victim);
                  logger.info(printMe);
                  if (muted.containsKey(sender)){
                     ArrayList<String> n = muted.get(sender);
                     if (!n.contains(victim)) {
                        n.add(victim);
                        muted.replace(sender,n);
                     }
                  }
                  else {
                     ArrayList<String> n = new ArrayList<>();
                     n.add(victim);
                     muted.put(sender,n);

                  }
                  break;
               case UNMUTE:
                  String s = client.getClientName();
                  String v = message.substring(message.indexOf(' ') + 1);
                  if (muted.containsKey(s)){
                     ArrayList<String> n = muted.get(s);
                     if (n.contains(v)) {
                        n.remove(v);
                        muted.replace(s, n);
                     }
                  }
                  break;
               default:
                  wasCommand = false;
                  break;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return wasCommand;
   }

   // Command helper methods

   protected static void getRooms(String query, ServerThread client) {
      String[] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
      client.sendRoomsList(rooms,
            (rooms != null && rooms.length == 0) ? "No rooms found containing your query string" : null);
   }

   protected static void createRoom(String roomName, ServerThread client) {
      if (Server.INSTANCE.createNewRoom(roomName)) {
         Room.joinRoom(roomName, client);
      } else {
         client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
         client.sendRoomsList(null, String.format("Room %s already exists", roomName));
      }
   }

   protected static void joinRoom(String roomName, ServerThread client) {
      if (!Server.INSTANCE.joinRoom(roomName, client)) {
         client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
         client.sendRoomsList(null, String.format("Room %s doesn't exist", roomName));
      }
   }

   protected static void disconnectClient(ServerThread client, Room room) {
      client.setCurrentRoom(null);
      client.disconnect();
      room.removeClient(client);
   }
   // end command helper methods

   /***
    * Takes a sender and a message and broadcasts the message to all clients in
    * this room. Client is mostly passed for command purposes but we can also use
    * it to extract other client info.
    * 
    * @param sender  The client sending the message
    * @param message The message to broadcast inside the room
    */
   protected synchronized void sendMessage(ServerThread sender, String message) {

    
      if (!isRunning) {
         return;
      }
      info("Sending message to " + clients.size() + " clients");
      if (sender != null && processCommands(message, sender)) {
         // it was a command, don't broadcast
         return;
      }
      //sp2673 10/22/22
      message = formatMessage(message);
      long from = (sender == null) ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
      if (message.startsWith("@")){
         int whitespaceIndex = message.indexOf(' ');
         String whisperUser = message.substring(1,whitespaceIndex);
         if (muted.containsKey(whisperUser) && sender != null && muted.get(whisperUser).contains(sender.getClientName())){
            return;
         }
         for (int i = 0; i < clients.size(); i++) {
            if (whisperUser.equals(clients.get(i).getClientName())){
               ServerThread client = clients.get(i);
               boolean messageSent = client.sendMessage(from, message);
               if (!messageSent) {
                  handleDisconnect(clients.iterator(), client);
               }
            }
         }
      }
      synchronized (clients) {
         Iterator<ServerThread> iter = clients.iterator();
         while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.sendMessage(from, message);
            if (!messageSent) {
               handleDisconnect(iter, client);
            }
         }
      }
   }

   protected String formatMessage(String message) {
      String alteredMessage = message;

      // expect pairs ** -- __
      if (converter == null) {
         converter = new HashMap<String, String>();
         // user symbol => output text separated by |
         converter.put("\\*{2}", "<b>|</b>");
         converter.put("--", "<i>|</i>");
         converter.put("__", "<u>|</u>");
         converter.put("#r#", "<font color=\"red\">|</font>");
         converter.put("#g#", "<font color=\"green\">|</font>");
         converter.put("#b#", "<font color=\"blue\">|</font>");
      }
      for (Entry<String, String> kvp : converter.entrySet()) {
         if (GeneralUtils.countOccurencesInString(alteredMessage, kvp.getKey().toLowerCase()) >= 2) {
            String[] s1 = alteredMessage.split(kvp.getKey().toLowerCase());
            String m = "";
            for (int i = 0; i < s1.length; i++) {
               if (i % 2 == 0) {
                  m += s1[i];
               } else {
                  String[] wrapper = kvp.getValue().split("\\|");
                  m += String.format("%s%s%s", wrapper[0], s1[i], wrapper[1]);
               }
            }
            alteredMessage = m;
         }
      }

      return alteredMessage;
   }

   protected synchronized void sendUserListToClient(ServerThread receiver) {
      info(String.format("Room[%s] Syncing client list of %s to %s", getName(), clients.size(),
            receiver.getClientName()));
      synchronized (clients) {
         Iterator<ServerThread> iter = clients.iterator();
         while (iter.hasNext()) {
            ServerThread clientInRoom = iter.next();
            if (clientInRoom.getClientId() != receiver.getClientId()) {
               boolean messageSent = receiver.sendExistingClient(clientInRoom.getClientId(),
                     clientInRoom.getClientName(),
                     clientInRoom.getFormattedName());
               // receiver somehow disconnected mid iteration
               if (!messageSent) {
                  handleDisconnect(null, receiver);
                  break;
               }
            }
         }
      }
   }

   protected synchronized void sendRoomJoined(ServerThread receiver) {
      boolean messageSent = receiver.sendRoomName(getName());
      if (!messageSent) {
         handleDisconnect(null, receiver);
      }
   }

   protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
      // converted to a backwards loop to help avoid concurrent list modification
      // due to the recursive sendConnectionStatus()
      // this should only be needed in this particular method due to the recusion
      if (clients == null) {
         return;
      }
      synchronized (clients) {
         for (int i = clients.size() - 1; i >= 0; i--) {
            ServerThread client = clients.get(i);
            boolean messageSent = client.sendConnectionStatus(sender.getClientId(), sender.getClientName(),
                  sender.getFormattedName(),
                  isConnected);
            if (!messageSent) {
               clients.remove(i);
               info("Removed client " + client.getClientName());
               checkClients();
               sendConnectionStatus(client, false);
            }
         }
      }
   }

   protected synchronized void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
      if (iter != null) {
         iter.remove();
      }
      info("Removed client " + client.getClientName());
      checkClients();
      sendConnectionStatus(client, false);
      // sendMessage(null, client.getClientName() + " disconnected");
   }

   public void close() {
      logger.info(getName() + " closing");
      Server.INSTANCE.removeRoom(this);
      isRunning = false;
      clients = null;
   }
}
