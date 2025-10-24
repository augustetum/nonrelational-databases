package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.FreelancerDetailsDto;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;
import repository.FreelancerRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final BookingService bookingService;

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public FreelancerService(FreelancerRepository freelancerRepository, BookingService bookingService, JedisPool jedisPool, ObjectMapper objectMapper) {
        this.freelancerRepository = freelancerRepository;
        this.bookingService = bookingService;

        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    public Optional<FreelancerDetailsDto> getFreelancerDetails(String userId) {
        Optional<FreelancerDetailsDto> maybeDetailsDto;
        String freelancerDataKey = String.format("freelancers:%s", userId);

        try (Jedis jedis = jedisPool.getResource()) {
            String cachedData = jedis.get(freelancerDataKey);

            if (cachedData != null) {
                FreelancerDetailsDto freelancerDetailsDto = objectMapper.readValue(cachedData, FreelancerDetailsDto.class);
                maybeDetailsDto = Optional.of(freelancerDetailsDto);
            }
            else {
                maybeDetailsDto = freelancerRepository.getDetails(userId);
            }

            return maybeDetailsDto;
        }
        catch (Exception ex) {
            System.out.println("Problem with redis");
        }

        return Optional.empty();
    }

    public List<LocalDate> getAvailableDates(String userId) {
        List<LocalDate> bookedDates = bookingService.getBookedDates(userId);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(1);
        List<LocalDate> freeDates = startDate.datesUntil(endDate)
                .collect(Collectors.toList());
        freeDates.removeAll(bookedDates);
        return freeDates;
    }
    
    public List<FreelancerDetailsDto> getLeaderboard(String sortBy, int limit, int skip) {
        if (!sortBy.equals("averageRating") && !sortBy.equals("jobsCompleted")) {
            sortBy = "averageRating";
        }
        
        String leaderboardKey = String.format("leaderboard:%s:%d:%d", sortBy, limit, skip);

        try (Jedis jedis = jedisPool.getResource()) {
            List<FreelancerDetailsDto> freelancerDetailsList;

            if (jedis.exists(leaderboardKey)) {
                List<String> cachedIds = jedis.lrange(leaderboardKey, 0, -1);
                freelancerDetailsList = new ArrayList<FreelancerDetailsDto>();
                
                for (String id : cachedIds) {
                    Optional<FreelancerDetailsDto> maybeDetails = getFreelancerDetails(id);
                    
                    if (!maybeDetails.isPresent()) {
                        throw new Exception("Couldn't get freelancer from cache or database");
                    }
                    
                    freelancerDetailsList.add(maybeDetails.get());
                }

                return freelancerDetailsList;
            }

            // cache miss
            freelancerDetailsList = freelancerRepository.getLeaderboard(sortBy, limit, skip);
            List<String> freelancerIds = freelancerDetailsList.stream().map(FreelancerDetailsDto::getId).collect(Collectors.toList());

            if (freelancerIds != null && !freelancerIds.isEmpty()) {
                jedis.del(leaderboardKey);
                jedis.rpush(leaderboardKey, freelancerIds.toArray(new String[0]));
                jedis.expire(leaderboardKey, 300);
            }

            return freelancerDetailsList;
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to retrieve freelancer details", ex);
        }
    }
}
