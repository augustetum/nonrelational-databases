package entity;

import lombok.Data;

import java.util.Date;

@Data
public class Booking {
    private String id;
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