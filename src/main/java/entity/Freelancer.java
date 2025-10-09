package entity;

import lombok.Data;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.util.List;

@Data
public class Freelancer {
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String city;
    private String rank;
    private int jobsCompleted;
    private double rating;

    private List<Workfield> workfields;
}
