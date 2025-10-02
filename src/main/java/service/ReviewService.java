package service;

import java.util.List;
import org.springframework.stereotype.Service;
import dto.NewReviewDto;
import entity.Review;
import repository.ReviewRepository;

@Service
public class ReviewService {
    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    public List<Review> getByRevieweeId(String revieweeId) {
        return repository.get(revieweeId);
    }

    public void addReview(NewReviewDto dto) {
        Review review = new Review();

        review.setRating(dto.rating);
        review.setDetails(dto.details);
        review.setAuthorId(dto.authorId);

        repository.add(dto.revieweeId, review);
    }
}