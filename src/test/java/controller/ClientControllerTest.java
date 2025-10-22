package controller;

import dto.ClientDetailsDto;
import dto.EditClientDetailsDto;
import dto.ValidationResultDto;
import entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import service.ClientService;
import service.ClientValidationService;
import service.CustomClientDetails;

import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private ClientValidationService validationService;

    @InjectMocks
    private ClientController clientController;

    private Authentication authentication;
    private Client testClient;
    private CustomClientDetails clientDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test client
        testClient = new Client();
        testClient.setId("client123");
        testClient.setEmail("test@example.com");
        testClient.setPassword("hashedpassword");
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setCity("Test City");
        testClient.setPhoneNumber(37061234567L);

        // Setup authentication
        clientDetails = mock(CustomClientDetails.class);
        when(clientDetails.getUser()).thenReturn(testClient);
        authentication = new UsernamePasswordAuthenticationToken(
            clientDetails, 
            null, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
    }

    @Test
    void getClientDetails_ValidClient_ReturnsClientDetails() {
        // Arrange
        String userId = "client123";
        ClientDetailsDto clientDetailsDto = new ClientDetailsDto();
        clientDetailsDto.setId(testClient.getId());
        clientDetailsDto.setFirstName(testClient.getFirstName());
        clientDetailsDto.setLastName(testClient.getLastName());
        clientDetailsDto.setEmail(testClient.getEmail());
        clientDetailsDto.setCity(testClient.getCity());
        clientDetailsDto.setPhoneNumber(testClient.getPhoneNumber());
        
        when(clientService.getClientDetails(userId)).thenReturn(Optional.of(clientDetailsDto));

        // Act
        ResponseEntity<?> response = clientController.getClientDetails(userId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Optional);
        assertTrue(((Optional<?>) response.getBody()).isPresent());
        assertEquals(clientDetailsDto, ((Optional<?>) response.getBody()).get());
        verify(clientService, times(1)).getClientDetails(userId);
    }

    @Test
    void getClientDetails_NonExistentClient_ReturnsNotFound() {
        // Arrange
        String userId = "nonExistentClient";
        when(clientService.getClientDetails(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = clientController.getClientDetails(userId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Optional);
        assertFalse(((Optional<?>) response.getBody()).isPresent());
    }

    @Test
    void editClientDetails_ValidData_ReturnsOk() {
        // Arrange
        EditClientDetailsDto editDto = new EditClientDetailsDto();
        editDto.setFirstName("Updated");
        editDto.setLastName("User");
        editDto.setCity("Updated City");
        editDto.setPhoneNumber(37069876543L);

        when(validationService.validate(any(Client.class))).thenReturn(ValidationResultDto.valid());

        // Act
        ResponseEntity<?> response = clientController.editClientDetails(authentication, editDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clientService, times(1)).editClientDetails(eq("client123"), any(Client.class));
    }

    @Test
    void editClientDetails_InvalidData_ReturnsBadRequest() {
        // Arrange
        EditClientDetailsDto editDto = new EditClientDetailsDto();
        editDto.setFirstName("");
        editDto.setLastName("");
        editDto.setCity("");

        ValidationResultDto validationResult = ValidationResultDto.invalid("Validation failed");
        when(validationService.validate(any(Client.class))).thenReturn(validationResult);

        // Act
        ResponseEntity<?> response = clientController.editClientDetails(authentication, editDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(validationResult, response.getBody());
        verify(clientService, never()).editClientDetails(anyString(), any(Client.class));
    }

    @Test
    void deleteClient_ValidClient_ReturnsOk() {
        // Act
        ResponseEntity<?> response = clientController.deleteClient(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clientService, times(1)).deleteClient("client123");
    }
}
