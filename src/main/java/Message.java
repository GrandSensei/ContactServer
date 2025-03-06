public record Message(Type type, String content) {
    public enum Type {
        //Command Type Messages
        ADD_CONTACT,
        UPDATE_CONTACT,
        DELETE_CONTACT,
        GET_CONTACTS,
        SEARCH_CONTACTS,
        EMPTY,

        //A response to the action by the client
        RESPONSE,
        ERROR,

        //Restricted Access permissions
        SET_ALLOWED_TYPE,
        LOGIN_REQUEST,


    }
    @Override
    public String toString() {
        return type + ":" + content;
    }

    public static Message fromString(String str) {
        try {
            String[] parts = str.split(":", 2);
            if (parts.length == 1) {
                return new Message(Type.EMPTY, "");
            }
            if (parts.length < 2) {
                throw new IllegalArgumentException("Message format invalid: " + str);
            }
            return new Message(Type.valueOf(parts[0]), parts[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse message: " + str, e);
        }
    }

}


