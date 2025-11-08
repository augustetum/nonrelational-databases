package controller;

import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import entity.Client;
import service.ClientAuthService;
import service.ClientService;
import service.EventLogService;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FreelancerAuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientAuthService clientAuthService;
    private final FreelancerAuthService freelancerAuthService;
    private final EventLogService eventLogService;
    private final ClientService clientService;

    public AuthController(ClientAuthService clientAuthService, FreelancerAuthService freelancerAuthService,
            EventLogService eventLogService, ClientService clientService) {
        this.clientAuthService = clientAuthService;
        this.freelancerAuthService = freelancerAuthService;
        this.eventLogService = eventLogService;
        this.clientService = clientService;
    }

    @PostMapping("/register/{userType}")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, @PathVariable String userType) {
        if (userType.equals("client")) {
            AuthResponse authResponse = clientAuthService.register(request);
            Optional<Client> client = clientService.getByEmail(authResponse.getEmail());

            if (client.isPresent()) {
                Client user = client.get();
                eventLogService.logEvent("CLIENT", user.getId(), "CLIENT_REGISTER", "SUCCESS", user.getId(),
                        "REGISTER");
            }
            return ResponseEntity.ok(authResponse);
        }

        else
            return ResponseEntity.ok(freelancerAuthService.register(request));
    }

    @PostMapping("/login/{userType}")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, @PathVariable String userType) {
        if (userType.equals("client"))
            return ResponseEntity.ok(clientAuthService.authenticate(request));
        else
            return ResponseEntity.ok(freelancerAuthService.authenticate(request));
    }
}