package controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dto.AddReviewRequestDto;
import dto.PermissionCheckResultDto;
import dto.EditReviewRequestDto;
import dto.RemoveReviewRequestDto;
import dto.ValidationResultDto;
import entity.Review;
import entity.ReviewId;
import service.CustomClientDetails;
import service.CustomFreelancerDetails;
import service.EventLogService;
import service.ReviewPermissionService;
import service.ReviewService;
import service.ReviewValidationService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewPermissionService permissionService;
    @Autowired
    private ReviewValidationService validationService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<List<Review>> getByRevieweeId(Authentication authentication, String revieweeId) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        List<Review> reviews = reviewService.getByRevieweeId(revieweeId, isClient);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> addReview(Authentication authentication, @RequestBody AddReviewRequestDto requestDto) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } else {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        }

        // check if user allowed to add review
        PermissionCheckResultDto permissionResult = permissionService.canAddReview(requestDto.revieweeId, userId,
                isClient);

        if (permissionResult.isDenied()) {
            eventLogService.logEvent("REVIEW", null, "REVIEW_CREATE", "FAILURE", userId, "PERMISSION DENIED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        // construct & validate review
        Review review = new Review();
        review.setId(new ReviewId(requestDto.revieweeId));
        review.setRating(requestDto.rating);
        review.setDetails(requestDto.details);
        review.setAuthorId(userId);

        ValidationResultDto validationResult = validationService.validate(review, false);
        if (validationResult.isInvalid()) {
            eventLogService.logEvent("REVIEW", null, "REVIEW_CREATE", "FAILURE", userId, "REVIEW INVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        // add review
        reviewService.addReview(review, isClient);
        eventLogService.logEvent("REVIEW", review.getId().reviewId(), "REVIEW_CREATE", "SUCCESS", userId,
                "RATING: " + review.getRating() + ", DETAILS: " + review.getDetails());
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> editReview(Authentication authentication, @RequestBody EditReviewRequestDto requestDto) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } else {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        }

        // check if user allowed to edit review
        PermissionCheckResultDto permissionResult = permissionService.canEditReview(requestDto.revieweeId,
                requestDto.reviewId, userId, isClient);

        if (permissionResult.isDenied()) {
            eventLogService.logEvent("REVIEW", requestDto.reviewId, "REVIEW_EDIT", "FAILURE", userId,
                    "PERMISSION DENIED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        // construct & validate review
        Review review = new Review();
        review.setId(new ReviewId(requestDto.revieweeId, requestDto.reviewId));
        review.setRating(requestDto.rating);
        review.setDetails(requestDto.details);
        review.setAuthorId(userId);

        ValidationResultDto validationResult = validationService.validate(review, true);
        if (validationResult.isInvalid()) {
            eventLogService.logEvent("REVIEW", review.getId().reviewId(), "REVIEW_EDIT", "FAILURE", userId,
                    "REVIEW INVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        // edit review
        reviewService.editReview(review, isClient);
        eventLogService.logEvent("REVIEW", review.getId().reviewId(), "REVIEW_EDIT", "SUCCESS", userId,
                "RATING: " + review.getRating() + ", DETAILS: " + review.getDetails());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> removeReview(Authentication authentication,
            @RequestBody RemoveReviewRequestDto requestDto) {
        boolean isClient = authentication.getPrincipal() instanceof CustomClientDetails;

        String userId;
        if (isClient) {
            CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        } else {
            CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
            userId = userDetails.getUser().getId();
        }

        // check if user allowed to edit review
        PermissionCheckResultDto permissionResult = permissionService.canDeleteReview(requestDto.revieweeId,
                requestDto.reviewId, userId, isClient);

        if (permissionResult.isDenied()) {
            eventLogService.logEvent("REVIEW", requestDto.reviewId, "REVIEW_DELETE", "FAILURE", userId,
                    "PERMISSION DENIED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        // delete review
        ReviewId id = new ReviewId(requestDto.revieweeId, requestDto.reviewId);
        reviewService.removeReview(id, isClient);
        eventLogService.logEvent("REVIEW", requestDto.reviewId, "REVIEW_DELETE", "SUCCESS", userId,
                null);
        return ResponseEntity.ok().build();
    }
}