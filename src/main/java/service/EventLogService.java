package service;

import entity.Event;
import repository.EventLogRepository;

public class EventLogService {
    private final EventLogRepository eventLogRepository;

    public EventLogService(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public void logEvent(String entityType, String eventStatus, String userId, String details) {
        Event event = new Event(entityType, eventStatus, userId, details);
        eventLogRepository.save(event);
    }
}
