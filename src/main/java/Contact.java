import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact {

    public enum ContactType {
        SALES,
        TOS,
        DISC,
        SUPPLIER,
        CUSTOMER,
    }


    private final UUID id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String company;
    private ContactType type;


    public Contact(String name, String phone, String email, String company, String address, ContactType type) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.company = company;
        this.address = address;
        this.type = type;
    }

    public Contact(String name, String phone, String email, String company, String address) {
        this(name, phone, email, company, address, ContactType.CUSTOMER);  // default type if not set
    }

    public Contact(String id, String name, String phone, String email, String company, String address) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.company = company;
        this.address = address;
        this.type = ContactType.CUSTOMER; // default if not provided
    }

    // where you supply the id as a String and explicitly provide the ContactType.
    public Contact(String id, String name, String phone, String email, String company, String address, ContactType type) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.company = company;
        this.address = address;
        this.type = type;
    }


    public Contact(Contact contact) {
        this.id = contact.id;
        this.name = contact.name;
        this.phone = contact.phone;
        this.email = contact.email;
        this.company = contact.company;
        this.address = contact.address;
        this.type = contact.type;
    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getCompany() {
        return company;
    }

    public ContactType getType() {
        return type;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setType(String type) {
        switch (type) {
            case "SALES":
                this.type = ContactType.SALES;
                break;
            case "TOS":
                    this.type = ContactType.TOS;
                    break;
            case "DISC":
                        this.type = ContactType.DISC;
                        break;
            case "SUPPLIER":
                this.type = ContactType.SUPPLIER;
                break;
            case "CUSTOMER":
                    this.type = ContactType.CUSTOMER;
                    break;
                    default:
                        break;

        }
    }

    public static ContactType getContactType(String type) {
        return switch (type) {
            case "SALES" -> ContactType.SALES;
            case "TOS" -> ContactType.TOS;
            case "DISC" -> ContactType.DISC;
            case "SUPPLIER" -> ContactType.SUPPLIER;
            case "CUSTOMER" -> ContactType.CUSTOMER;
            default -> null;
        };
    }

    //Was not aware of the fact that we can use C syntax in Java like this.
    @Override
    public String toString() {
        return String.format("Name: %s | Phone: %s | Email: %s | Company: %s | Address: %s | Type: %s",
                name, phone, email, company, address, type);
    }

    public static Contact fromString(String input) {
        String regex = "Name: (.*?) \\| Phone: (.*?) \\| Email: (.*?) \\| Company: (.*?) \\| Address: (.*?) \\| Type: (.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            return new Contact(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), getContactType(matcher.group(6)));
        } else {
            throw new IllegalArgumentException("Invalid input format");
        }
    }

    public String toFileString() {
        return String.join(":", id.toString(), name, phone, email, company, address, type.toString());
    }
}

