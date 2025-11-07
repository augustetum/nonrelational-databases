package repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import entity.Event;
import util.IdentifierGenerator;

public class EventLogRepository {
    private final CqlSession session;
    private final PreparedStatement insertStatement;
    private final PreparedStatement selectAllStatement;

    public EventLogRepository(CqlSession session) {
        this.session = session;

        this.insertStatement = session.prepare(
                "INSERT INTO events (id, time, type, status, eventId, details) VALUES (?, ?, ?, ?)");

        this.selectAllStatement = session.prepare(
                "SELECT id, time, type, status, eventId, details FROM events");
    }

    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(IdentifierGenerator.generateId());
        }
        if (event.getTime() == null) {
            event.setTime(new Date());
        }

        BoundStatement bound = insertStatement.bind(
                event.getId(),
                event.getTime(),
                event.getType(),
                event.getStatus(),
                event.getUserId(),
                event.getDetails());

        session.execute(bound);
        return event;
    }

    public List<Event> findAll() {
        ResultSet rs = session.execute(selectAllStatement.bind());
        List<Event> events = new ArrayList<>();

        for (Row row : rs) {
            events.add(mapRowToEvent(row));
        }

        return events;
    }

    private Event mapRowToEvent(Row row) {
        Event event = new Event();
        event.setId(row.getString("id"));
        event.setTime(row.getLocalDate("time")); // nei vienas cassandros date type nematchina ko man reikia tai mes
                                                 // errorus bet ai bbd sutvarkysiu veliau
        event.setType(row.getEvent("type")); // custom enumu cassandroj geriau nenaudot lmao!!! will fix later
        event.setStatus(row.getStatus("status")); // once again custom enumas lol xd! reik tsg i stringus perconvertuot
        event.setUserId(row.getString("userId"));
        event.setDetails(row.getString("details"));
        return event;
    }

}