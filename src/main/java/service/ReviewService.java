package service;

import java.util.List;

import org.springframework.stereotype.Service;
import entity.Review;
import entity.ReviewId;
import redis.clients.jedis.Jedis;
import repository.ClientReviewRepository;
import repository.FreelancerReviewRepository;
import repository.cache.FreelancerCacheRepository;

@Service
public class ReviewService {
    private final ClientReviewRepository clientReviewRepository;
    private final FreelancerReviewRepository freelancerReviewRepository;
    private final FreelancerCacheRepository freelancerCacheRepository;

    public ReviewService(ClientReviewRepository clientReviewRepository, FreelancerReviewRepository freelancerReviewRepository, FreelancerCacheRepository freelancerCacheRepository) {
        this.clientReviewRepository = clientReviewRepository;
        this.freelancerReviewRepository = freelancerReviewRepository;
        this.freelancerCacheRepository = freelancerCacheRepository;
    }

    public List<Review> getByRevieweeId(String revieweeId, boolean isClient) {
        if (isClient) {
            return freelancerReviewRepository.getAll(revieweeId);
        }
        else {
            return clientReviewRepository.getAll(revieweeId);
        }
    }

    public void addReview(Review review, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.add(review);

            ReviewId reviewId = review.getId();
            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);
            }
        }
        else {
            clientReviewRepository.add(review);
        }
    }

    public void editReview(Review review, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.update(review);

            ReviewId reviewId = review.getId();
            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);
            }
        }
        else {
            clientReviewRepository.update(review);
        }
    }

    public void removeReview(ReviewId id, boolean isClient) {
        if (isClient) {
            freelancerReviewRepository.remove(id);

            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(id.revieweeId(), jedisConn);
            }
        }
        else {
            clientReviewRepository.remove(id);
        }
    }
}