package repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import entity.Message;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageRepository {
    private final CqlSession session;
    private final PreparedStatement insertStatement;
    private final PreparedStatement selectByConversationStatement;

    public MessageRepository(CqlSession session) {
        this.session = session;

        this.insertStatement = session.prepare(
                "INSERT INTO messages (conversation_id, timestamp, id, from_user, to_user, content) " +
                        "VALUES (?, ?, ?, ?, ?, ?)");

        this.selectByConversationStatement = session.prepare(
                "SELECT conversation_id, timestamp, id, from_user, to_user, content " +
                        "FROM messages WHERE conversation_id = ? LIMIT ?");
    }

    public void save(Message message, String conversationId) {
        Instant timestamp = message.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        BoundStatement bound = insertStatement.bind(
                conversationId,
                timestamp,
                message.getId(),
                message.getFrom(),
                message.getTo(),
                message.getContent()
        );

        session.execute(bound);
    }

    public List<Message> findByConversationId(String conversationId, int limit) {
        BoundStatement bound = selectByConversationStatement.bind(conversationId, limit);
        ResultSet rows = session.execute(bound);
        List<Message> messages = new ArrayList<>();

        for (Row row : rows) {
            messages.add(mapRowToMessage(row));
        }

        return messages;
    }

    private Message mapRowToMessage(Row row) {
        Message message = new Message();
        message.setId(row.getString("id"));
        message.setFrom(row.getString("from_user"));
        message.setTo(row.getString("to_user"));
        message.setContent(row.getString("content"));

        Instant instant = row.getInstant("timestamp");
        if (instant != null) {
            message.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        }

        return message;
    }
}