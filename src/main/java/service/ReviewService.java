package service;

import java.util.List;
import org.springframework.stereotype.Service;
import entity.Review;
import repository.ReviewRepository;

@Service
public class ReviewService {
    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    public List<Review> getByRevieweeId(String id) {
        return repository.getByRevieweeId(id);
    }
}
