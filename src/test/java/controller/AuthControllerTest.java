package controller;

import dto.AuthRequest;
import dto.AuthResponse;
import dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import service.ClientAuthService;
import service.FreelancerAuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private ClientAuthService clientAuthService;

    @Mock
    private FreelancerAuthService freelancerAuthService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_Client_ReturnsAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        AuthResponse expectedResponse = new AuthResponse("token123", "test@example.com");
        when(clientAuthService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(request, "client");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(clientAuthService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void register_Freelancer_ReturnsAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        AuthResponse expectedResponse = new AuthResponse("token456", "test@example.com");
        when(freelancerAuthService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(request, "freelancer");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(freelancerAuthService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void login_Client_ReturnsAuthResponse() {
        // Arrange
        AuthRequest request = new AuthRequest();
        AuthResponse expectedResponse = new AuthResponse("token789", "test@example.com");
        when(clientAuthService.authenticate(any(AuthRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request, "client");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(clientAuthService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_Freelancer_ReturnsAuthResponse() {
        // Arrange
        AuthRequest request = new AuthRequest();
        AuthResponse expectedResponse = new AuthResponse("token012", "test@example.com");
        when(freelancerAuthService.authenticate(any(AuthRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request, "freelancer");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(freelancerAuthService, times(1)).authenticate(any(AuthRequest.class));
    }
}
