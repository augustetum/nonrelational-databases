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
import util.IdentifierGenerator;

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

    public List<Booking> getByClientId(String clientId){
        List<Booking> allBookings = getAllBookings();
        return allBookings.stream()
                    .filter(booking -> booking.getClientId().equals(clientId))
                    .toList();
    }

    public Booking getById(String bookingId){
        List<Booking> allBookings = getAllBookings();
        return allBookings.stream()
                    .filter(booking -> booking.getId().equals(bookingId))
                    .findFirst().orElse(null);
    }

    public void add(Booking booking){
        String bookingId = IdentifierGenerator.generateId();
        booking.setId(bookingId);

        Document bookingDocument = bookingToDocument(booking);
        dbContext.bookings.insertOne(bookingDocument);
    }

    public void update(String bookingId, Booking updatedBooking){
        Bson filter = Filters.eq("_id", bookingId);
        Bson updates = Updates.combine(
                        Updates.set("time", updatedBooking.getTime()),
                        Updates.set("address", updatedBooking.getAddress()),
                        Updates.set("details", updatedBooking.getDetails())
        );
        dbContext.bookings.updateOne(filter, updates);
    }

    public void delete(String bookingId){
        Bson filter = Filters.eq("_id", bookingId);
        dbContext.bookings.deleteOne(filter);
    }

    public Booking documentToBooking(Document document) {
        Booking booking = new Booking();

        booking.setId(document.getString("_id"));
        booking.setTime(document.getDate("time"));
        booking.setAddress(document.getString("address"));
        booking.setDetails(document.getString("details"));
        booking.setClientId(document.getString("clientId"));
        booking.setFreelancerId(document.getString("freelancerId"));

        return booking;
    }

    public Document bookingToDocument(Booking booking){
        Document document = new Document();
        document.append("_id", booking.getId());
        document.append("time", booking.getTime());
        document.append("address", booking.getAddress());
        document.append("details", booking.getDetails());
        document.append("clientId", booking.getClientId());
        document.append("freelancerId", booking.getFreelancerId());

        return document;
    }
}
