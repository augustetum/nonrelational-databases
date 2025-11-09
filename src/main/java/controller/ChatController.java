package controller;

import entity.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    public void sendPrivateMessage(@Payload Message message, Principal principal) {
        chatService.sendPrivateMessage(principal.getName(), message.getTo(), message.getContent());
    }

    @GetMapping("/api/chat/history/{userId}")
    public List<Message> getHistory(@PathVariable String userId, Principal principal) {
        return chatService.getConversationHistory(principal.getName(), userId);
    }
}

