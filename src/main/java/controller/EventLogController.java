package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import entity.Event;
import service.EventLogService;

@RestController
@RequestMapping("api/event-log")
public class EventLogController {

    @Autowired
    private EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(eventLogService.getAllEvents());
    }

    @GetMapping("/user")
    public ResponseEntity<?> getByUserId(@RequestParam String userId) {
        List<Event> eventLog = eventLogService.getByUserId(userId);
        if (eventLog.isEmpty())
            return ResponseEntity.ok("Nothing to show");
        return ResponseEntity.ok(eventLogService.getByUserId(userId));
    }
}
