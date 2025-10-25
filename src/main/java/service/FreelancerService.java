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
import repository.FreelancerCacheRepository;
import repository.FreelancerRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final FreelancerCacheRepository freelancerCacheRepository;
    
    private final BookingService bookingService;

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public FreelancerService(FreelancerRepository freelancerRepository, FreelancerCacheRepository freelancerCacheRepository, BookingService bookingService, JedisPool jedisPool, ObjectMapper objectMapper) {
        this.freelancerRepository = freelancerRepository;
        this.freelancerCacheRepository = freelancerCacheRepository;
        this.bookingService = bookingService;

        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    public Optional<FreelancerDetailsDto> getFreelancerDetails(String userId) {
        FreelancerDetailsDto cachedData = freelancerCacheRepository.getFreelancerDetails(userId);
        if (cachedData != null) {
            return Optional.of(cachedData);
        }

        Optional<FreelancerDetailsDto> maybeDetailsDto = freelancerRepository.getDetails(userId);

        if (maybeDetailsDto.isPresent()) {
            FreelancerDetailsDto dto = maybeDetailsDto.get();
            freelancerCacheRepository.setFreelancerDetails(dto);
        }

        return maybeDetailsDto;
    }
    
    public List<FreelancerDetailsDto> getLeaderboard(String sortBy, int limit, int skip) {
        if (!sortBy.equals("averageRating") && !sortBy.equals("jobsCompleted")) {
            sortBy = "averageRating";
        }
        
        String leaderboardKey = String.format("leaderboard:%s:%d:%d", sortBy, limit, skip);
        try (Jedis jedis = jedisPool.getResource()) {
            List<FreelancerDetailsDto> freelancerDetailsList;

            if (jedis.exists(leaderboardKey)) {
                System.out.println("list from cache");

                freelancerDetailsList = new ArrayList<FreelancerDetailsDto>();
                List<String> cachedIds = jedis.lrange(leaderboardKey, 0, -1);

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

            if (freelancerIds != null) {
                System.out.println("list from db");

                jedis.del(leaderboardKey);
                jedis.rpush(leaderboardKey, freelancerIds.toArray(new String[0]));
                jedis.expire(leaderboardKey, 300); // TODO: delete later
            }

            return freelancerDetailsList;
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to retrieve freelancer leaderboard", ex);
        }
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
}
