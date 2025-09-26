package service;

import com.mongodb.client.*;
import entity.Booking;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("bookings");

            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Booking booking = documentToBooking(doc);
                    bookings.add(booking);
                }
            }
        }

        return bookings;
    }

    public Booking documentToBooking(Document doc) {
        Booking booking = new Booking();
        booking.setId(doc.getString("_id"));
        booking.setTime(doc.getString("time"));
        booking.setAddress(doc.getString("address"));
        booking.setDetails(doc.getString("details"));

        return booking;
    }
}