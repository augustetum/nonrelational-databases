package repository;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private final PreparedStatement insertStatement;
    private final PreparedStatement insertByTimeStatement;
    private final PreparedStatement selectAllStatement;
    private final PreparedStatement selectByUserStatement;
    private final PreparedStatement selectByDateStatement;

    public EventLogRepository(CqlSession session) {
        this.session = session;

        this.insertStatement = session.prepare(
                "INSERT INTO events (id, time, entityType, entityId, eventType, eventStatus, userId, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        this.insertByTimeStatement = session.prepare(
                "INSERT INTO eventsByTime (timeBucket, time, id, userId, entityType, entityId, eventType, eventStatus, details) VALUES (?,?,?,?,?,?,?,?,?)");

        this.selectAllStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM events");

        this.selectByUserStatement = session.prepare(
                "SELECT id, time, entityType, entityId, eventType, eventStatus, userId, details FROM events WHERE userId = ?");

        this.selectByDateStatement = session.prepare(
                "SELECT id, timeBucket, time, entityType, entityId, eventType, eventStatus, userId, details FROM eventsByTime WHERE timeBucket = ? AND time >= ? AND time <= ?");

    }

    // CREATE TABLE IF NOT EXISTS darbsciu_rankuciu_klubas.events (id TEXT, time
    // TIMESTAMP, entityType TEXT, entityId TEXT, eventType TEXT, eventStatus TEXT,
    // userId TEXT, details TEXT, PRIMARY KEY (userId, time, id));

    // CREATE TABLE darbsciu_rankuciu_klubas.eventsByTime ( timeBucket BIGINT, time
    // TIMESTAMP, id TEXT,
    // userId TEXT, entityType TEXT, entityId TEXT, eventType TEXT, eventStatus
    // TEXT, details TEXT, PRIMARY KEY (timeBucket, time, id)) WITH CLUSTERING
    // ORDER BY (time DESC);

    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(IdentifierGenerator.generateId());
        }

        BoundStatement bound = insertStatement.bind(
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

        session.execute(bound);
        session.execute(timeBound);
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