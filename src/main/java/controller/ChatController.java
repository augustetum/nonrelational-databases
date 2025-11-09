package controller;

import entity.Message;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import dto.CreateConversationRequestDto;
import dto.AddMessageDto;
import service.ChatService;
import service.CustomClientDetails;
import service.CustomFreelancerDetails;

import java.util.List;

@CrossOrigin(origins = "*") 
@Controller
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload AddMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) {
            System.err.println("Username not found in session!");
            return;
        }
        
        // TODO: where the fuck username comes from
        chatService.sendPrivateMessage(message.getConversationId(), username, message.getContent());
    }

    @GetMapping("/api/chat/history/{conversationId}")
    public ResponseEntity<?> getConversationHistory(@PathVariable String conversationId) {
        List<Message> result = chatService.getConversationHistory(conversationId);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/chat/messages/{userId}")
    public ResponseEntity<?> getUserMessages(@PathVariable String userId) {
        List<Message> result = chatService.getUserMessages(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/chat")
    public ResponseEntity<?> addConversation(Authentication authentication, @RequestBody CreateConversationRequestDto newConversationDto) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } 
        else {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        }

        String recipientId = newConversationDto.getRecipientId();
        if (isClient) {
            chatService.createConversation(userId, recipientId);    
        } 
        else {
            chatService.createConversation(recipientId, userId);   
        }

        return ResponseEntity.ok().build();
    }
}

