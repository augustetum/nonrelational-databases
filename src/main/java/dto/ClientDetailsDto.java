package dto;

import lombok.Data;

@Data
public class ClientDetailsDto {
    private String id;
    private String firstName;
    private String lastName;
    private double rating;
    private long phoneNumber;
    private String city;
}
