package entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@EqualsAndHashCode(callSuper=true)
public class Client implements User{
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private long phoneNumber;
    private String city;
    private double rating;
}

