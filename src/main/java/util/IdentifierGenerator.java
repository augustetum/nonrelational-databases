package util;

import org.bson.types.ObjectId;

public class IdentifierGenerator {
    public static String generateId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }
}