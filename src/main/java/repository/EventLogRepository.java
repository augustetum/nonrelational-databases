package repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import entity.Event;
import util.IdentifierGenerator;

@Repository
public class EventLogRepository {
    private final CqlSession session;
    private final PreparedStatement insertByUserStatement;
    private final PreparedStatement insertByTimeStatement;
    private final PreparedStatement insertByEntityStatement;
    private final PreparedStatement insertStatement;
    private final PreparedStatement selectAllStatement;
    private final PreparedStatement selectByUserStatement;
    private final PreparedStatement selectByDateStatement;
    private final PreparedStatement selectByEntityStatement;
    private final PreparedStatement selectByIdStatement;

    public EventLogRepository(CqlSession session) {
        this.session = session;

        this.insertByUserStatement = session.prepare(
                "INSERT INTO eventsByUser (id, time, entityType, entityId, eventType, eventStatus, userId, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        this.insertByTimeStatement = session.prepare(
                "INSERT INTO eventsByTime (timeBucket, time, id, entityType, entityId, eventType, eventStatus, userId, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        this.insertByEntityStatement = session.prepare(
                "INSERT INTO eventsByEntity (id, time, entityType, entityId, eventType, eventStatus, userId, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        this.insertStatement = session.prepare(
                "INSERT INTO events (id, time, entityType, entityId, eventType, eventStatus, userId, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        this.selectAllStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM eventsByUser");

        this.selectByUserStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM eventsByUser WHERE userId = ?");

        this.selectByDateStatement = session.prepare(
                "SELECT id, timeBucket, time, entityType, entityId, eventType, eventStatus, userId, details FROM eventsByTime WHERE timeBucket = ? AND time >= ? AND time <= ?");

        this.selectByEntityStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM eventsByEntity WHERE entityType = ?");

        this.selectByIdStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM events WHERE id = ?");

    }

    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(IdentifierGenerator.generateId());
        }

        BoundStatement userBound = insertByUserStatement.bind(
                event.getId(),
                event.getTime(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getEventStatus(),
                event.getUserId(),
                event.getDetails());

        BoundStatement timeBound = insertByTimeStatement.bind(
                getTimeBucket(event.getTime()),
                event.getTime(),
                event.getId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getEventStatus(),
                event.getUserId(),
                event.getDetails());

        BoundStatement entityBound = insertByEntityStatement.bind(
                event.getId(),
                event.getTime(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getEventStatus(),
                event.getUserId(),
                event.getDetails());

        BoundStatement bound = insertStatement.bind(
                event.getId(),
                event.getTime(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getEventStatus(),
                event.getUserId(),
                event.getDetails());

        System.out.println("Writing to: " + session.getMetadata().getNodes().values());
        session.execute(userBound);
        session.execute(timeBound);
        session.execute(entityBound);
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

    public List<Event> getByUserId(String userId) {
        ResultSet rows = session.execute(selectByUserStatement.bind(userId));
        List<Event> events = new ArrayList<>();

        for (Row row : rows) {
            events.add(mapRowToEvent(row));
        }

        return events;
    }

    public List<Event> getByDate(Instant start, Instant end) {
        List<Event> events = new ArrayList<>();

        long startBucket = getTimeBucket(start);
        long endBucket = getTimeBucket(end);

        for (long bucket = startBucket; bucket <= endBucket; bucket++) {
            BoundStatement bound = selectByDateStatement.bind(bucket, start, end);
            ResultSet rs = session.execute(bound);

            for (Row row : rs) {
                events.add(mapRowToEvent(row));
            }

        }
        events.sort(Comparator.comparing(Event::getTime).reversed());
        return events;
    }

    public List<Event> getByEntityType(String entityType) {
        ResultSet rows = session.execute(selectByEntityStatement.bind(entityType));
        List<Event> events = new ArrayList<>();

        for (Row row : rows) {
            events.add(mapRowToEvent(row));
        }

        return events;
    }

    public Event getById(String id) {
        ResultSet rows = session.execute(selectByIdStatement.bind(id));
        Event event = null;

        for (Row row : rows) {
            event = mapRowToEvent(row);
        }

        return event;
    }

    private Event mapRowToEvent(Row row) {
        Event event = new Event();
        event.setId(row.getString("id"));
        event.setTime(row.getInstant("time"));
        event.setEntityType(row.getString("entityType"));
        event.setEntityId(row.getString("entityId"));
        event.setEventType(row.getString("eventType"));
        event.setEventStatus(row.getString("eventStatus"));
        event.setUserId(row.getString("userId"));
        event.setDetails(row.getString("details"));
        return event;
    }

    private Long getTimeBucket(Instant time) {
        return time.atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
    }

}