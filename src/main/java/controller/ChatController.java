package controller;

import entity.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public void sendPrivateMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username == null) {
            System.err.println("Username not found in session!");
            return;
        }
        chatService.sendPrivateMessage(username, message.getTo(), message.getContent());
    }

    @GetMapping("/api/chat/history/{userId}")
    public List<Message> getHistory(@PathVariable String userId, Principal principal) {
        return chatService.getConversationHistory(principal.getName(), userId);
    }
}

