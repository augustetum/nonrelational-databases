package entity;

import java.util.Date;
import java.util.Optional;

import lombok.Data;

@Data
public class Event {
    private String Id;
    private Date time;
    private EntityType type;
    private EventStatus status;
    private String userId;
    private String details;
}
