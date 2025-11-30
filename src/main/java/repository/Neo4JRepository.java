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
import entity.Freelancer;

@Repository
public class Neo4JRepository {

    private final Driver driver;

    Neo4JRepository(Driver driver) {
        this.driver = driver;
    }

    public void addBooking(Booking booking) {
        String statement = """
                    match (client:Client { clientId:$clientId })
                    match (category:WorkfieldCategory { id:$workfieldId })
                    with client, category
                    create (client) -[:CREATED]-> (:Booking { bookingId:$bookingId, date:$date}) -[:REQUIRES]-> (category)
                """;

        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("workfieldId", booking.getWorkfieldId());
        params.put("bookingId", booking.getId());

        LocalDate date = booking.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        params.put("date", date);
        params.put("clientId", booking.getClientId());

        // execute statement
        try (Session session = driver.session()) {
            session.run(statement, params);
        }
    }

    public void addClient(Client client) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {

                Map<String, Object> params = new HashMap<>();
                params.put("clientId", client.getId());
                params.put("city", client.getCity());

                tx.run("CREATE (c:Client {clientId: $clientId, city: $city})",
                        params);
                return null;
            });
        }
    }

    public void addFreelancer(Freelancer freelancer) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {

                Map<String, Object> params = new HashMap<>();
                params.put("freelancerId", freelancer.getId());
                params.put("city", freelancer.getCity());

                tx.run("CREATE (f:Freelancer {freelancerId: $freelancerId, city: $city})",
                        params);
                return null;
            });
        }
    }

    public void addWorkfieldForFreelancer(String freelancerId, String workfieldId) {
        String statement = """
                    match (freelancer:Freelancer { freelancerId:$freelancerId })
                    match (category:WorkfieldCategory { id:$workfieldId })
                    with freelancer, category
                    create (freelancer) -[:CAN_DO]-> (category)
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("workfieldId", workfieldId);
        params.put("freelancerId", freelancerId);

        try (Session session = driver.session()) {
            session.run(statement, params);
        }
    }
}