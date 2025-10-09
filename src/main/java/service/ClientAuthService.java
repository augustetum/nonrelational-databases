package service;

import org.springframework.security.crypto.password.PasswordEncoder;
import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import entity.Client;
import repository.ClientRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ClientAuthService(
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setCity(request.getCity());
        client.setPhoneNumber(request.getPhoneNumber());

        clientRepository.add(client);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(new CustomUserDetails(client));

        return new AuthResponse(jwtToken, client.getEmail());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(new CustomUserDetails(client));

        return new AuthResponse(jwtToken, client.getEmail());
    }
}