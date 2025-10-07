package entity;

import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class Booking {
    private String id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private Date time;
    private String address;
    private String details;
    private String clientId;
    private String freelancerId;
}

/*

- istraukt konkretaus user'io booking'us 

- pakurt nauja bookinga

- pakeisti booking'o detales

- istrint (cancel) booking'a
 */