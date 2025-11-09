package service;

import entity.Conversation;
import entity.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import dto.ClientDetailsDto;
import dto.FreelancerDetailsDto;
import repository.ClientRepository;
import repository.FreelancerRepository;
import repository.MessageRepository;
import util.IdentifierGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService{
    private final MessageRepository messageRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientRepository clientRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository, FreelancerRepository freelancerRepository, ClientRepository clientRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.freelancerRepository = freelancerRepository;
        this.clientRepository = clientRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendPrivateMessage(String conversationId, String senderId, String content) {
        Conversation conversation = messageRepository.getConversationById(conversationId);
        if (conversation == null) {
            return;
        }

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
        Conversation conversation = messageRepository.getConversationById(conversationId);

        if (conversation == null) {
            return null;
        }

        return messageRepository.findByConversationId(conversationId, 50);
    }

    public List<Message> getUserMessages(String senderId) {
        return messageRepository.findBySenderId(senderId, 50);
    }

    public void createConversation(String freelancerId, String clientId) {
        // check if freelancer exists
        Optional<FreelancerDetailsDto> maybeFreelancerDetails = freelancerRepository.getDetails(freelancerId);
        
        if (!maybeFreelancerDetails.isPresent()) {
            return;
        }   

        // check if client exists
        Optional<ClientDetailsDto> maybeClientDetails = clientRepository.getDetails(clientId);
        
        if (!maybeClientDetails.isPresent()) {
            return;
        }  

        // check if such conversation exists already
        Conversation conversation = messageRepository.getConversationByUsers(freelancerId, clientId);
        
        if (conversation != null) {
            return;
        }

        // if such conversation doesn't exist, create new one
        messageRepository.addConversation(freelancerId, clientId);
    }
}
