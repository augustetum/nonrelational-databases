package controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
        return ResponseEntity.ok(eventLog);
    }

    @GetMapping("/date")
    public ResponseEntity<?> getByDate(@RequestParam String start, @RequestParam String end) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        LocalDate startDate = LocalDate.parse(start, dtf);
        Instant startInstant = startDate.atStartOfDay(ZoneId.of("Europe/Vilnius")).toInstant();

        LocalDate endDate = LocalDate.parse(end, dtf);
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.of("Europe/Vilnius")).toInstant();

        List<Event> eventLog = eventLogService.getByDate(startInstant, endInstant);
        if (eventLog.isEmpty())
            return ResponseEntity.ok("Nothing to show");
        return ResponseEntity.ok(eventLog);
    }

}
