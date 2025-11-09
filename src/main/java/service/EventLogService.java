package service;

import java.util.List;

import org.springframework.stereotype.Service;

import entity.Event;
import repository.EventLogRepository;

@Service
public class EventLogService {
    private final EventLogRepository eventLogRepository;

    public EventLogService(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public void logEvent(String entityType, String entityId, String eventType, String eventStatus, String userId,
            String details) {
        Event event = new Event(entityType, entityId, eventType, eventStatus, userId, details);
        eventLogRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventLogRepository.findAll();
    }

    public List<Event> getByUserId(String userId) {
        return eventLogRepository.getByUserId(userId);
    }
}
