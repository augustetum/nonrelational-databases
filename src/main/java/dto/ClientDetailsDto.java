package dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ClientDetailsDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal rating;
    private long phoneNumber;
    private String city;
}
