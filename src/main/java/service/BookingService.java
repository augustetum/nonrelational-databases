package service;

import entity.Booking;
import repository.BookingsRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingService {
    private final BookingsRepository repository;

    public BookingService(BookingsRepository repository) {
        this.repository = repository;
    }

    public List<Booking> getAllBookings() {
        return repository.getAllBookings();
    }
}