package repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import entity.Booking;
import entity.Freelancer;

public class Neo4JRepository {

    private final Driver driver;
    private final FreelancerRepository freelancerRepository;

    Neo4JRepository(Driver driver, FreelancerRepository freelancerRepository) {
        this.driver = driver;
        this.freelancerRepository = freelancerRepository;
    }

    public void addBooking(Booking booking) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                Freelancer freelancer = freelancerRepository.findById(booking.getFreelancerId())
                        .orElseThrow(() -> new RuntimeException("Freelancer not found"));

                String city = freelancer.getCity();
                if (city == null) {
                    city = "Nenurodyta";
                }

                LocalDate date = booking.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                Map<String, Object> params = new HashMap<>();
                params.put("bookingId", booking.getId());
                params.put("date", date);
                params.put("city", city);
                params.put("client", booking.getClientId());
                params.put("freelancer", booking.getFreelancerId());

                tx.run("CREATE (b:Booking {bookingId: $bookingId, date: $date, city: $city, client: $client, freelancer: $freelancer})",
                        params);
                return null;
            });
        }
    }
}