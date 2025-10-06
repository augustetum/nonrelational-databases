package service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import dto.AddReviewDto;
import dto.PermissionCheckResultDto;
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

    public PermissionCheckResultDto canAddReview(AddReviewDto dto) {
        PermissionCheckResultDto result = new PermissionCheckResultDto();

        if (dto.revieweeId == dto.authorId) {
            result.setMessage("Users are not allowed write reviews to themselves.");
            return result;
        }

        Optional<Review> maybeReview; 
        if (dto.isClient) {
            maybeReview = freelancerReviewRepository.getByAuthorId(dto.revieweeId, dto.authorId);
        }
        else {
            maybeReview = clientReviewRepository.getByAuthorId(dto.revieweeId, dto.authorId);
        }

        if (maybeReview.isPresent()) {
            result.setMessage("Users are not allowed to write reviews to the same person more than once.");
            return result;
        }

        return result;
    }

    public PermissionCheckResultDto canEditReview(String revieweeId, String reviewId, String requestorId, boolean isClient) {
        PermissionCheckResultDto result = new PermissionCheckResultDto();
        
        Optional<Review> maybeReview;
        if(isClient) {
            maybeReview = freelancerReviewRepository.getByReviewId(revieweeId, reviewId);
        }
        else {
            maybeReview = clientReviewRepository.getByReviewId(revieweeId, reviewId);
        }

        if (!maybeReview.isPresent()) {
            result.setMessage("Review with specified id does not exist.");
            return result;
        }

        Review review = maybeReview.get();
        if (review.authorId != requestorId) {
            result.setMessage("Users are not allowed to edit reviews written by other users.");
            return result;
        }

        return result;
    }
}
