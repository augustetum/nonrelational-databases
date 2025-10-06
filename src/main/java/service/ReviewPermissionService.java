package service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import dto.AddReviewDto;
import dto.AuthorizationResultDto;
import entity.Review;
import repository.ClientReviewRepository;
import repository.FreelancerReviewRepository;

@Service
public class ReviewPermissionService {
    private final ClientReviewRepository clientReviewRepository;
    private final FreelancerReviewRepository freelancerReviewRepository;

    public ReviewPermissionService(ClientReviewRepository clientReviewRepository, FreelancerReviewRepository freelancerReviewRepository) {
        this.clientReviewRepository = clientReviewRepository;
        this.freelancerReviewRepository = freelancerReviewRepository;
    }

    public AuthorizationResultDto canAddReview(AddReviewDto dto) {
        AuthorizationResultDto result = new AuthorizationResultDto();

        if (dto.revieweeId == dto.authorId) {
            result.setMessage("Users can't write reviews to themselves.");
            return result;
        }

        Optional<Review> review; 
        if (dto.isClient) {
            review = freelancerReviewRepository.getByAuthorId(dto.revieweeId, dto.authorId);
        }
        else {
            review = clientReviewRepository.getByAuthorId(dto.revieweeId, dto.authorId);
        }

        if (review.isPresent()) {
            result.setMessage("User can't write review to the same person twice.");
            return result;
        }

        return result;
    }
}
