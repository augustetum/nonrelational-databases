package dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LeaderboardDetailsDto {
    private String id;
    private String firstName;
    private String lastName;
    private BigDecimal rating;
    private int reviewNum;
}
