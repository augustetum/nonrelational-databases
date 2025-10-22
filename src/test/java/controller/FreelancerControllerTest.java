package controller;

import dto.FreelancerDetailsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import service.FreelancerService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
// Removed unused import
import static org.mockito.Mockito.when;

class FreelancerControllerTest {

    @Mock
    private FreelancerService freelancerService;

    @InjectMocks
    private FreelancerController freelancerController;

    private FreelancerDetailsDto testFreelancerDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        testFreelancerDetails = new FreelancerDetailsDto();
        testFreelancerDetails.setId("freelancer123");
        testFreelancerDetails.setFirstName("John");
        testFreelancerDetails.setLastName("Doe");
        testFreelancerDetails.setPhoneNumber(37061234567L);
        testFreelancerDetails.setCity("Test City");
        testFreelancerDetails.setRating(new java.math.BigDecimal("5.0"));
    }

    @Test
    void getFreelancerDetails_ExistingFreelancer_ReturnsFreelancerDetails() {
        // Arrange
        String userId = "freelancer123";
        when(freelancerService.getFreelancerDetails(userId))
            .thenReturn(Optional.of(testFreelancerDetails));

        // Act
        ResponseEntity<?> response = freelancerController.getFreelancerDetails(userId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Optional);
        assertTrue(((Optional<?>) response.getBody()).isPresent());
        assertEquals(testFreelancerDetails, ((Optional<?>) response.getBody()).get());
    }

    @Test
    void getFreelancerDetails_NonExistentFreelancer_ReturnsEmptyOptional() {
        // Arrange
        String nonExistentId = "nonExistentId";
        when(freelancerService.getFreelancerDetails(nonExistentId))
            .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = freelancerController.getFreelancerDetails(nonExistentId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() instanceof Optional && ((Optional<?>) response.getBody()).isEmpty());
    }

    @Test
    void getFreelancerDetails_NullUserId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            freelancerController.getFreelancerDetails(null);
        });
    }

    @Test
    void getFreelancerDetails_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        String userId = "errorId";
        when(freelancerService.getFreelancerDetails(userId))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            freelancerController.getFreelancerDetails(userId);
        });
    }
}
