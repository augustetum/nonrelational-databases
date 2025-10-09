package service;

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
}
