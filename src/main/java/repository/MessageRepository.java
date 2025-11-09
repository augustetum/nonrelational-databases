package repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import entity.Conversation;
import entity.Message;
import util.IdentifierGenerator;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageRepository {
    private final CqlSession session;

    private final PreparedStatement selectConversationById;
    private final PreparedStatement selectConversationByUsers;

    private final PreparedStatement selectByConversation;
    private final PreparedStatement selectBySender;

    private final PreparedStatement insertIntoConversationsById;
    private final PreparedStatement insertIntoConversationsByUsers;

    private final PreparedStatement insertIntoMessagesByConversation;
    private final PreparedStatement insertIntoMessagesBySender;

    public MessageRepository(CqlSession session) {
        this.session = session;

        this.selectConversationById = session.prepare(
            "SELECT conversation_id, freelancer_id, client_id " +
            "FROM conversations_by_id WHERE conversation_id = ?;"
        );

        this.selectConversationByUsers = session.prepare(
            "SELECT conversation_id, freelancer_id, client_id " +
            "FROM conversations_by_users WHERE freelancer_id = ? AND client_id = ?;"
        );

        this.selectByConversation = session.prepare(
            "SELECT message_id, conversation_id, sender_id, content, timestamp " +
            "FROM messages_by_conversation WHERE conversation_id = ? LIMIT ?;"
        );

        this.selectBySender = session.prepare(
            "SELECT message_id, conversation_id, sender_id, content, timestamp " +
            "FROM messages_by_sender WHERE sender_id = ? LIMIT ?;"
        );

        this.insertIntoConversationsByUsers = session.prepare(
            "INSERT INTO conversations_by_users (freelancer_id, client_id, conversation_id) " +
            "VALUES (?, ?, ?);"
        );

        this.insertIntoConversationsById = session.prepare(
            "INSERT INTO conversations_by_id (conversation_id, freelancer_id, client_id) " +
            "VALUES (?, ?, ?);"
        );

        this.insertIntoMessagesByConversation = session.prepare(
            "INSERT INTO messages_by_conversation (message_id, conversation_id, sender_id, content, timestamp) " +
            "VALUES (?, ?, ?, ?, ?);"
        );

        this.insertIntoMessagesBySender = session.prepare(
            "INSERT INTO messages_by_sender (message_id, conversation_id, sender_id, content, timestamp) " +
            "VALUES (?, ?, ?, ?, ?);"
        );
    }

    public List<Message> findByConversationId(String conversationId, int limit) {
        BoundStatement byConversationSelect = selectByConversation.bind(conversationId, limit);

        ResultSet rows = session.execute(byConversationSelect);
        List<Message> messages = new ArrayList<>();

        for (Row row : rows) {
            messages.add(mapRowToMessage(row));
        }

        return messages;
    }

    public List<Message> findBySenderId(String senderId, int limit) {
        BoundStatement bySenderSelect = selectBySender.bind(senderId, limit);

        ResultSet rows = session.execute(bySenderSelect);
        List<Message> messages = new ArrayList<>();

        for (Row row : rows) {
            messages.add(mapRowToMessage(row));
        }

        return messages;
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

    public Conversation getConversationById(String conversationId) {
        BoundStatement conversationSelect = selectConversationById.bind(conversationId);

        ResultSet rows = session.execute(conversationSelect);
        Row row = rows.one();

        if (row == null) {
            return null;
        }

        return mapRowToConversation(row);
    }

    public Conversation getConversationByUsers(String freelancerId, String clientId) {
        BoundStatement conversationSelect = selectConversationByUsers.bind(freelancerId, clientId);

        ResultSet rows = session.execute(conversationSelect);
        Row row = rows.one();

        if (row == null) {
            return null;
        }

        return mapRowToConversation(row);
    }

    public void addConversation(String freelancerId, String clientId) {
        String conversationId = IdentifierGenerator.generateId();

        BoundStatement conversationsByUsersInsert = insertIntoConversationsByUsers.bind(
            freelancerId,
            clientId,
            conversationId
        );

        BoundStatement conversationsByIdInsert = insertIntoConversationsById.bind(
            conversationId,
            freelancerId,
            clientId
        );

        session.execute(conversationsByUsersInsert);
        session.execute(conversationsByIdInsert);
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

    private Conversation mapRowToConversation(Row row) {
        Conversation conversation = new Conversation();

        String conversationId = row.getString("conversation_id");
        conversation.setConversationId(conversationId);
        
        String freelancerId = row.getString("freelancer_id");
        conversation.setFreelancerId(freelancerId);
        
        String clientId = row.getString("client_id");
        conversation.setClientId(clientId);

        return conversation;
    }
}