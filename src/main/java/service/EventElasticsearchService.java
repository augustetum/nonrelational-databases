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

    public List<Event> getAllEvents() {
        return eventElasticsearchRepository.getAllEvents();
    }
}