package entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
}
