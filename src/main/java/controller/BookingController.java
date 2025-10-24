package controller;

import entity.Booking;
import service.BookingPermissionService;
import service.BookingService;
import service.BookingValidationService;
import service.CustomClientDetails;
import service.CustomFreelancerDetails;
import util.IdentifierGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

        if (!isClient)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only clients can create bookings.");

        // check if permissions are okay
        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();
        String freelancerId = bookingRequest.getFreelancerId();

        PermissionCheckResultDto permissionResult = permissionService.canCreateBooking(bookingRequest);
        if (permissionResult.isDenied())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        // if everything is intact, create a booking
        Booking booking = new Booking();
        booking.setId(IdentifierGenerator.generateId());
        booking.setTime(bookingRequest.getTime());
        booking.setAddress(bookingRequest.getAddress());
        booking.setDetails(bookingRequest.getDetails());
        booking.setClientId(clientId);
        booking.setFreelancerId(freelancerId);

        // check if there are no null or invalid fields
        ValidationResultDto validationResult = validationService.validate(booking);

        // create reservation and give the user a reservation key
        bookingService.createReservation(booking);
        if (validationResult.isInvalid())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);

        Long remainingTime = bookingService.getRemainingTime(booking.getId());
        return ResponseEntity.ok(Map.of(
                "reservation", booking,
                "remainingSeconds", remainingTime));
    }

    // confirm booking using the booking id that was provided when creating it
    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<?> confirmBooking(@PathVariable String bookingId) {
        try {
            bookingService.confirmBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking confirmed"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> updateBooking(@PathVariable String bookingId,
            @RequestBody EditBookingRequestDto updatedBooking, Authentication authentication) {
        // check if user can edit the provided booking
        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId();

        PermissionCheckResultDto permissionResult = permissionService.canUpdateBooking(userId, bookingId,
                updatedBooking);
        if (permissionResult.isDenied())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        // create the updated booking
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setTime(updatedBooking.getTime());
        booking.setAddress(updatedBooking.getAddress());
        booking.setDetails(updatedBooking.getDetails());
        booking.setClientId(userId);
        booking.setFreelancerId(bookingService.getById(bookingId).getFreelancerId());

        // validate the updated booking
        ValidationResultDto validationResult = validationService.validate(booking);
        if (validationResult.isInvalid())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);

        // update the booking in the database
        bookingService.updateBooking(bookingId, booking);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
        bookingService.cancelReservation(reservationId);
        return ResponseEntity.ok(Map.of("message", "Reservation cancelled"));
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

        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}