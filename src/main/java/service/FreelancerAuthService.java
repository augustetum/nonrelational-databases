package service;

import org.springframework.security.crypto.password.PasswordEncoder;
import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import entity.Freelancer;
import repository.FreelancerRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import repository.FreelancerRepository;

@Service
public class FreelancerAuthService {

    private final FreelancerRepository freelancerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public FreelancerAuthService(
            FreelancerRepository FreelancerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.freelancerRepository = FreelancerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (freelancerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        Freelancer freelancer = Freelancer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .city(request.getCity())
                .phoneNumber(request.getPhoneNumber())
                .build();

        freelancerRepository.add(freelancer);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(new CustomFreelancerDetails(freelancer));

        return new AuthResponse(jwtToken, freelancer.getEmail());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var freelancer = freelancerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(new CustomFreelancerDetails(freelancer));

        return new AuthResponse(jwtToken, freelancer.getEmail());
    }
}