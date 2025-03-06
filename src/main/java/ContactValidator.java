public class ContactValidator {
    public static boolean isValid(Contact contact) {
        return  contact != null &&
                isValidName(contact.getName()) &&
                eitherContact(contact.getEmail(), contact.getPhone()) &&
                isValidCompany(contact.getCompany()) &&
                isValidComment(contact.getAddress());
    }

    private static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.length() <= AppConfig.MAX_NAME_LENGTH;
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false; // Reject null or blank emails
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") &&
                email.length() <= AppConfig.MAX_EMAIL_LENGTH;
    }

    private static boolean eitherContact(String email, String phone) {
        return (isValidEmail(email)) || (isValidPhone(phone));
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false; // Reject null or blank phone numbers
        }
        return phone.matches(
                "(?:\\+\\d{1,3} )?" +             //  country code (+1, +91, etc.)
                        "(\\d{10}" +              // 1234567890
                        "|\\d{3}-\\d{3}-\\d{4}" + // 123-456-7890
                        "|\\(\\d{3}\\) \\d{3}-\\d{4}" + // (123) 456-7890
                        "|\\(\\d{3}\\)\\d{3}-\\d{4})" // (123)456-7890
        ) && phone.length() <= AppConfig.MAX_PHONE_LENGTH;
    }


    private static boolean isValidCompany(String company) {
        if (company == null || company.trim().isEmpty()) {
            return false;
        }
        return company.length() <= AppConfig.MAX_COMPANY_LENGTH;
    }

    private static boolean isValidComment(String comment) {
        return comment==null || comment.length() <= AppConfig.MAX_COMMENT_LENGTH;
    }
}
