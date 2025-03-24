public class UserRegistration {
    public static void main(String[] args) {
        String username="admin";
        String password="admin";
        Contact.ContactType contactType= Contact.ContactType.SALES;
        User.UserRegistration.registerUser(username,password,contactType);

    }
}
