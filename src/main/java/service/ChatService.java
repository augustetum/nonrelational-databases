package service;

import entity.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import repository.MessageRepository;
import util.IdentifierGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ChatService{
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendPrivateMessage(String from, String to, String content) {
        Message message = new Message();
        message.setId(IdentifierGenerator.generateId());
        message.setFrom(from);
        message.setTo(to);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message, generateConversationId(from, to));
        messagingTemplate.convertAndSendToUser(to, "/queue/messages", message);
    }

    public List<Message> getConversationHistory(String user1, String user2) {
        return messageRepository.findByConversationId(generateConversationId(user1, user2), 50);
    }

    private String generateConversationId(String user1, String user2) {
        String[] users = {user1, user2};
        Arrays.sort(users);
        return users[0] + "_" + users[1];
    }
}
