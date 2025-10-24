package service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import dto.FreelancerDetailsDto;
import repository.FreelancerRepository;

@Service
public class FreelancerService {
    private final FreelancerRepository freelancerRepository;

    public FreelancerService(FreelancerRepository freelancerRepository) {
        this.freelancerRepository = freelancerRepository;
    }

    public Optional<FreelancerDetailsDto> getFreelancerDetails(String userId) {
        Optional<FreelancerDetailsDto> maybeFreelancerDetails = freelancerRepository.getDetails(userId);
        return maybeFreelancerDetails;
    }

    public List<FreelancerDetailsDto> getLeaderboard(String sortBy, int limit, int skip) {
        // Validate sortBy parameter
        if (!sortBy.equals("averageRating") && !sortBy.equals("jobsCompleted")) {
            sortBy = "averageRating"; // default
        }
        return freelancerRepository.getLeaderboard(sortBy, limit, skip);
    }
}
