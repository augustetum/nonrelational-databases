package entity;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.bson.BsonType;

import java.util.Date;

@Data
public class Booking {
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;
    private Date time;
    private String address;
    private String details;
}
