package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dto.FreelancerDetailsDto;
import repository.FreelancerCacheRepository;
import repository.FreelancerRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final FreelancerCacheRepository freelancerCacheRepository;
    
    private final BookingService bookingService;

    public FreelancerService(FreelancerRepository freelancerRepository, FreelancerCacheRepository freelancerCacheRepository, BookingService bookingService) {
        this.freelancerRepository = freelancerRepository;
        this.freelancerCacheRepository = freelancerCacheRepository;
        this.bookingService = bookingService;
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

        List<FreelancerDetailsDto> freelancerDetailsList;
        List<String> cachedIds = freelancerCacheRepository.getLeaderboardFreelancerIds(sortBy, limit, skip);
        
        if (cachedIds != null) {
            freelancerDetailsList = new ArrayList<FreelancerDetailsDto>();

            for (String id : cachedIds) {
                Optional<FreelancerDetailsDto> maybeDetails = getFreelancerDetails(id);
                FreelancerDetailsDto dto = maybeDetails.get();
                freelancerDetailsList.add(dto);
            }

            return freelancerDetailsList;
        }

        freelancerDetailsList = freelancerRepository.getLeaderboard(sortBy, limit, skip);
        if (freelancerDetailsList == null) {
            return null;
        }
        
        List<String> freelancerIds = freelancerDetailsList.stream().map(FreelancerDetailsDto::getId).collect(Collectors.toList());
        freelancerCacheRepository.setLeaderboardFreelancerIds(sortBy, limit, skip, freelancerIds);

        return freelancerDetailsList;
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
