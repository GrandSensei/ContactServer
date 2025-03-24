import org.mindrot.jbcrypt.BCrypt;

public class User {



    //Things the user will have
    private String username;
    private String password; //contains the salt internally, we will do it
    private Contact.ContactType contactType;

    public User(String username, String password,  Contact.ContactType contactType) {
        setUsername(username);
        setPassword(password);
        setContactType(contactType);
    }

    //getters
    public String getUsername() {
        return username;
    }
    public String getPasswordHash() {
        return password;
    }
    public Contact.ContactType getContactType() {
        return contactType;
    }

    //Setters
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setContactType(Contact.ContactType contactType) {
        this.contactType = contactType;
    }


    public static class UserRegistration{
        public static void registerUser(String username, String plainPassword,  Contact.ContactType contactType){
            String hashedPassword= BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            User user= new User(username, hashedPassword, contactType);
            new UserManager().createUser(user);
        }
    }

}

