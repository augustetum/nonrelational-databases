package service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import dto.FreelancerDetailsDto;
import repository.FreelancerRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;
    private final BookingService bookingService;

    public FreelancerService(FreelancerRepository freelancerRepository, BookingService bookingService) {
        this.freelancerRepository = freelancerRepository;
        this.bookingService = bookingService;
    }

    public Optional<FreelancerDetailsDto> getFreelancerDetails(String userId) {
        Optional<FreelancerDetailsDto> maybeFreelancerDetails = freelancerRepository.getDetails(userId);
        return maybeFreelancerDetails;
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
