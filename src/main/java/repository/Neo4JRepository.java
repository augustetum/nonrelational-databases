package repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Repository;

import entity.Booking;
import entity.Client;

@Repository
public class Neo4JRepository {

    private final Driver driver;

    Neo4JRepository(Driver driver) {
        this.driver = driver;
    }

    public void addBooking(Booking booking) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {

                LocalDate date = booking.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                Map<String, Object> params = new HashMap<>();
                params.put("workfieldId", booking.getWorkfieldId());
                params.put("bookingId", booking.getId());
                params.put("date", date);
                params.put("clientId", booking.getClientId());

                tx.run("CREATE (w:Workfield {workfieldId: $workfieldId})<-[:REQUIRES]-(b:Booking {bookingId: $bookingId, date: $date})<-[:CREATED]-(c:Client {clientId: $clientId})",
                        params);
                return null;
            });
        }
    }

    public void addClient(Client client) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {

                Map<String, Object> params = new HashMap<>();
                params.put("clientId", client.getId());

                tx.run("CREATE (c:Client {clientId: $clientId})",
                        params);
                return null;
            });
        }
    }
}