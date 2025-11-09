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

    private final PreparedStatement selectByConversation;

    private final PreparedStatement insertIntoMessagesByConversation;
    private final PreparedStatement insertIntoMessagesBySender;

    public MessageRepository(CqlSession session) {
        this.session = session;

        this.selectByConversation = session.prepare(
            "SELECT message_id, conversation_id, sender_id, content, timestamp " +
            "FROM messages_by_conversation WHERE conversation_id = ? LIMIT ?"
        );

        this.insertIntoMessagesByConversation = session.prepare(
            "INSERT INTO messages_by_conversation (message_id, conversation_id, sender_id, content, timestamp) " +
            "VALUES (?, ?, ?, ?, ?)"
        );

        this.insertIntoMessagesBySender = session.prepare(
            "INSERT INTO messages_by_sender (message_id, conversation_id, sender_id, content, timestamp) " +
            "VALUES (?, ?, ?, ?, ?)"
        );
    }

    public void save(Message message) {
        Instant timestamp = message.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        BoundStatement byChatInsert = insertIntoMessagesByConversation.bind(
            message.getMessageId(),
            message.getConversationId(),
            message.getSenderId(),
            message.getContent(),
            timestamp
        );

        BoundStatement bySenderInsert = insertIntoMessagesBySender.bind(
            message.getMessageId(),
            message.getConversationId(),
            message.getSenderId(),
            message.getContent(),
            timestamp
        );

        session.execute(byChatInsert);
        session.execute(bySenderInsert);
    }

    public List<Message> findByConversationId(String conversationId, int limit) {
        BoundStatement byChatSelect = selectByConversation.bind(conversationId, limit);

        ResultSet rows = session.execute(byChatSelect);
        List<Message> messages = new ArrayList<>();

        for (Row row : rows) {
            messages.add(mapRowToMessage(row));
        }

        return messages;
    }

    private Message mapRowToMessage(Row row) {
        Message message = new Message();

        String messsageId = row.getString("message_id");
        message.setMessageId(messsageId);

        String conversationId = row.getString("conversation_id");
        message.setConversationId(conversationId);

        String senderId = row.getString("sender_id");
        message.setSenderId(senderId);

        String content = row.getString("content");
        message.setContent(content);

        Instant instant = row.getInstant("timestamp");
        if (instant != null) {
            message.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        }

        return message;
    }
}