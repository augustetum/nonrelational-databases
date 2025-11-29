package controller;

import entity.Booking;
import entity.BookingStatus;
import service.BookingPermissionService;
import service.BookingService;
import service.BookingValidationService;
import service.CustomClientDetails;
import service.CustomFreelancerDetails;
import service.EventLogService;
import util.IdentifierGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dto.CreateBookingRequestDto;
import dto.EditBookingRequestDto;
import dto.PermissionCheckResultDto;
import dto.ValidationResultDto;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingValidationService validationService;

    @Autowired
    private BookingPermissionService permissionService;

    @Autowired
    private EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Booking>> getByClientId(@PathVariable String clientId) {
        List<Booking> bookings = bookingService.getByClientId(clientId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getById(@PathVariable String bookingId) {
        Booking booking = bookingService.getById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String freelancerId,
            @RequestParam String bookingDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Vilnius"));
            Date date = formatter.parse(bookingDate);
            boolean available = bookingService.isDateAvailable(freelancerId, date);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> createReservation(@RequestBody CreateBookingRequestDto bookingRequest,
            Authentication authentication) {

        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        if (!isClient) {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            eventLogService.logEvent("BOOKING", null, "BOOKING_CREATE", "FAILURE",
                    userDetails.getUser().getId(),
                    "IS NOT CLIENT");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only clients can create bookings.");
        }

        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();
        String freelancerId = bookingRequest.getFreelancerId();

        PermissionCheckResultDto permissionResult = permissionService.canCreateBooking(bookingRequest, clientId);
        if (permissionResult.isDenied())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        Booking booking = new Booking();
        booking.setId(IdentifierGenerator.generateId());
        booking.setTime(bookingRequest.getTime());
        booking.setAddress(bookingRequest.getAddress());
        booking.setWorkfieldId(bookingRequest.getWorkfieldId());
        booking.setDetails(bookingRequest.getDetails());
        booking.setClientId(clientId);
        booking.setFreelancerId(freelancerId);

        ValidationResultDto validationResult = validationService.validate(booking);

        if (validationResult.isInvalid()) {
            eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CREATE", "FAILURE", clientId,
                    "INVALID BOOKING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        bookingService.createReservation(booking);
        eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CREATE", "SUCCESS", clientId,
                "BOOKING_TIME: " + booking.getTime() + ", BOOKING_ADDRESS: " + booking.getAddress() + ", DETAILS: "
                        + booking.getDetails() + ", FREELANCER_ID: " + freelancerId);

        Long remainingTime = bookingService.getRemainingTime(booking.getId());
        return ResponseEntity.ok(Map.of(
                "reservation", booking,
                "remainingSeconds", remainingTime));
    }

    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<?> confirmBooking(@PathVariable String bookingId) {
        try {
            Booking booking = bookingService.getReservation(bookingId);
            bookingService.confirmBooking(bookingId);
            eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CONFIRM", "SUCCESS", booking.getClientId(),
                    "BOOKING_TIME: " + booking.getTime() + ", BOOKING_ADDRESS: " + booking.getAddress() + ", DETAILS: "
                            + booking.getDetails() + ", FREELANCER_ID: " + booking.getFreelancerId());
            return ResponseEntity.ok(Map.of("message", "Booking confirmed"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> updateBooking(@PathVariable String bookingId,
            @RequestBody EditBookingRequestDto updatedBooking, Authentication authentication) {

        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId();

        PermissionCheckResultDto permissionResult = permissionService.canUpdateBooking(userId, bookingId,
                updatedBooking);
        if (permissionResult.isDenied())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setTime(updatedBooking.getTime());
        booking.setAddress(updatedBooking.getAddress());
        booking.setDetails(updatedBooking.getDetails());
        booking.setClientId(userId);
        booking.setFreelancerId(bookingService.getById(bookingId).getFreelancerId());

        ValidationResultDto validationResult = validationService.validate(booking);
        if (validationResult.isInvalid()) {
            eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_EDIT", "FAILURE", userId,
                    "BOOKING INVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        bookingService.updateBooking(bookingId, booking);
        eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_EDIT", "SUCCESS", userId,
                "BOOKING_TIME: " + booking.getTime() + ", BOOKING_ADDRESS: " + booking.getAddress() + ", DETAILS: "
                        + booking.getDetails() + ", FREELANCER_ID: " + booking.getFreelancerId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
        Booking booking = bookingService.getReservation(reservationId);
        bookingService.cancelReservation(reservationId);
        eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CONFIRM", "FAILURE", booking.getClientId(),
                "RESERVATION CANCELLED");
        return ResponseEntity.ok(Map.of("message", "Reservation cancelled"));
    }

    @PatchMapping("/{bookingId}/complete")
    public ResponseEntity<?> markBookingAsCompleted(@PathVariable String bookingId, Authentication authentication) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        if (!isClient) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only clients can mark bookings as completed.");
        }

        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();

        // Get the booking
        Booking booking = bookingService.getById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        }

        // Verify the client owns this booking
        if (!booking.getClientId().equals(clientId)) {
            eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_COMPLETE", "SUCCESS", clientId,
                    "BOOKING DOES NOT BELONG TO CLIENT");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only mark your own bookings as completed.");
        }

        // Update the booking status
        bookingService.updateBookingStatus(bookingId, BookingStatus.COMPLETED);
        eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_COMPLETE", "SUCCESS", clientId,
                "BOOKING_TIME: " + booking.getTime() + ", BOOKING_ADDRESS: " + booking.getAddress() + ", DETAILS: "
                        + booking.getDetails() + ", FREELANCER_ID: " + booking.getFreelancerId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingId, Authentication authentication) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } else {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        }

        PermissionCheckResultDto permissionResult = permissionService.canDeleteBooking(bookingId, userId);

        if (permissionResult.isDenied()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        Booking booking = bookingService.getById(bookingId);
        bookingService.deleteBooking(bookingId);
        eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_DELETE", "SUCCESS", booking.getClientId(), null);
        return ResponseEntity.ok().build();
    }
}