import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminClientGUI extends JFrame {

    private static final String SERVER_ADDRESS = AppConfig.SERVER_ADDRESS;
    private static final int SERVER_PORT = 12345;
    private Map<UUID, Contact> contacts = new HashMap<UUID, Contact>();

    private JTabbedPane tabbedPane;
    private final JTextField nameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField companyField = new JTextField(20);
    private final JTextField commentField = new JTextField(20);
    private final JTextField searchField = new JTextField(20);
    private final JTextArea contactList = new JTextArea(20, 50);
  //  private final JTextField usernameField = new JTextField(20);
   // private final JPasswordField passwordField = new JPasswordField(20);

    private JTextArea userList = new JTextArea(20, 50);
    private PrintWriter out;
    private BufferedReader in;
    private SSLSocket socket;

    //Please get some way of login config for this
    //private final Contact.ContactType allowedType = Contact.ContactType.SALES;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new AdminClientGUI().setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error connecting to server: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
            }
        });
    }

    public AdminClientGUI() throws IOException {
        connectToServer();
        if (!performLogin()){
            throw new IOException("Login failed.Exiting.");
        }else {setupGUI();
        startMessageListener();}

    }

    //Handling the login part
    private boolean performLogin() {
        JPanel loginPanel = new JPanel(new GridLayout(2,2));
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        loginPanel.add(new JLabel("Admin Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Admin Password:"));
        loginPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null,loginPanel,"login",JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            sendMessage(new Message(Message.Type.LOGIN_REQUEST,username+":"+password));
            try{
                String response = in.readLine();
                Message resp= Message.fromString(response);
                if (resp.type() == Message.Type.ERROR) {
                    JOptionPane.showMessageDialog(this,"Login Failed"+resp.content(),"Login Error",JOptionPane.ERROR_MESSAGE);
                    return false;
                }else if (resp.type() == Message.Type.RESPONSE_USER) {
                    JOptionPane.showMessageDialog(null,resp.content(),"Login Successful",JOptionPane.INFORMATION_MESSAGE);
                    return true;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,"Login error"+e.getMessage(),"Login Error",JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }
    //Handles all the connections
    private void connectToServer() throws IOException {
        try{
            //Load the client's truststore (which must trust the server's certificate)
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream tsIS = new FileInputStream("clienttruststore.jks")) {
                trustStore.load(tsIS, "changeit".toCharArray());
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            socket = (SSLSocket) ssf.createSocket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (Exception e) {
            throw new IOException("Key exchange failed: " + e.getMessage(), e);
        }

    }

    //the UI
    private void setupGUI() {
        setTitle("Admin Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Create tabbed interface
        tabbedPane = new JTabbedPane();

        // User Management
        JPanel userTab = createUserPanel();
        JPanel contactTab = createContactPanel();

        tabbedPane.addTab("User Management", userTab);
        tabbedPane.addTab("Contact Management", contactTab);

        add(tabbedPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        refreshContacts();
        refreshUsers();
    }

    private JPanel createUserPanel() {
        JPanel userPanel = new JPanel();
        // User List
        userList.setEditable(false);
        JScrollPane userScroll = new JScrollPane(userList);

        // User Controls
        JPanel userControlPanel = new JPanel();
        JButton addUserButton = new JButton("Add User");
        JButton deleteUserButton = new JButton("Delete User");
        JButton refreshUsersButton = new JButton("Refresh");
        JButton searchUserButton = new JButton("Search");

        userControlPanel.add(addUserButton);
        userControlPanel.add(deleteUserButton);
        userControlPanel.add(refreshUsersButton);
        userControlPanel.add(searchUserButton);
       // userControlPanel.add(new JLabel("Username:"));
      //  userControlPanel.add(usernameField);
       // userControlPanel.add(new JLabel("Password:"));
      //  userControlPanel.add(passwordField);
        userPanel.add(userControlPanel, BorderLayout.SOUTH);
        userPanel.add(userScroll, BorderLayout.CENTER);

        refreshUsersButton.addActionListener(e -> refreshUsers());
        deleteUserButton.addActionListener(e -> deleteUser());
        addUserButton.addActionListener(e -> addUser());

        return userPanel;
    }

    private JPanel createContactPanel() {
        JPanel contactPanel = new JPanel();

        contactList.setEditable(false);
        JScrollPane contactScroll = new JScrollPane(contactList);

        JPanel contactControlPanel = new JPanel(new GridLayout(6, 2));
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2, 5, 5));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Company:"));
        inputPanel.add(companyField);
        inputPanel.add(new JLabel("Comment:"));
        inputPanel.add(commentField);
        inputPanel.add(new JLabel("Search:"));
        inputPanel.add(searchField);

        JButton addButton = new JButton("Add Contact");
        JButton clearButton = new JButton("Clear Fields");
        JButton deleteButton = new JButton("Delete Contact");
        JButton refreshButton = new JButton("Refresh");
        JButton searchButton = new JButton("Search");

        contactControlPanel.add(addButton);
        contactControlPanel.add(clearButton);
        contactControlPanel.add(deleteButton);
        contactControlPanel.add(refreshButton);
        contactControlPanel.add(searchButton);

        contactPanel.add(inputPanel, BorderLayout.SOUTH);
        contactPanel.add(contactControlPanel, BorderLayout.NORTH);
        contactPanel.add(contactScroll, BorderLayout.CENTER);

        addButton.addActionListener(e -> addContact());
        clearButton.addActionListener(e-> clearFields());
        deleteButton.addActionListener(e-> deleteContact2());
        refreshButton.addActionListener(e -> refreshContacts());
        searchButton.addActionListener(e -> searchContacts());


        return contactPanel;
    }






    //Handles responses
    private void startMessageListener() {
        new Thread(() -> {
            try  {
                String encryptedMessage;
                while ((encryptedMessage = in.readLine()) != null) {
                    System.out.println("Received from server: " + encryptedMessage);
                    Message message = Message.fromString(encryptedMessage);
                    handleServerMessage(message);
                    //  populateContactsFromList();
                }
            } catch (IOException e) {
                showError("Lost connection to server: " + e.getMessage());
            } catch (Exception e) {
                showError("Decryption error: " + e.getMessage());
            }
        }).start();
    }
    private void handleServerMessage(Message message) {
        if (message.content()!=null) {
            SwingUtilities.invokeLater(() -> {
                switch (message.type()) {
                    //For listing the contact
                    case RESPONSE_USER ->{
                        contactList.setText("");
                        listingContacts(message.content());
                        populateContactsFromList();
                    }
                    case RESPONSE_ADMIN -> {
                        userList.setText("");
                        listingUsers(message.content());
                    }
                    //For any potential errors
                    case ERROR -> showError(message.content());

                }
            });
        }
    }
    private void listingContacts(String content) {
        String[] parts = content.split("/n");
        StringBuilder listedContact = new StringBuilder(contactList.getText());
        contactList.setText("");

        for (String part : parts) {
            if (!part.contentEquals(listedContact)) {
                listedContact.append(part).append("\n");
            }
        }
        contactList.setText(listedContact.toString());
        // contactList.setText(content);
    }
    private void listingUsers(String content) {
        String[] parts = content.split("/n");
        StringBuilder listedUser = new StringBuilder(userList.getText());
        userList.setText("");

        for (String part : parts) {
            if (!listedUser.toString().contains(part)) {
                listedUser.append(part).append("\n");
            }
        }
        userList.setText(listedUser.toString());

    }


    //Function for a local Storage
    private void populateContactsFromList() {
        contacts.clear(); // Clear the current map to avoid duplicates
        String[] lines = contactList.getText().split("\n"); // Split the contactList text into lines
        for (String line : lines) {
            Contact contact = Contact.fromString(line);
            contacts.put(contact.getId(), contact);
        }
        System.out.println("Contacts listed: " + contacts.keySet());
    }

    private void updateContactsList(){
        String[] lines = contactList.getText().split("\n"); // Split the contactList text into lines
        for (String line : lines) {
            Contact contact = Contact.fromString(line);
            //Btw this is wrong but cba to spend time on this.
            if (!contacts.containsValue(contact)) {
                contacts.put(contact.getId(), contact);
            }
        }
        System.out.println("Contacts listed: " + contacts.keySet());
    }


    //Helper functions for GUI of User Panel
    private void addUser() {
        JPanel addUserPanel = new JPanel(new GridLayout(3, 2));
        //All of this looks weird to me ngl
        //need to learn this
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Contact.ContactType> typeCombo = new JComboBox<>(Contact.ContactType.values());

       // addUserPanel.add(new JLabel("Username:"));
        addUserPanel.add(usernameField);
       // addUserPanel.add(new JLabel("Password:"));
        addUserPanel.add(passwordField);
        //addUserPanel.add(new JLabel("Type:"));
        addUserPanel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(this, addUserPanel, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            //Learning this from C was a mindfuck
            String userData = String.format("%s:%s:%s",
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    ((Contact.ContactType) typeCombo.getSelectedItem()).name());
            sendMessage(new Message(Message.Type.ADD_USER, userData));
        }
    }
    private void deleteUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username to delete:");
        if (username != null && !username.isEmpty()) {
            sendMessage(new Message(Message.Type.DELETE_USER, username));
        }
    }
    private void refreshUsers() {
        clearUserList();
        sendMessage(new Message(Message.Type.GET_USERS,""));
    }
    private void clearUserList(){
        userList.setText("");
    }




    //Helper functions for GUI of Contact Panel
    private void addContact() {

        //We add the comment/address first cause otherwise it doesn't register it if its an empty text.
        String contactData = String.join(":",
                nameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                commentField.getText(),
                companyField.getText());
        sendMessage(new Message(Message.Type.ADD_CONTACT, contactData));
        clearFields();
        contactList.setText("");

    }
    private void clearList(){
        contactList.setText("");
    }
    private void refreshContacts() {
        clearList();
        sendMessage(new Message(Message.Type.GET_CONTACTS, ""));
    }
    private void searchContacts() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            clearList();
            sendMessage(new Message(Message.Type.SEARCH_CONTACTS, query));
        } else {
            refreshContacts();
        }
    }
    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        companyField.setText("");
        commentField.setText("");
        searchField.setText("");
    }
    private void deleteContact2(){
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            sendMessage(new Message(Message.Type.DELETE_CONTACT, query));
        }
        clearFields();
    }
    private void deleteContact() {
        String contactData = String.join(":",
                nameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                commentField.getText(),
                companyField.getText());
        sendMessage(new Message(Message.Type.DELETE_CONTACT, contactData));
    }

    private void sendMessage(Message message) {
        out.println(message.toString());
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        refreshContacts();
    }
}
