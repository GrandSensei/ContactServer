import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class ContactServer {
    private static final int PORT = AppConfig.SERVER_PORT;
    private final ContactManager contactManager;
    private final ExecutorService executorService;
    private final SSLServerSocket sslServerSocket;

    //Keep track of all the clients using the clientHandler
    private final List<ClientHandler> clientHandlerList= new CopyOnWriteArrayList<>();
    public static volatile boolean running = true;

    public ContactServer() throws Exception {
        this.contactManager = new ContactManager();
        this.executorService = Executors.newCachedThreadPool();
        SSLContext sslContext = createSSLContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        sslServerSocket = (SSLServerSocket) ssf.createServerSocket(PORT);

    }

    public static void main(String[] args) throws Exception {
        ContactServer server = new ContactServer();
        server.start();
    }

    private SSLContext createSSLContext() throws Exception {
        // Load the server's keystore containing the private key and certificate.
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream keyStoreIS = new FileInputStream("serverkeystore.jks")) {
            keyStore.load(keyStoreIS, "changeit".toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "changeit".toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }


    public void start() {

        try {
            System.out.println("TLS Server started on port " + PORT);
            while (running) {
                SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, contactManager, this);
                clientHandlerList.add(clientHandler);
                executorService.execute(clientHandler);
        }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }


    //Broadcasting every time the list is updated by someone
    public void broadcastContactsList(){
        for(ClientHandler clientHandler : clientHandlerList){
            clientHandler.sendContactsList();
        }
    }


    //Remove the client handler from the list when they disconnect
    public void removeClientHandler(ClientHandler clientHandler){
        clientHandlerList.remove(clientHandler);
    }


    public  void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (sslServerSocket != null && !sslServerSocket.isClosed()) {
                sslServerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }



    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ContactManager contactManager;
        private final PrintWriter out;
        private final BufferedReader in;
        private final ContactServer server;
        ScheduledExecutorService timeoutScheduler = Executors.newScheduledThreadPool(1);

        //Something better should be there here na?
        private Contact.ContactType allowedType;



        //Session timeout counter
        private long sessionTimer= System.currentTimeMillis();

        public ClientHandler(SSLSocket socket, ContactManager contactManager, ContactServer server) throws IOException {
            this.clientSocket = socket;
            this.contactManager = contactManager;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = server;
        }

        //This needs something.......
        private boolean validateUser(String username, String password) {
            return username != null && !username.isEmpty() && password != null && !password.isEmpty();
        }

        // For demonstration, assign allowed type based on username.
        private Contact.ContactType getAllowedTypeForUser(String username) {
            //Make a function that will go to my User database and get me the required Type.
            //Ideally make a whole new class
            return switch (username.toLowerCase()) {
                case "sales" -> Contact.ContactType.SALES;
                case "tos" -> Contact.ContactType.TOS;
                case "disc" -> Contact.ContactType.DISC;
                case "supplier" -> Contact.ContactType.SUPPLIER;
                default -> Contact.ContactType.CUSTOMER;
            };
        }


        //The part which has the thread and is responsible for session time and accepting/sending messages
        @Override
        public void run() {
            try {
                //Authentication phase
                String inputLine= in.readLine();
                if (inputLine == null) {
                    cleanup();
                    return;
                }
                Message firstMessage= Message.fromString(inputLine);

                if (firstMessage.type() == Message.Type.LOGIN_REQUEST) {
                    String[] credentials = firstMessage.content().split(":");
                    if (credentials.length != 2) {
                       sendError("Invalid credentials");
                        cleanup();
                        return;
                    }
                    //ok I store my credentials here
                    String username = credentials[0];
                    String password = credentials[1];
                    //This part should involve my database? prolly
                    if (validateUser(username, password)) {
                      allowedType = getAllowedTypeForUser(username);
                      sendResponse("logged in");
                    }else {
                        sendError("Invalid username or password");
                        cleanup();
                        return;
                    }
                }else {
                    sendError("Invalid request");
                    cleanup();
                    return;
                }

                /*
                //To Verify the Client's access permissions
                String firstInput = in.readLine();
                if (firstInput == null) {
                    cleanup();
                    return;
                }
                Message firstMessage = Message.fromString(firstInput);
                if (firstMessage.type() != Message.Type.SET_ALLOWED_TYPE) {
                    sendError("First message must be SET_ALLOWED_TYPE.");
                    cleanup();
                    return;
                }
                try {
                    allowedType = Contact.ContactType.valueOf(firstMessage.content());
                } catch (IllegalArgumentException e) {
                    sendError("Invalid allowed type: " + firstMessage.content());
                    cleanup();
                    return;
                }

                 */

                //Ig I will start my session timer here
                // Schedule timeout check every 5 seconds
                timeoutScheduler.scheduleAtFixedRate(() -> {
                    if (System.currentTimeMillis() - sessionTimer > 15 * 60 * 1000) {
                        sendError("Session timeout");
                        cleanup();
                    }
                }, 0, 5, TimeUnit.SECONDS);
                String input;
                //Now that TLS secures the connection, messages are in plain text.
                //The bulk of the client interactions happen here.
                while ((input = in.readLine()) != null) {
                    //I started my timer first in when I initialized sessionTimer,
                    //Now that I am in my while loop, I will reset it after each time
                    //I handle a message.
                    sessionTimer= System.currentTimeMillis();

                    Message message = Message.fromString(input);
                    handleMessage(message);
                    Logger.log(input);
                }

            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                cleanup();
            }
        }


        //Main place which decides what to do with the input from the client
        private void handleMessage(Message message) {
            try {
                switch (message.type()) {
                    case ADD_CONTACT -> {
                        String[] parts = message.content().split(":");
                        //Use while loop to add parts rather than hardcoding the size
                        if(parts[0].equals("QQQ")) {
                            ContactServer.running = false;

                        }
                        else if (parts.length == 5) {
                            //Notice the switch between 4 and 3 for the company name and the address
                            Contact contact = new Contact(parts[0], parts[1], parts[2], parts[4], parts[3], allowedType);

                            if (ContactValidator.isValid(contact)) {
                                contactManager.addContact(contact);

                            }else {
                                sendError("Invalid contact format.");
                                break;
                            }
                            //This is depreciated now, We always receive parts size 5.
                        }else if ((parts.length == 4)) {
                            if( parts[1].isEmpty()) {
                                Contact contact = new Contact(parts[0]," ",parts[1], parts[2],parts[3], allowedType );
                                if (ContactValidator.isValid(contact)) {
                                    contactManager.addContact(contact);

                                }else{
                                    sendError("Invalid contact format.");
                                    break;
                                }
                            }else if (parts[2].isEmpty()) {
                                Contact contact = new Contact(parts[0], parts[1]," ", parts[3], parts[2], allowedType );
                                if (ContactValidator.isValid(contact)) {
                                    contactManager.addContact(contact);

                                }else {
                                    sendError("Invalid contact format.");
                                    break;
                                }
                            }
                        }
                        server.broadcastContactsList();
                    }
                    case GET_CONTACTS -> sendContactsList();

                    case DELETE_CONTACT -> {
                        String messageStr = message.content();
                        //This part is depreciated
                        Contact result = null;
                        List<Contact> options= contactManager.searchContacts(messageStr);
                        for (Contact contact : options ) {
                            if (contact.getType() == allowedType) {
                                result = contact;
                                break;
                            }
                        }

                        contactManager.deleteContact(messageStr);
                        server.broadcastContactsList();
                    }
                    case SEARCH_CONTACTS -> {
                        String query = message.content();
                        StringBuilder result = new StringBuilder();
                        for (Contact contact : contactManager.searchContacts(query)) {
                            result.append(contact.toString()).append("\n");
                            sendResponse(result.toString());
                            result.setLength(0);
                        }
                    }
                }
            } catch (Exception e) {
                sendError("Error processing request: " + e.getMessage());
            }
        }


        //Senders, need it because the inputs need a bit of parsing to Message which is cumbersome
        private void sendResponse(String content) {
            try {
                Message response = new Message(Message.Type.RESPONSE, content);
                out.println(response.toString());
            } catch (Exception e) {
                System.err.println("Error sending response: " + e.getMessage());
            }
        }
        private void sendError(String error) {
            try {
                Message errorMessage = new Message(Message.Type.ERROR, error);
                out.println(errorMessage.toString());
            } catch (Exception e) {
                System.err.println("Error sending error: " + e.getMessage());
            }
        }
        private void sendContactsList() {
            StringBuilder result = new StringBuilder();

            for (Contact contact : contactManager.getAllContacts()) {
                if (contact.getType() == allowedType) { //  only send contacts matching the allowed type
                    //My stupidity here shone when I used /n instead of \n
                    result.append(contact.toString()).append("/n");
                    //sendResponse(result.toString());
                    // result.setLength(0);
                }
                //sendResponse(result.toString());
                //result.setLength(0);
            }
            sendResponse(result.toString());

        }

        //Final cleanup
        private void cleanup(){
            try {
                in.close();
                out.close();
                clientSocket.close();
                timeoutScheduler.shutdown();
                System.out.println("Client socket closed"+ clientSocket.getInetAddress());
            } catch (IOException e) {
                System.err.println("Error cleaning up client handler: " + e.getMessage());
            }finally {
                server.removeClientHandler(this);
            }
        }
    }

}

