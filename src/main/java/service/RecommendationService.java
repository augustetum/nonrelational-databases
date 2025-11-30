package service;

import java.util.List;

import org.springframework.stereotype.Service;

import dto.FreelancerDetailsDto;
import repository.FreelancerRepository;
import repository.RecommendationRepository;

@Service
public class RecommendationService {
    private RecommendationRepository recommendationRepository;
    private FreelancerRepository freelancerRepository;

    public RecommendationService(RecommendationRepository recommendationRepository, FreelancerRepository freelancerRepository) {
        this.recommendationRepository = recommendationRepository;
        this.freelancerRepository = freelancerRepository;
    }

    public List<FreelancerDetailsDto> getRecommendationsByWorkfieldCategory(String clientId) {
        List<String> freelancerIds = recommendationRepository.getByWorkfieldCategory(clientId, 5);
        return freelancerRepository.getDetails(freelancerIds);
    }

    public List<FreelancerDetailsDto> getRecommendationsBySimilarClients(String clientId) {
        List<String> freelancerIds = recommendationRepository.getBySimilarClients(clientId, 5);
        return freelancerRepository.getDetails(freelancerIds);
    }
}
