package service;

import org.springframework.security.crypto.password.PasswordEncoder;
import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import entity.Client;
import repository.ClientRepository;
import repository.Neo4JRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EventLogService eventLogService;
    private final Neo4JRepository neo4jRepository;

    public ClientAuthService(
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EventLogService eventLogService,
            Neo4JRepository neo4jRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.eventLogService = eventLogService;
        this.neo4jRepository = neo4jRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            eventLogService.logEvent("CLIENT", null, "CLIENT_REGISTER", "FAILURE", null,
                    "EMAIL ALREADY REGISTERED");
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        Client client = Client.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .city(request.getCity())
                .phoneNumber(request.getPhoneNumber())
                .build();

        clientRepository.add(client);
        neo4jRepository.addClient(client);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(new CustomClientDetails(client));

        return new AuthResponse(jwtToken, client.getEmail());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var client = clientRepository.findByEmail(request.getEmail());
        if (!client.isPresent()) {
            eventLogService.logEvent("CLIENT", null, "CLIENT_LOGIN", "FAILURE", null,
                    "USER NOT FOUND");
            throw new RuntimeException("User not found");
        }

        var presentClient = client.get();

        var jwtToken = jwtService.generateToken(new CustomClientDetails(presentClient));

        return new AuthResponse(jwtToken, presentClient.getEmail());
    }
}