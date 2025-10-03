package service;

import java.util.List;
import org.springframework.stereotype.Service;
import dto.AddReviewDto;
import dto.EditReviewDto;
import dto.GetReviewsDto;
import dto.RemoveReviewDto;
import entity.Review;
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
            return clientReviewRepository.get(dto.revieweeId);
        }
        else {
            return freelancerReviewRepository.get(dto.revieweeId);
        }
    }

    public void addReview(AddReviewDto dto) {
        Review review = new Review();
        review.setRating(dto.rating);
        review.setDetails(dto.details);
        review.setAuthorId(dto.authorId);

        if (dto.isClient) {
            clientReviewRepository.add(dto.revieweeId, review);
        }
        else {
            freelancerReviewRepository.add(dto.revieweeId, review);
        }
    }

    public void editReview(EditReviewDto dto) {
        Review review = new Review();
        review.setId(dto.id);
        review.setRating(dto.rating);
        review.setDetails(dto.details);
        review.setAuthorId(dto.authorId);

        freelancerReviewRepository.update(dto.revieweeId, review);
    }

    public void removeReview(RemoveReviewDto dto) {
        freelancerReviewRepository.remove(dto.revieweeId, dto.reviewId);
    }
}