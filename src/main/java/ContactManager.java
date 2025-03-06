import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class ContactManager {

    static final Map<UUID, Contact> contacts = new ConcurrentHashMap<>();
    private final File contactsFile;
    private Connection connection;

    public ContactManager() throws IOException {
        this.contactsFile = new File(AppConfig.CONTACTS_FILE);
        connectToDatabase();
        loadContactsFromDatabase();
        //loadContacts();
    }

    private void connectToDatabase() {
        try{
            String url = "jdbc:mysql://localhost:3306/contacts_db";
            String username = "root";
            String password = "";
           // Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(url,username,password);
            System.out.println("Connected to database");
        }catch (SQLException e){
            System.err.println("Error connecting to database"+ e.getMessage());
        }
    }

    private void loadContactsFromDatabase() {
        String sql = "SELECT * FROM contacts";
        try(Statement stmt= connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                UUID id = UUID.fromString(rs.getString("id"));
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String company = rs.getString("company");
                String address = rs.getString("address");
                String type = rs.getString("type");

                Contact contact= new Contact(String.valueOf(id),name, email, phone,company, address, Contact.ContactType.valueOf(type));
                contacts.put(id, contact);
            }
            System.out.println("Loaded from contacts_db " + contacts.size() + " contacts");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addContact(Contact contact) {
        contacts.put(contact.getId(), contact);
        String query = "INSERT INTO contacts (id, name, phone, email, company, address, `type`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setString(1,contact.getId().toString());
            stmt.setString(2,contact.getName());
            stmt.setString(3,contact.getPhone());
            stmt.setString(4,contact.getEmail());
            stmt.setString(5,contact.getCompany());
            stmt.setString(6,contact.getAddress());
            stmt.setString(7,contact.getType().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding contact: " + e.getMessage());
        }
    }

    public synchronized void updateContact(Contact contact) {
        if (contacts.containsKey(contact.getId())) {
            contacts.put(contact.getId(), contact);
            String query = "UPDATE contacts SET name = ?, phone = ?, email = ?, company = ?, address = ?, type = ? WHERE id = ?";
            contactQueryExecution(contact, query);
        }
    }

    private void contactQueryExecution(Contact contact, String query) {
        try(PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setString(1,contact.getId().toString());
            stmt.setString(2,contact.getName());
            stmt.setString(3,contact.getPhone());
            stmt.setString(4,contact.getEmail());
            stmt.setString(5,contact.getCompany());
            stmt.setString(6,contact.getAddress());
            stmt.setString(7,contact.getType().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding contact: " + e.getMessage());
        }

    }

    public synchronized void deleteContact(String contact) {
        List<Contact> list= searchContacts(contact);

            UUID id = (list.getFirst().getId());
            String query = "DELETE FROM contacts WHERE id = ?";
            try(PreparedStatement stmt = connection.prepareStatement(query)){
                stmt.setString(1,id.toString());
                stmt.executeUpdate();
                System.out.println("Deleted contact " + contact);
                contacts.remove(id);
            }catch (SQLException e){
                System.err.println("Error deleting contact: " + e.getMessage());
            }

    }

    /*
    public synchronized void deleteContact(String id) {
        try {
            UUID contactId = UUID.fromString(id);
            if (contacts.containsKey(contactId)) {
                contacts.remove(contactId);
                String query = "DELETE FROM contacts WHERE id = ?";
                try(PreparedStatement stmt = connection.prepareStatement(query)){
                    stmt.setString(1,id);
                    stmt.executeUpdate();

                }catch (SQLException e){
                    System.err.println("Error deleting contact: " + e.getMessage());
                }
                System.out.println("1 contact deleted.");
                saveContacts();
            } else {
                System.out.println("No contact found with the given ID.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid contact ID format.");
        }
    }
     */


    // Gets a contact from the hashmap.
    public Contact getContact(String id) {
        return contacts.get(UUID.fromString(id));
    }

    //Gets all contacts from the hashmap
    public Collection<Contact> getAllContacts() {
        return new ArrayList<>(contacts.values());
    }


    public List<Contact> searchContacts(String query) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE"+" name LIKE ? OR phone LIKE ? OR email LIKE ? OR company LIKE ? OR address LIKE ? OR type LIKE ?";
        try(PreparedStatement stmt =connection.prepareStatement(sql)){
            String searchQuery= "%"+query+"%";
            for (int i =1; i<=6;++i){
                stmt.setString(i,searchQuery);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String company = rs.getString("company");
                String address = rs.getString("address");
                String typeStr = rs.getString("type");
                Contact contact = new Contact(id.toString(),name, phone, email, company, address, Contact.ContactType.valueOf(typeStr));
                results.add(contact);
            }


        } catch (SQLException e) {
            System.err.println("Error searching contacts: " + e.getMessage());
        }
        return results;
    }

    public  List<Contact> searchContactsBeta(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList(); // Return an empty list if query is null/empty
        }

        String lowercaseQuery = query.toLowerCase().trim();

        return contacts.values().stream()
                .filter(c -> containsIgnoreCase(c.getName(), lowercaseQuery) ||
                        containsIgnoreCase(c.getEmail(), lowercaseQuery) ||
                        (c.getPhone() != null && c.getPhone().trim().contains(query)) ||
                        containsIgnoreCase(c.getCompany(), lowercaseQuery))
                .toList();
    }

    // Utility method to handle null-safe string comparison
    private  boolean containsIgnoreCase(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }


//Depreciated Functions when we used txt files to store contacts.
    private void loadContacts() throws IOException {
        contactsFile.createNewFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(contactsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 7){
                    Contact contact = new Contact(parts[1], parts[2], parts[3], parts[5], parts[4]);
                    contact.setType(parts[6]);
                    contacts.put(UUID.fromString(parts[0]), contact);
                }
                if (parts.length == 6) {
                    Contact contact = new Contact(parts[1], parts[2], parts[3], parts[5], parts[4]);
                    contacts.put(UUID.fromString(parts[0]), contact);
                }else if ((parts.length == 5)) {
                    if( parts[2].isEmpty()) {
                        Contact contact = new Contact(parts[1]," ",parts[2], parts[3],parts[4] );

                        contacts.put(UUID.fromString(parts[0]), contact);
                    }else if (parts[3].isEmpty()) {
                        Contact contact = new Contact(parts[1], parts[2]," ", parts[4], parts[3]);
                        contacts.put(UUID.fromString(parts[0]), contact);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading contacts: " + e.getMessage());
        }
    }

    private void saveContacts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(contactsFile))) {
            for (Contact contact : contacts.values()) {
                writer.write(contact.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving contacts: " + e.getMessage());
        }
    }
}

