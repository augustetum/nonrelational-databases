package repository;

import java.util.ArrayList;
import java.util.List;

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
                "INSERT INTO events (id, time, type, status, eventId, details) VALUES (?, ?, ?, ?, ?, ?)");

        this.selectAllStatement = session.prepare(
                "SELECT id, time, type, status, eventId, details FROM events");
    }

    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(IdentifierGenerator.generateId());
        }

        BoundStatement bound = insertStatement.bind(
                event.getId(),
                event.getTime(),
                event.getEntityType(),
                event.getEventStatus(),
                event.getUserId(),
                event.getDetails());

        session.execute(bound);
        return event;
    }

    public List<Event> findAll() {
        ResultSet rows = session.execute(selectAllStatement.bind());
        List<Event> events = new ArrayList<>();

        for (Row row : rows) {
            events.add(mapRowToEvent(row));
        }

        return events;
    }

    private Event mapRowToEvent(Row row) {
        Event event = new Event();
        event.setId(row.getString("id"));
        event.setTime(row.getInstant("time"));
        event.setEntityType(row.getString("entityType"));
        event.setEventStatus(row.getString("eventStatus"));
        event.setUserId(row.getString("userId"));
        event.setDetails(row.getString("details"));
        return event;
    }

}