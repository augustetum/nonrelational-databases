package service;

import entity.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import repository.MessageRepository;
import util.IdentifierGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService{
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public ChatService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate, SimpUserRegistry userRegistry) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    public void sendPrivateMessage(String conversationId, String senderId, String content) {
        Message message = new Message();
        message.setMessageId(IdentifierGenerator.generateId());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        String destination = "/queue/messages-" + conversationId;
        messagingTemplate.convertAndSend(destination, message);
    }

    public List<Message> getConversationHistory(String conversationId) {
        return messageRepository.findByConversationId(conversationId, 50);
    }

    public List<Message> getUserMessages(String senderId) {
        return messageRepository.findBySenderId(senderId, 50);
    }
}
