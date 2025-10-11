package dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FreelancerDetailsDto {
    private String id;
    private String firstName;
    private String lastName;
    private BigDecimal rating;
    private long phoneNumber;
    private String city;
}
