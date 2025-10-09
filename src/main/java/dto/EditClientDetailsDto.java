package dto;

import lombok.Data;

@Data
public class EditClientDetailsDto {
    private String firstName;
    private String lastName;
    private String email;
    private long phoneNumber;
    private String city;
}
