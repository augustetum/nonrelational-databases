package service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dto.FreelancerDetailsDto;
import dto.LeaderboardDetailsDto;
import redis.clients.jedis.Jedis;
import repository.FreelancerRepository;
import repository.LeaderboardRepository;
import repository.cache.FreelancerCacheRepository;
import repository.cache.LeaderboardCacheRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final LeaderboardRepository leaderboardRepository;

    private final FreelancerCacheRepository freelancerCacheRepository;
    private final LeaderboardCacheRepository leaderboardCacheRepository;   
    
    private final BookingService bookingService;

    public FreelancerService(
        FreelancerRepository freelancerRepository, 
        FreelancerCacheRepository freelancerCacheRepository, 
        LeaderboardRepository leaderboardRepository,
        LeaderboardCacheRepository leaderboardCacheRepository, 
        BookingService bookingService
    ) {
        this.freelancerRepository = freelancerRepository;
        this.leaderboardRepository = leaderboardRepository;
        
        this.freelancerCacheRepository = freelancerCacheRepository;
        this.leaderboardCacheRepository = leaderboardCacheRepository;

        this.bookingService = bookingService;
    }

    public Optional<FreelancerDetailsDto> getFreelancerDetails(String userId) {
        try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
            FreelancerDetailsDto cachedData = freelancerCacheRepository.getFreelancerDetails(userId, jedisConn);
            if (cachedData != null) {
                System.out.println("Freelancer cache hit");
                return Optional.of(cachedData);
            }

            System.out.println("Freelancer db hit");
            Optional<FreelancerDetailsDto> maybeDetailsDto = freelancerRepository.getDetails(userId);

            if (maybeDetailsDto.isPresent()) {
                FreelancerDetailsDto dto = maybeDetailsDto.get();
                freelancerCacheRepository.setFreelancerDetails(dto, jedisConn);
            }

            return maybeDetailsDto;
        }
    }
    
    public List<LeaderboardDetailsDto> getRatingLeaderboard(int limit, int skip) {
        List<LeaderboardDetailsDto> leaderboardDetails;

        // try to retrieve from cache
        try (Jedis jedisConn = leaderboardCacheRepository.getJedisConnection()) {
            leaderboardDetails = leaderboardCacheRepository.getAverageRatingLeaderboard(limit, skip, jedisConn);
        }

        if (leaderboardDetails != null) {
            return leaderboardDetails;
        }

        // cache miss logic
        leaderboardDetails = leaderboardRepository.getRatingLeaderboard();

        try (Jedis jedisConn = leaderboardCacheRepository.getJedisConnection()) {
            leaderboardCacheRepository.setAverageRatingLeaderboard(leaderboardDetails, jedisConn);
        }

        // return only requested items
        leaderboardDetails = leaderboardDetails.stream()
            .skip(skip)
            .limit(limit)
            .collect(Collectors.toList());
        
        return leaderboardDetails;
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
