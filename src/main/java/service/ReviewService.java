package service;

import java.util.List;
import org.springframework.stereotype.Service;
import dto.GetReviewsDto;
import dto.RemoveReviewDto;
import entity.Review;
import entity.ReviewId;
import repository.ClientReviewRepository;
import repository.FreelancerReviewRepository;

@Service
public class ReviewService {
    private final ClientReviewRepository clientReviewRepository;
    private final FreelancerReviewRepository freelancerReviewRepository;

    public ReviewService(ClientReviewRepository clientReviewRepository, FreelancerReviewRepository freelancerReviewRepository) {
        this.clientReviewRepository = clientReviewRepository;
        this.freelancerReviewRepository = freelancerReviewRepository;
    }

    public List<Review> getByRevieweeId(GetReviewsDto dto) {
        if (dto.isClient) {
            return freelancerReviewRepository.getAll(dto.revieweeId);
        }
        else {
            return clientReviewRepository.getAll(dto.revieweeId);
        }
    }

    public void addReview(Review review, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.add(review);
        }
        else {
            clientReviewRepository.add(review);
        }
    }

    public void editReview(Review review, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.update(review);
        }
        else {
            clientReviewRepository.update(review);
        }
    }

    public void removeReview(ReviewId id, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.remove(id);
        }
        else {
            clientReviewRepository.remove(id);
        }
    }
}