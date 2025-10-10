package entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private long phoneNumber;
    private String city;
    private double rating;
}