package controller;

import dto.CreateBookingRequestDto;
import dto.EditBookingRequestDto;
import dto.PermissionCheckResultDto;
import dto.ValidationResultDto;
import entity.Booking;
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
import entity.Client;
import service.*;

// No longer needed
import java.util.Calendar;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingValidationService validationService;

    @Mock
    private BookingPermissionService permissionService;

    @InjectMocks
    private BookingController bookingController;

    private Authentication clientAuth;
    private Authentication freelancerAuth;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        testBooking = new Booking();
        testBooking.setId("booking123");
        testBooking.setClientId("client123");
        testBooking.setFreelancerId("freelancer123");
        
        // Set up test time (tomorrow)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        testBooking.setTime(cal.getTime());
        testBooking.setAddress("123 Test St");
        testBooking.setDetails("Test booking details");

        // Setup authentication
        CustomClientDetails clientDetails = mock(CustomClientDetails.class);
        when(clientDetails.getUser()).thenReturn(new entity.Client());
        clientAuth = new UsernamePasswordAuthenticationToken(clientDetails, null, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        CustomFreelancerDetails freelancerDetails = mock(CustomFreelancerDetails.class);
        freelancerAuth = new UsernamePasswordAuthenticationToken(freelancerDetails, null, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_FREELANCER")));
    }

    @Test
    void getAllBookings_ReturnsListOfBookings() {
        // Arrange
        when(bookingService.getAllBookings()).thenReturn(Arrays.asList(testBooking));

        // Act
        ResponseEntity<List<Booking>> response = bookingController.getAllBookings();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(bookingService, times(1)).getAllBookings();
    }

    @Test
    void getByClientId_ReturnsClientBookings() {
        // Arrange
        String clientId = "client123";
        when(bookingService.getByClientId(clientId)).thenReturn(Collections.singletonList(testBooking));

        // Act
        ResponseEntity<List<Booking>> response = bookingController.getByClientId(clientId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(bookingService, times(1)).getByClientId(clientId);
    }

    @Test
    void getById_ReturnsBooking() {
        // Arrange
        String bookingId = "booking123";
        when(bookingService.getById(bookingId)).thenReturn(testBooking);

        // Act
        ResponseEntity<?> response = bookingController.getById(bookingId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(bookingId, ((Booking) response.getBody()).getId());
        verify(bookingService, times(1)).getById(bookingId);
    }

    @Test
    void createBooking_ValidRequest_ReturnsOk() {
        // Arrange
        CreateBookingRequestDto request = new CreateBookingRequestDto();
        request.setFreelancerId("freelancer123");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        request.setTime(cal.getTime());
        request.setAddress("123 Test St");
        request.setDetails("Test details");

        when(permissionService.canCreateBooking(any())).thenReturn(PermissionCheckResultDto.valid());
        when(validationService.validate(any())).thenReturn(ValidationResultDto.valid());

        // Act
        ResponseEntity<?> response = bookingController.createBooking(request, clientAuth);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(bookingService, times(1)).createBooking(any(Booking.class));
    }

    @Test
    void createBooking_NotClient_ReturnsForbidden() {
        // Arrange
        CreateBookingRequestDto request = new CreateBookingRequestDto();

        // Act
        ResponseEntity<?> response = bookingController.createBooking(request, freelancerAuth);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookingService, never()).createBooking(any());
    }

    @Test
    void updateBooking_ValidRequest_ReturnsOk() {
        // Arrange
        String bookingId = "booking123";
        EditBookingRequestDto updatedBooking = new EditBookingRequestDto();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        updatedBooking.setTime(cal.getTime());
        updatedBooking.setAddress("456 New St");
        updatedBooking.setDetails("Updated details");

        // Mock the client authentication
        Client client = new Client();
        client.setId("client123");
        CustomClientDetails clientDetails = new CustomClientDetails(client);
        Authentication auth = new UsernamePasswordAuthenticationToken(clientDetails, "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        when(permissionService.canUpdateBooking(anyString(), anyString(), any()))
            .thenReturn(PermissionCheckResultDto.valid());
        when(validationService.validate(any())).thenReturn(ValidationResultDto.valid());
        when(bookingService.getById(bookingId)).thenReturn(testBooking);

        // Act
        ResponseEntity<?> response = bookingController.updateBooking(bookingId, updatedBooking, auth);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(bookingService, times(1)).updateBooking(eq(bookingId), any(Booking.class));
    }

    @Test
    void deleteBooking_ClientWithPermission_ReturnsOk() {
        // Arrange
        String bookingId = "booking123";
        when(permissionService.canDeleteBooking(eq(bookingId), anyString()))
            .thenReturn(PermissionCheckResultDto.valid());

        // Mock the client authentication
        Client client = new Client();
        client.setId("client123");
        CustomClientDetails clientDetails = new CustomClientDetails(client);
        Authentication auth = new UsernamePasswordAuthenticationToken(clientDetails, "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        // Act
        ResponseEntity<?> response = bookingController.deleteBooking(bookingId, auth);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(bookingService, times(1)).deleteBooking(bookingId);
    }

    @Test
    void deleteBooking_WithoutPermission_ReturnsBadRequest() {
        // Arrange
        String bookingId = "booking123";
        when(permissionService.canDeleteBooking(eq(bookingId), anyString()))
            .thenReturn(PermissionCheckResultDto.invalid("Not authorized"));

        // Mock the client authentication
        Client client = new Client();
        client.setId("client123");
        CustomClientDetails clientDetails = new CustomClientDetails(client);
        Authentication auth = new UsernamePasswordAuthenticationToken(clientDetails, "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        // Act
        ResponseEntity<?> response = bookingController.deleteBooking(bookingId, auth);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        verify(bookingService, never()).deleteBooking(anyString());
    }
}
