import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    //Deal with all the user database management

    private static List<User> users = new ArrayList<>();
    private static Connection connection;

    public UserManager() {
        ConnectToDatabase();
    }

    //Hmm, I think so for now I should not store my user data in a Hashmap here
    public static void ConnectToDatabase() {
       try{
           String url = "jdbc:mysql://localhost:3306/users_db";
           String username = "root";
           String password = "";

           connection = DriverManager.getConnection(url, username, password);
           System.out.println("Connection established with the userDatabase");

       } catch (SQLException e) {
           System.err.println("Error connecting to the users database");
       }
    }

    public static User findUserByUsername(String username) {
        String query="SELECT password_hash, contact_type FROM users WHERE username = ?";

        try(PreparedStatement stmt= connection.prepareStatement(query)){
            stmt.setString(1, username);
            ResultSet rs=stmt.executeQuery();
            if(rs.next()){
                return new User(
                        username,
                        rs.getString("password_hash"),
                        Contact.ContactType.valueOf(rs.getString("contact_type"))
                );
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the users database");
        }
        return null;
    }

    //Admin specific commands
    public  User registerUser(String username, String plainPassword,  Contact.ContactType contactType){
        String hashedPassword= BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        User user= new User(username, hashedPassword, contactType);
        new UserManager().createUser(user);
        return user;
    }
    public boolean createUser(User user) {

        String query ="INSERT INTO users (username, password_hash, contact_type) VALUES (?, ?, ?)";

        try(
        PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,user.getUsername());
            stmt.setString(2,user.getPasswordHash());
            stmt.setString(3,user.getContactType().toString());
            stmt.executeUpdate();
    }catch (SQLException e) {
            System.err.println("Error connecting to the users database");
        }
        return true;
    }
    public boolean deleteUser(String username) {
        if(findUserByUsername(username) == null){
            return false;
        }
        String query ="DELETE FROM users WHERE username = ?";
        try(PreparedStatement stmt= connection.prepareStatement(query)){
            stmt.setString(1,username);
            stmt.executeUpdate();
        }catch (SQLException e){
            System.err.println("Error connecting to the users database");
        }
        return true;
    }
    public boolean updateUserType(String username,Contact.ContactType newType){
        if(findUserByUsername(username) == null){
            return false;
        }
        if (findUserByUsername(username).getContactType() == newType) {
            System.out.println("Type is already in use");
            return true;
        }
        String query ="UPDATE users SET contact_type = ? WHERE username = ?";
        try(PreparedStatement stmt= connection.prepareStatement(query)){
            stmt.setString(1,newType.toString());
            stmt.setString(2,username);
            stmt.executeUpdate();
        }catch (SQLException e) {
            System.err.println("Error connecting to the users database");
        }
        return true;
    }
    public List<User> getAllUsers(){
        String query ="SELECT * FROM users";
        try(PreparedStatement stmt= connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query)){
            while (rs.next()){
                String username = rs.getString("username");
                String password = rs.getString("password_hash");
                String contactType = rs.getString("contact_type");
                users.add(new User(username,password,Contact.ContactType.valueOf(contactType)));
            }
        }catch (SQLException e) {
            System.err.println("Error connecting to the users database");
        }
        return users;
    }

    //Getter
    public static Connection getConnection() {
        return connection;
    }
}
