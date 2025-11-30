package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dto.FreelancerDetailsDto;
import service.CustomClientDetails;
import service.RecommendationService;

@Controller
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("/byWorkfieldCategory")
    public ResponseEntity<?> getRecommendationsByWorkfieldCategory(Authentication authentication) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } 
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Freelancers shouldn't be able to call this endpoint");
        }

        List<FreelancerDetailsDto> recommendations = recommendationService.getRecommendationsByWorkfieldCategory(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/bySimilarClients")
    public ResponseEntity<?> getRecommendationsBySimilarClients(Authentication authentication) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } 
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Freelancers shouldn't be able to call this endpoint");
        }

        List<FreelancerDetailsDto> recommendations = recommendationService.getRecommendationsBySimilarClients(userId);
        return ResponseEntity.ok(recommendations);
    }
}
