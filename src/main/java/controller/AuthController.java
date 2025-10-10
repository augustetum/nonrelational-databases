package controller;

import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import service.ClientAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FreelancerAuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientAuthService clientAuthService;
    private final FreelancerAuthService freelancerAuthService;

    public AuthController(ClientAuthService clientAuthService, FreelancerAuthService freelancerAuthService) {
        this.clientAuthService = clientAuthService;
        this.freelancerAuthService = freelancerAuthService;
    }

    @PostMapping("/register/{userType}")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, @PathVariable String userType) {
        if(userType.equals("client"))  return ResponseEntity.ok(clientAuthService.register(request));
        else return ResponseEntity.ok(freelancerAuthService.register(request));
    }

    @PostMapping("/login/{userType}")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, @PathVariable String userType) {
        if(userType.equals("client")) return ResponseEntity.ok(clientAuthService.authenticate(request));
        else return ResponseEntity.ok(freelancerAuthService.authenticate(request));
    }
}