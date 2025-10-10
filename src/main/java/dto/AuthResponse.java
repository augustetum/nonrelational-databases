package dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    
    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
}
