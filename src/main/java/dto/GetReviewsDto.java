package dto;

import lombok.Data;

@Data
public class GetReviewsDto {
    public String revieweeId;
    public Boolean isClient;
}
