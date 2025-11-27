package controller;

import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import entity.Client;
import entity.Freelancer;
import service.ClientAuthService;
import service.ClientService;
import service.EventLogService;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FreelancerAuthService;
import service.FreelancerService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientAuthService clientAuthService;
    private final FreelancerAuthService freelancerAuthService;
    private final EventLogService eventLogService;
    private final ClientService clientService;
    private final FreelancerService freelancerService;

    public AuthController(ClientAuthService clientAuthService, FreelancerAuthService freelancerAuthService,
            EventLogService eventLogService, ClientService clientService, FreelancerService freelancerService) {
        this.clientAuthService = clientAuthService;
        this.freelancerAuthService = freelancerAuthService;
        this.eventLogService = eventLogService;
        this.clientService = clientService;
        this.freelancerService = freelancerService;
    }

    @PostMapping("/register/{userType}")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, @PathVariable String userType) {
        if (userType.equals("client")) {
            AuthResponse authResponse = clientAuthService.register(request);
            Optional<Client> client = clientService.getByEmail(authResponse.getEmail());

            if (client.isPresent()) {
                Client user = client.get();
                eventLogService.logEvent("CLIENT", user.getId(), "CLIENT_REGISTER", "SUCCESS", user.getId(),
                        null);
            }
            return ResponseEntity.ok(authResponse);
        }

        else {
            AuthResponse authResponse = freelancerAuthService.register(request);
            Optional<Freelancer> freelancer = freelancerService.getByEmail(authResponse.getEmail());

            if (freelancer.isPresent()) {
                Freelancer user = freelancer.get();
                eventLogService.logEvent("FREELANCER", user.getId(), "FREELANCER_REGISTER", "SUCCESS", user.getId(),
                        null);
            }

            return ResponseEntity.ok(authResponse);
        }
    }

    @PostMapping("/login/{userType}")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, @PathVariable String userType) {
        if (userType.equals("client")) {
            AuthResponse authResponse = clientAuthService.authenticate(request);
            Client client = clientService.getByEmail(authResponse.getEmail()).get();
            eventLogService.logEvent("CLIENT", client.getId(), "CLIENT_LOGIN", "SUCCESS", client.getId(),
                    null);

            return ResponseEntity.ok(authResponse);
        } else {
            AuthResponse authResponse = freelancerAuthService.authenticate(request);
            Freelancer freelancer = freelancerService.getByEmail(authResponse.getEmail()).get();

            eventLogService.logEvent("FREELANCER", freelancer.getId(), "FREELANCER_LOGIN", "SUCCESS",
                    freelancer.getId(),
                    null);
            return ResponseEntity.ok(authResponse);
        }
    }
}