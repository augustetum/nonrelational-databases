package repository;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import config.MongoDbContext;
import entity.Booking;
import entity.BookingStatus;

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

    public List<Booking> getByClientId(String clientId) {
        List<Booking> allBookings = getAllBookings();
        return allBookings.stream()
                .filter(booking -> booking.getClientId().equals(clientId))
                .toList();
    }

    public List<Booking> getByFreelancerId(String freelancerId) {
        List<Booking> allBookings = getAllBookings();
        List<Booking> freelancerBookings = allBookings.stream()
                .filter(booking -> booking.getFreelancerId().equals(freelancerId))
                .toList();
        return freelancerBookings;
    }

    public Booking getById(String bookingId) {
        List<Booking> allBookings = getAllBookings();
        return allBookings.stream()
                .filter(booking -> booking.getId().equals(bookingId))
                .findFirst().orElse(null);
    }

    public void add(Booking booking) {

        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }

        Document bookingDocument = bookingToDocument(booking);
        dbContext.bookings.insertOne(bookingDocument);
    }

    public void update(String bookingId, Booking updatedBooking) {
        Bson filter = Filters.eq("_id", bookingId);
        Bson updates = Updates.combine(
                Updates.set("time", updatedBooking.getTime()),
                Updates.set("address", updatedBooking.getAddress()),
                Updates.set("details", updatedBooking.getDetails()));
        dbContext.bookings.updateOne(filter, updates);
    }

    public void updateStatus(String bookingId, BookingStatus status) {
        Bson filter = Filters.eq("_id", bookingId);
        Bson update = Updates.set("status", status.name());
        dbContext.bookings.updateOne(filter, update);
    }

    public void delete(String bookingId) {
        Bson filter = Filters.eq("_id", bookingId);
        dbContext.bookings.deleteOne(filter);
    }

    public Booking documentToBooking(Document document) {
        Booking booking = new Booking();

        booking.setId(document.getString("_id"));
        booking.setTime(document.getDate("time"));
        booking.setAddress(document.getString("address"));
        booking.setWorkfieldId(document.getString("workfieldId"));
        booking.setDetails(document.getString("details"));
        booking.setClientId(document.getString("clientId"));
        booking.setFreelancerId(document.getString("freelancerId"));

        String statusStr = document.getString("status");
        booking.setStatus(statusStr != null ? BookingStatus.valueOf(statusStr) : BookingStatus.PENDING);

        return booking;
    }

    public Document bookingToDocument(Booking booking) {
        Document document = new Document();
        document.append("_id", booking.getId());
        document.append("time", booking.getTime());
        document.append("address", booking.getAddress());
        document.append("workfieldId", booking.getWorkfieldId());
        document.append("details", booking.getDetails());
        document.append("clientId", booking.getClientId());
        document.append("freelancerId", booking.getFreelancerId());
        document.append("status", booking.getStatus().name());

        return document;
    }
}
