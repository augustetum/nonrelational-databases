package service;

import entity.Booking;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import entity.BookingStatus;
import entity.Freelancer;
import repository.BookingRepository;
import repository.FreelancerRepository;
import repository.cache.FreelancerCacheRepository;

import org.neo4j.driver.Session;
import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository repository;
    private final FreelancerCacheRepository freelancerCacheRepository;
    private final FreelancerRepository freelancerRepository;
    private final EventLogService eventLogService;

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    private final Driver neo4jDriver;

    private static final int RESERVATION_TTL_SECONDS = 600;

    public BookingService(BookingRepository repository, FreelancerCacheRepository freelancerCacheRepository,
            JedisPool jedisPool, ObjectMapper objectMapper, EventLogService eventLogService, Driver neo4JDriver,
            FreelancerRepository freelancerRepository) {
        this.repository = repository;
        this.freelancerRepository = freelancerRepository;
        this.jedisPool = jedisPool;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.eventLogService = eventLogService;
        this.neo4jDriver = neo4JDriver;

        this.freelancerCacheRepository = freelancerCacheRepository;
    }

    public List<Booking> getAllBookings() {
        return repository.getAllBookings();
    }

    public List<Booking> getByClientId(String clientId) {
        return repository.getByClientId(clientId);
    }

    public Booking getById(String bookingId) {
        return repository.getById(bookingId);
    }

    public List<LocalDate> getBookedDates(String freelancerId) {
        List<Booking> bookings = repository.getByFreelancerId(freelancerId);
        List<LocalDate> bookedDates = bookings.stream()
                .map(booking -> booking.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .collect(Collectors.toList());
        return bookedDates;
    }

    public Booking createReservation(Booking booking) {
        try (Jedis jedis = jedisPool.getResource()) {
            String dateKey = buildDateKey(booking.getFreelancerId(), booking.getTime());
            if (jedis.exists(dateKey)) {
                eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CREATE", "FAILURE",
                        booking.getClientId(), "DATE ALREADY RESERVED");
                throw new IllegalStateException("This date is already reserved");
            }

            String reservationKey = buildReservationKey(booking.getId());
            String reservationJson = objectMapper.writeValueAsString(booking);
            System.out.println("set " + reservationKey);

            jedis.watch(dateKey);
            var transaction = jedis.multi();
            transaction.setex(reservationKey, RESERVATION_TTL_SECONDS, reservationJson);
            transaction.setex(dateKey, RESERVATION_TTL_SECONDS, booking.getClientId());
            transaction.exec();

            if (booking != null)
                return booking;
            return new Booking();
        } catch (Exception e) {
            eventLogService.logEvent("BOOKING", booking.getId(), "BOOKING_CREATE", "FAILURE",
                    booking.getClientId(), null);
            throw new RuntimeException("Failed to create reservation", e);
        }
    }

    public Booking getReservation(String bookingId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = buildReservationKey(bookingId);
            System.out.println("trying to get: " + key);
            String json = jedis.get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, Booking.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get reservation", e);
        }
    }

    public boolean isDateAvailable(String freelancerId, Date bookingDate) {
        try (Jedis jedis = jedisPool.getResource()) {
            String dateKey = buildDateKey(freelancerId, bookingDate);
            return !jedis.exists(dateKey);
        }
    }

    public void confirmBooking(String bookingId) {
        try (Jedis jedis = jedisPool.getResource()) {
            Booking booking = getReservation(bookingId);

            if (booking == null) {
                eventLogService.logEvent("BOOKING", bookingId, "BOOKING_CONFIRM", "FAILURE", "none",
                        "BOOKING NOT FOUND");
                throw new IllegalStateException("Reservation not found or expired");
            }

            String reservationKey = buildReservationKey(bookingId);
            String dateKey = buildDateKey(booking.getFreelancerId(),
                    booking.getTime());

            jedis.del(reservationKey, dateKey);
            try (Session session = neo4jDriver.session()) {
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
                    params.put("bookingId", bookingId);
                    params.put("date", date);
                    params.put("city", city);
                    params.put("client", booking.getClientId());
                    params.put("freelancer", booking.getFreelancerId());
                    params.put("status", booking.getStatus());

                    tx.run("CREATE (b:Booking {bookingId: $bookingId, date: $date, city: $city, client: $client, freelancer: $freelancer, status: $status})",
                            params);
                    return null;
                });
            }

            repository.add(booking);
        }

    }

    public void cancelReservation(String bookingId) {
        try (Jedis jedis = jedisPool.getResource()) {
            Booking reservation = getReservation(bookingId);

            if (reservation != null) {
                String reservationKey = buildReservationKey(bookingId);
                String dateKey = buildDateKey(reservation.getFreelancerId(),
                        reservation.getTime());
                jedis.del(reservationKey, dateKey);
            }
        }
    }

    public Long getRemainingTime(String bookingId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = buildReservationKey(bookingId);
            return jedis.ttl(key);
        }
    }

    private String buildReservationKey(String bookingId) {
        return "reservation:" + bookingId;
    }

    private String buildDateKey(String freelancerId, Date bookingDate) {
        return "date:" + freelancerId + ":" + bookingDate.toString();
    }

    public void updateBooking(String bookingId, Booking updatedBooking) {
        repository.update(bookingId, updatedBooking);
    }

    public void deleteBooking(String bookingId) {
        repository.delete(bookingId);
    }

    public void updateBookingStatus(String bookingId, BookingStatus status) {
        repository.updateStatus(bookingId, status);
        Booking booking = repository.getById(bookingId);

        if (status == BookingStatus.COMPLETED) {
            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(booking.getFreelancerId(), jedisConn);
            }
        }
    }
}