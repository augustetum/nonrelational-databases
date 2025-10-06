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
import dto.AuthorizationResultDto;
import dto.EditReviewDto;
import dto.EditReviewRequestDto;
import dto.GetReviewsDto;
import dto.RemoveReviewDto;
import dto.RemoveReviewRequestDto;
import dto.AddReviewDto;
import entity.Review;
import enumerator.AuthorizationStatus;
import service.ReviewPermissionService;
import service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewPermissionService permissionService;
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
        AddReviewDto addReviewDto = new AddReviewDto();
        addReviewDto.setRating(requestDto.rating);
        addReviewDto.setDetails(requestDto.details);
        addReviewDto.setAuthorId(authorId);
        addReviewDto.setClient(isClient);
        addReviewDto.setRevieweeId(requestDto.revieweeId);
        
        // check if user allowed to add review
        AuthorizationResultDto authorizationResult = permissionService.canAddReview(addReviewDto);
        AuthorizationStatus status = authorizationResult.getStatus(); 
        
        if (status == AuthorizationStatus.FAILURE)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authorizationResult);
        }

        // add review
        reviewService.addReview(addReviewDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping
    public void editReview(String authorId, boolean isClient, @RequestBody EditReviewRequestDto requestDto) {
        EditReviewDto editReviewDto = new EditReviewDto();
        editReviewDto.setId(requestDto.id);
        editReviewDto.setRating(requestDto.rating);
        editReviewDto.setDetails(requestDto.details);
        editReviewDto.setAuthorId(authorId);
        editReviewDto.setClient(isClient);
        editReviewDto.setRevieweeId(requestDto.revieweeId);   
        
        reviewService.editReview(editReviewDto);
    }

    @DeleteMapping
    public void removeReview(String authorId, boolean isClient, @RequestBody RemoveReviewRequestDto requestDto) {
        RemoveReviewDto removeReviewDto = new RemoveReviewDto();
        removeReviewDto.setRevieweeId(requestDto.revieweeId);
        removeReviewDto.setReviewId(requestDto.reviewId);
        removeReviewDto.setAuthorId(authorId);
        removeReviewDto.setClient(isClient);

        reviewService.removeReview(removeReviewDto);
    }
}