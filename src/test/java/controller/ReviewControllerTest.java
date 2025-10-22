package controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import dto.AddReviewRequestDto;
import dto.EditReviewRequestDto;
import dto.PermissionCheckResultDto;
import dto.RemoveReviewRequestDto;
import dto.ValidationResultDto;
import entity.Review;
import entity.ReviewId;
import service.CustomClientDetails;
import service.CustomFreelancerDetails;
import service.ReviewPermissionService;
import service.ReviewService;
import service.ReviewValidationService;

class ReviewControllerTest {

    @Mock
    private ReviewPermissionService permissionService;

    @Mock
    private ReviewValidationService validationService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private Authentication clientAuth;
    private Authentication freelancerAuth;
    private CustomClientDetails clientDetails;
    private CustomFreelancerDetails freelancerDetails;
    private Review testReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        clientDetails = mock(CustomClientDetails.class);
        entity.Client client = new entity.Client();
        client.setId("client123");
        when(clientDetails.getUser()).thenReturn(client);
        clientAuth = new UsernamePasswordAuthenticationToken(
            clientDetails, 
            "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );

        freelancerDetails = mock(CustomFreelancerDetails.class);
        entity.Freelancer freelancer = new entity.Freelancer();
        freelancer.setId("freelancer123");
        when(freelancerDetails.getUser()).thenReturn(freelancer);
        freelancerAuth = new UsernamePasswordAuthenticationToken(
            freelancerDetails,
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_FREELANCER"))
        );

        testReview = new Review();
        testReview.setId(new ReviewId("freelancer123"));
        testReview.setRating(new java.math.BigDecimal("5.0"));
        testReview.setDetails("Great service!");
        testReview.setAuthorId("client123");
        
        // Default permission service behavior
        when(permissionService.canAddReview(anyString(), anyString(), anyBoolean()))
            .thenReturn(PermissionCheckResultDto.valid());
        when(permissionService.canEditReview(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(PermissionCheckResultDto.valid());
        when(permissionService.canDeleteReview(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(PermissionCheckResultDto.valid());
        when(validationService.validate(any(Review.class), anyBoolean()))
            .thenReturn(ValidationResultDto.valid());
    }

    @Test
    void getByRevieweeId_ClientRequest_ReturnsReviews() {
        // Arrange
        String revieweeId = "freelancer123";
        when(reviewService.getByRevieweeId(eq(revieweeId), anyBoolean()))
            .thenReturn(Collections.singletonList(testReview));

        // Act
        ResponseEntity<List<Review>> response = reviewController.getByRevieweeId(clientAuth, revieweeId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testReview, response.getBody().get(0));
    }

    @Test
    void addReview_ValidRequest_ReturnsOk() {
        // Arrange
        AddReviewRequestDto requestDto = new AddReviewRequestDto();
        requestDto.setRevieweeId("freelancer123");
        requestDto.setRating(new java.math.BigDecimal("5.0"));
        requestDto.setDetails("Great service!");

        // Act
        ResponseEntity<?> response = reviewController.addReview(clientAuth, requestDto);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(reviewService, times(1)).addReview(any(Review.class), anyBoolean());
    }

    @Test
    void addReview_WithoutPermission_ReturnsBadRequest() {
        // Arrange
        AddReviewRequestDto requestDto = new AddReviewRequestDto();
        requestDto.setRevieweeId("freelancer123");
        requestDto.setRating(new java.math.BigDecimal("5.0"));
        requestDto.setDetails("Great service!");

        // Override default permission check to deny
        when(permissionService.canAddReview(anyString(), anyString(), anyBoolean()))
            .thenReturn(PermissionCheckResultDto.invalid("Not allowed to review this user"));

        // Act
        ResponseEntity<?> response = reviewController.addReview(clientAuth, requestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).addReview(any(Review.class), anyBoolean());
    }

    @Test
    void editReview_ValidRequest_ReturnsOk() {
        // Arrange
        EditReviewRequestDto requestDto = new EditReviewRequestDto();
        requestDto.setRevieweeId("freelancer123");
        requestDto.setReviewId("review123");
        requestDto.setRating(new java.math.BigDecimal("4.0"));
        requestDto.setDetails("Good service");
        
        // Setup test review for the service to return
        when(reviewService.getByRevieweeId(anyString(), anyBoolean()))
            .thenReturn(Collections.singletonList(testReview));

        // Act
        ResponseEntity<?> response = reviewController.editReview(clientAuth, requestDto);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(reviewService, times(1)).editReview(any(Review.class), anyBoolean());
    }

    @Test
    void removeReview_WithPermission_ReturnsOk() {
        // Arrange
        RemoveReviewRequestDto requestDto = new RemoveReviewRequestDto();
        requestDto.setRevieweeId("freelancer123");
        requestDto.setReviewId("review123");
        
        // Setup test review for the service to return
        when(reviewService.getByRevieweeId(anyString(), anyBoolean()))
            .thenReturn(Collections.singletonList(testReview));

        // Act
        ResponseEntity<?> response = reviewController.removeReview(clientAuth, requestDto);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(reviewService, times(1)).removeReview(any(ReviewId.class), anyBoolean());
    }

    @Test
    void removeReview_WithoutPermission_ReturnsBadRequest() {
        // Arrange
        RemoveReviewRequestDto requestDto = new RemoveReviewRequestDto();
        requestDto.setRevieweeId("freelancer123");
        requestDto.setReviewId("review123");
        
        // Setup test review for the service to return
        when(reviewService.getByRevieweeId(anyString(), anyBoolean()))
            .thenReturn(Collections.singletonList(testReview));
        
        // Override default permission check to deny
        when(permissionService.canDeleteReview(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(PermissionCheckResultDto.invalid("Not allowed to remove this review"));

        // Act
        ResponseEntity<?> response = reviewController.removeReview(clientAuth, requestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).removeReview(any(ReviewId.class), anyBoolean());
    }
    
}
