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
    private Date reservedAt;

    public Booking(Date time, String address, String details, String freelancerId, String clientId) {
        this.time = time;
        this.address = address;
        this.details = details;
        this.freelancerId = freelancerId;
        this.clientId = clientId;
        this.reservedAt = new Date();
    }

    public Booking() {
    }
}