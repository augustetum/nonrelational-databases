package controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dto.CreateReviewRequestDto;
import dto.NewReviewDto;
import entity.Review;
import service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getByRevieweeId(String id) {
        List<Review> reviews = reviewService.getByRevieweeId(id);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public void addReview(String authorId, @RequestBody CreateReviewRequestDto requestDto) {
        NewReviewDto newReviewDto = new NewReviewDto();

        newReviewDto.setRating(requestDto.rating);
        newReviewDto.setDetails(requestDto.details);
        newReviewDto.setAuthorId(authorId);
        newReviewDto.setRevieweeId(requestDto.revieweeId);
    
        reviewService.addReview(newReviewDto);
    }
}