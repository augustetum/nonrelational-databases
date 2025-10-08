package dto;

import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class EditBookingRequestDto {
    private String id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private Date time;
    private String address;
    private String details;
}