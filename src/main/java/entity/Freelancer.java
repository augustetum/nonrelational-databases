package entity;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import lombok.Data;

@Data
public class Freelancer {
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private long phoneNumber;
    private String city;

    // private List<Workfield> workfields;
}
