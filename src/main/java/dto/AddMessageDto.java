package dto;

import lombok.Data;

@Data
public class AddMessageDto {
    private String conversationId;
    private String senderId;
    private String content;
}
