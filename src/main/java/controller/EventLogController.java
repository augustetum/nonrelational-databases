package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import service.EventLogService;

@RestController
@RequestMapping("api/event-log")
public class EventLogController {
    @Autowired
    EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(eventLogService.getAllEvents());
    }
}
