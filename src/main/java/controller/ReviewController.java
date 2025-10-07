package controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import dto.EditReviewDto;
import dto.EditReviewRequestDto;
import dto.GetReviewsDto;
import dto.RemoveReviewDto;
import dto.RemoveReviewRequestDto;
import dto.ValidationResultDto;
import dto.AddReviewDto;
import entity.Review;
import enumerator.PermissionStatus;
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

    @GetMapping
    public ResponseEntity<List<Review>> getByRevieweeId(boolean isClient, String revieweeId) {
        GetReviewsDto getReviewsDto = new GetReviewsDto();
        getReviewsDto.setRevieweeId(revieweeId);
        getReviewsDto.setIsClient(isClient);
        
        List<Review> reviews = reviewService.getByRevieweeId(getReviewsDto);

        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> addReview(String authorId, boolean isClient, @RequestBody AddReviewRequestDto requestDto) {        
        // check if user allowed to add review
        PermissionCheckResultDto permissionResult = permissionService.canAddReview(requestDto.revieweeId, authorId, isClient);
        PermissionStatus status = permissionResult.getStatus(); 
        
        if (status == PermissionStatus.DENIED)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        // construct & validate review
        Review review = new Review();
        review.setRating(requestDto.rating);
        review.setDetails(requestDto.details);
        review.setAuthorId(authorId);

        ValidationResultDto validationResult = validationService.validate(review, false);
        if (validationResult.isInvalid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        } 

        // add review
        AddReviewDto addReviewDto = new AddReviewDto();
        addReviewDto.setRating(requestDto.rating);
        addReviewDto.setDetails(requestDto.details);
        addReviewDto.setAuthorId(authorId);
        addReviewDto.setClient(isClient);
        addReviewDto.setRevieweeId(requestDto.revieweeId);

        reviewService.addReview(addReviewDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> editReview(String authorId, boolean isClient, @RequestBody EditReviewRequestDto requestDto) {
        // check if user allowed to edit review
        PermissionCheckResultDto permissionResult = permissionService.canEditReview(requestDto.revieweeId, requestDto.id, authorId, isClient);
        PermissionStatus status = permissionResult.getStatus(); 

        if (status == PermissionStatus.DENIED)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }

        // construct & validate review
        Review review = new Review();
        review.setId(requestDto.id);
        review.setRating(requestDto.rating);
        review.setDetails(requestDto.details);
        review.setAuthorId(authorId);

        ValidationResultDto validationResult = validationService.validate(review, true);
        if (validationResult.isInvalid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        } 
        
        // edit review
        EditReviewDto editReviewDto = new EditReviewDto();
        editReviewDto.setId(requestDto.id);
        editReviewDto.setRating(requestDto.rating);
        editReviewDto.setDetails(requestDto.details);
        editReviewDto.setAuthorId(authorId);
        editReviewDto.setClient(isClient);
        editReviewDto.setRevieweeId(requestDto.revieweeId);

        reviewService.editReview(editReviewDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> removeReview(String authorId, boolean isClient, @RequestBody RemoveReviewRequestDto requestDto) {
        // check if user allowed to edit review
        PermissionCheckResultDto permissionResult = permissionService.canDeleteReview(requestDto.revieweeId, requestDto.reviewId, authorId, isClient);
        PermissionStatus status = permissionResult.getStatus(); 

        if (status == PermissionStatus.DENIED)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        }
        
        // delete review       
        RemoveReviewDto removeReviewDto = new RemoveReviewDto();
        removeReviewDto.setRevieweeId(requestDto.revieweeId);
        removeReviewDto.setReviewId(requestDto.reviewId);
        removeReviewDto.setAuthorId(authorId);
        removeReviewDto.setClient(isClient);

        reviewService.removeReview(removeReviewDto);

        return ResponseEntity.ok().build();
    }
}