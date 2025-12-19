package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import entity.Event;
import service.EventElasticsearchService;

@RestController
@RequestMapping("api/events/elasticsearch")
public class EventElasticsearchController {

    @Autowired
    private EventElasticsearchService eventElasticsearchService;

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        List<Event> events = eventElasticsearchService.getAllEvents();
        if (events.isEmpty())
            return ResponseEntity.ok("Nothing to show");
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-status")
    public ResponseEntity<?> getByEventStatus(@RequestParam String status) {
        List<Event> events = eventElasticsearchService.getByEventStatus(status);
        if (events.isEmpty())
            return ResponseEntity.ok("Nothing to show");
        return ResponseEntity.ok(events);
    }
}