package service;

import java.util.List;

import org.springframework.stereotype.Service;

import entity.Event;
import repository.EventElasticsearchRepository;

@Service
public class EventElasticsearchService {

    private final EventElasticsearchRepository eventElasticsearchRepository;

    public EventElasticsearchService(EventElasticsearchRepository eventElasticsearchRepository) {
        this.eventElasticsearchRepository = eventElasticsearchRepository;
    }

    public List<Event> getByEventStatus(String eventStatus) {
        return eventElasticsearchRepository.findByEventStatus(eventStatus);
    }

    public List<Event> getByEventType(String eventType) {
        return eventElasticsearchRepository.findByEventType(eventType);
    }

    public List<Event> getByEntityType(String entityType) {
        return eventElasticsearchRepository.findByEntityType(entityType);
    }

    public List<Event> getByUserId(String userId) {
        return eventElasticsearchRepository.findByUserId(userId);
    }

    public List<Event> getByEntityId(String entityId) {
        return eventElasticsearchRepository.findByEntityId(entityId);
    }

    public List<Event> getAllEvents() {
        return eventElasticsearchRepository.getAllEvents();
    }
}