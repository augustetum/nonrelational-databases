package entity;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import lombok.Data;

@Data
public class Review {
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;
    private double stars;
    private String details;
}
