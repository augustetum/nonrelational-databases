package controller;

import entity.Message;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import dto.AddMessageDto;
import service.ChatService;

import java.security.Principal;
import java.util.List;

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

    @GetMapping("/api/chat/history/{userId}")
    public ResponseEntity<?> getHistory(@PathVariable String userId, Principal principal) {
        List<Message> result = chatService.getConversationHistory(principal.getName(), userId);
        return ResponseEntity.ok(result);
    }
}

