package service;

import java.util.Optional;
import org.springframework.stereotype.Service;
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

    public PermissionCheckResultDto canAddReview(String revieweeId, String requestorId, boolean isClient) {
        if (revieweeId == requestorId) {
            return PermissionCheckResultDto.invalid("Users are not allowed write reviews to themselves.");
        }

        Optional<Review> maybeReview; 
        if (isClient) {
            maybeReview = freelancerReviewRepository.getByAuthorId(revieweeId, requestorId);
        }
        else {
            maybeReview = clientReviewRepository.getByAuthorId(revieweeId, requestorId);
        }

        if (maybeReview.isPresent()) {
            return PermissionCheckResultDto.invalid("Users are not allowed to write reviews to the same person more than once.");
        }

        return PermissionCheckResultDto.valid();
    }

    public PermissionCheckResultDto canEditReview(String revieweeId, String reviewId, String requestorId, boolean isClient) {       
        Optional<Review> maybeReview;
        if(isClient) {
            maybeReview = freelancerReviewRepository.getByReviewId(revieweeId, reviewId);
        }
        else {
            maybeReview = clientReviewRepository.getByReviewId(revieweeId, reviewId);
        }

        if (!maybeReview.isPresent()) {
            return PermissionCheckResultDto.invalid("Review with specified id does not exist.");
        }

        Review review = maybeReview.get();
        if (!requestorId.equals(review.authorId)) {
            return PermissionCheckResultDto.invalid("Users are not allowed to edit reviews written by other users.");
        }

        return PermissionCheckResultDto.valid();
    }

    public PermissionCheckResultDto canDeleteReview(String revieweeId, String reviewId, String requestorId, boolean isClient) {
        Optional<Review> maybeReview;
        if(isClient) {
            maybeReview = freelancerReviewRepository.getByReviewId(revieweeId, reviewId);
        }
        else {
            maybeReview = clientReviewRepository.getByReviewId(revieweeId, reviewId);
        }

        if (!maybeReview.isPresent()) {
            return PermissionCheckResultDto.invalid("Review with specified id does not exist.");
        }

        Review review = maybeReview.get();
        if (!requestorId.equals(review.authorId)) {
            return PermissionCheckResultDto.invalid("Users are not allowed to delete reviews written by other users.");
        }

        return PermissionCheckResultDto.valid();
    }
}
