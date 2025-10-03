package controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dto.AddReviewRequestDto;
import dto.EditReviewDto;
import dto.EditReviewRequestDto;
import dto.RemoveReviewDto;
import dto.RemoveReviewRequestDto;
import dto.AddReviewDto;
import entity.Review;
import service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getByRevieweeId(String revieweeId) {
        List<Review> reviews = reviewService.getByRevieweeId(revieweeId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public void addReview(String authorId, @RequestBody AddReviewRequestDto requestDto) {
        AddReviewDto addReviewDto = new AddReviewDto();

        addReviewDto.setRating(requestDto.rating);
        addReviewDto.setDetails(requestDto.details);
        addReviewDto.setAuthorId(authorId);
        addReviewDto.setRevieweeId(requestDto.revieweeId);
    
        reviewService.addReview(addReviewDto);
    }

    @PutMapping
    public void editReview(String authorId, @RequestBody EditReviewRequestDto requestDto) {
        EditReviewDto editReviewDto = new EditReviewDto();

        editReviewDto.setId(requestDto.id);
        editReviewDto.setRating(requestDto.rating);
        editReviewDto.setDetails(requestDto.details);
        editReviewDto.setAuthorId(authorId);
        editReviewDto.setRevieweeId(requestDto.revieweeId);   
        
        reviewService.editReview(editReviewDto);
    }

    @DeleteMapping
    public void removeReview(@RequestBody RemoveReviewRequestDto requestDto) {
        RemoveReviewDto removeReviewDto = new RemoveReviewDto();

        removeReviewDto.setRevieweeId(requestDto.revieweeId);
        removeReviewDto.setReviewId(requestDto.reviewId);

        reviewService.removeReview(removeReviewDto);
    }
}