package entity;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

@Data
public class Event {
    private String Id;
    private Instant time;
    private String entityType; // su kuo susijes event - logino butu user, palikto review butu review, sukurto
                               // bookingo butu booking
    private String eventStatus; // ar pavyko
    private String userId; // useris kuris inicijavo
    private String details; // further details - kada, kam review, kas uzbookintas, etc etc
}
