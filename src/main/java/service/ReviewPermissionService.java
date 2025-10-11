package service;

import java.util.Optional;
import org.springframework.stereotype.Service;

import dto.ClientDetailsDto;
import dto.FreelancerDetailsDto;
import dto.PermissionCheckResultDto;
import entity.Freelancer;
import entity.Review;
import repository.ClientRepository;
import repository.ClientReviewRepository;
import repository.FreelancerRepository;
import repository.FreelancerReviewRepository;

@Service
public class ReviewPermissionService {
    private final ClientRepository clientRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientReviewRepository clientReviewRepository;
    private final FreelancerReviewRepository freelancerReviewRepository;

    public ReviewPermissionService(
        ClientRepository clientRepository, 
        FreelancerRepository freelancerRepository, 
        ClientReviewRepository clientReviewRepository, 
        FreelancerReviewRepository freelancerReviewRepository
    ) {
        this.clientRepository = clientRepository;
        this.freelancerRepository = freelancerRepository;
        this.clientReviewRepository = clientReviewRepository;
        this.freelancerReviewRepository = freelancerReviewRepository;
    }

    public PermissionCheckResultDto canAddReview(String revieweeId, String requestorId, boolean isClient) {
        if (revieweeId == requestorId) {
            return PermissionCheckResultDto.invalid("Users are not allowed write reviews to themselves.");
        }

        if(!userExists(revieweeId, isClient)) {
            return PermissionCheckResultDto.invalid("User with specified id does not exist.");
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
        if(!userExists(revieweeId, isClient)) {
            return PermissionCheckResultDto.invalid("User with specified id does not exist.");
        }
        
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
        if (!requestorId.equals(review.getAuthorId())) {
            return PermissionCheckResultDto.invalid("Users are not allowed to edit reviews written by other users.");
        }

        return PermissionCheckResultDto.valid();
    }

    public PermissionCheckResultDto canDeleteReview(String revieweeId, String reviewId, String requestorId, boolean isClient) {
        if(!userExists(revieweeId, isClient)) {
            return PermissionCheckResultDto.invalid("User with specified id does not exist.");
        }
        
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
        if (!requestorId.equals(review.getAuthorId())) {
            return PermissionCheckResultDto.invalid("Users are not allowed to delete reviews written by other users.");
        }

        return PermissionCheckResultDto.valid();
    }

    private boolean userExists(String userId, boolean requestorIsClient) {
        if (requestorIsClient) {
            Optional<FreelancerDetailsDto> maybeFreelancer = freelancerRepository.getDetails(userId);

            if (!maybeFreelancer.isPresent()) {
                return false;
            }
        }
        else {
            Optional<ClientDetailsDto> maybeClient = clientRepository.getDetails(userId);

            if (!maybeClient.isPresent()) {
                return false;
            }
        }

        return true;
    }
}
