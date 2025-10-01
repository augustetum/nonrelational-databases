package repository;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import config.MongoDbContext;
import entity.Booking;

@Repository
public class BookingRepository {
    private final MongoDbContext dbContext;

    public BookingRepository(MongoDbContext dbContext) {
        this.dbContext = dbContext;
    }

    public List<Booking> getAllBookings() {
        return dbContext.bookings.find()
            .into(new ArrayList<Document>())
            .stream()
            .map(this::documentToBooking)
            .toList();
    }

    public Booking documentToBooking(Document document) {
        Booking booking = new Booking();

        booking.setId(document.getString("_id"));
        booking.setTime(document.getDate("time"));
        booking.setAddress(document.getString("address"));
        booking.setDetails(document.getString("details"));

        return booking;
    }
}
