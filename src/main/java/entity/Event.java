package entity;

import java.time.Instant;

import lombok.Data;

@Data
public class Event {
    public Event(String entityType, String entityId, String eventType, String eventStatus, String userId,
            String details) {
        this.id = null;
        this.time = Instant.now();
        this.entityType = entityType;
        this.entityId = entityId;
        this.eventType = eventType;
        this.eventStatus = eventStatus;
        this.userId = userId;
        this.details = details;
    }

    public Event() {
    }

    private String id;
    private Instant time;
    private String entityType; // su kuo susijes event - logino butu user, palikto review butu review, sukurto
                               // bookingo butu booking
    private String entityId; // pvz bookingo id
    private String eventType;
    private String eventStatus; // ar pavyko
    private String userId; // useris kuris inicijavo
    private String details; // further details - kada, kam review, kas uzbookintas, etc etc
}
