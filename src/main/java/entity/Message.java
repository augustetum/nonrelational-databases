package entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private String id;
    private LocalDateTime timestamp;
    private String from;
    private String to;
    private String content;
}
