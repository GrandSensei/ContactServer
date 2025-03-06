
public class Users {

    //What is going to happen here?

    //First make a type for userTypes
    public enum UserType {

    }

    //Things the user will have
    String username;
    String password;
    UserType type;

    public Users(String username, String password, UserType type) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public UserType getType() {
        return type;
    }

}
