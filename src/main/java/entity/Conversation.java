package entity;

import lombok.Data;

@Data
public class Conversation {
    private String conversationId;
    private String freelancerId; 
    private String clientId; 
}
