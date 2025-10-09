package controller;

import entity.Booking;
import service.BookingPermissionService;
import service.BookingService;
import service.BookingValidationService;
import util.IdentifierGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.CreateBookingRequestDto;
import dto.EditBookingRequestDto;
import dto.PermissionCheckResultDto;
import dto.ValidationResultDto;

import java.util.List;

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
    public ResponseEntity<List<Booking>> getByClientId(@PathVariable String clientId){
        List<Booking> bookings = bookingService.getByClientId(clientId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getById(@PathVariable String bookingId){
        Booking booking = bookingService.getById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequestDto bookingRequest, String clientId, String freelancerId){
        //check if permissions are okay
        PermissionCheckResultDto permissionResult = permissionService.canCreateBooking(freelancerId, clientId, bookingRequest);
        if(permissionResult.isDenied()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        //if everything is intact, create a booking
        Booking booking = new Booking();
        booking.setId(IdentifierGenerator.generateId());
        booking.setTime(bookingRequest.getTime());
        booking.setAddress(bookingRequest.getAddress());
        booking.setDetails(bookingRequest.getDetails());
        booking.setClientId(clientId);
        booking.setFreelancerId(freelancerId);

         //check if there are no null or invalid fields
        ValidationResultDto validationResult = validationService.validate(booking);
        if(validationResult.isInvalid()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        
        //add new booking to database
        bookingService.createBooking(booking);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> updateBooking(@PathVariable String bookingId, String userId, @RequestBody EditBookingRequestDto updatedBooking){
        //check if user can edit the provided booking
        PermissionCheckResultDto permissionResult = permissionService.canUpdateBooking(userId, bookingId, updatedBooking);
        if(permissionResult.isDenied()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);

        //create the updated booking
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setTime(updatedBooking.getTime());
        booking.setAddress(updatedBooking.getAddress());
        booking.setDetails(updatedBooking.getDetails());
        booking.setClientId(userId);
        booking.setFreelancerId(bookingService.getById(bookingId).getFreelancerId());

        //validate the updated booking
        ValidationResultDto validationResult = validationService.validate(booking);
        if(validationResult.isInvalid()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);

        //update the booking in the database
        bookingService.updateBooking(bookingId, booking);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingId, String userId){
        PermissionCheckResultDto permissionResult = permissionService.canDeleteBooking(bookingId, userId);

        if(permissionResult.isDenied())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}