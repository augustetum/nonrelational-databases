package entity;

import lombok.Data;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

@Data
public class Review {
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;
    private double rating;
    private String details;
}
