package service;

import entity.Booking;
import repository.BookingRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public List<Booking> getAllBookings() {
        return repository.getAllBookings();
    }

    public List<Booking> getByClientId(String clientId){
        return repository.getByClientId(clientId);
    }

    public Booking getById(String bookingId){
        return repository.getById(bookingId);
    }

    public void createBooking(Booking booking){
        repository.add(booking);
    }

    public void updateBooking(String bookingId, Booking updatedBooking){
        repository.update(bookingId, updatedBooking);
    }

    public void deleteBooking(String bookingId){
        repository.delete(bookingId);
    }
}