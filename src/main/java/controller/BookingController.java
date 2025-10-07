package controller;

import entity.Booking;
import service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<List<Booking>> getByClientId(@PathVariable String clientId){
        List<Booking> bookings = bookingService.getByClientId(clientId);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking){
        bookingService.createBooking(booking);
        return ResponseEntity.ok().build();
    }
}