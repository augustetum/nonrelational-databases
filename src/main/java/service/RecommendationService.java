package service;

import java.util.ArrayList;
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

    public List<FreelancerDetailsDto> getRecommendationsBySkill(String clientId) {
        // List<String> freelancerIds = recommendationRepository.getBySkill(clientId);
        
        List<String> freelancerIds = new ArrayList<>();
        freelancerIds.add("68ee7ec61c6318f81772862d");
        freelancerIds.add("68ee7f271c6318f81772862e");
        freelancerIds.add("68ee7f541c6318f81772862f");

        return freelancerRepository.getDetails(freelancerIds);
    }
}
