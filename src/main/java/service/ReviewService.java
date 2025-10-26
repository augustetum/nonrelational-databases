package service;

import java.util.List;

import org.springframework.stereotype.Service;

import entity.Review;
import entity.ReviewId;
import redis.clients.jedis.Jedis;
import repository.ClientReviewRepository;
import repository.FreelancerReviewRepository;
import repository.cache.FreelancerCacheRepository;
import repository.cache.LeaderboardCacheRepository;

@Service
public class ReviewService {
    private final ClientReviewRepository clientReviewRepository;
    private final FreelancerReviewRepository freelancerReviewRepository;

    private final FreelancerCacheRepository freelancerCacheRepository;
    private final LeaderboardCacheRepository leaderboardCacheRepository;

    public ReviewService(ClientReviewRepository clientReviewRepository, FreelancerReviewRepository freelancerReviewRepository, FreelancerCacheRepository freelancerCacheRepository, LeaderboardCacheRepository leaderboardCacheRepository) {
        this.clientReviewRepository = clientReviewRepository;
        this.freelancerReviewRepository = freelancerReviewRepository;

        this.freelancerCacheRepository = freelancerCacheRepository;
        this.leaderboardCacheRepository = leaderboardCacheRepository;
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
            // update cache
            String freelancerId = review.getId().revieweeId();

            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(freelancerId, jedisConn);
                leaderboardCacheRepository.updateEntryDetailsOnAdd(review, jedisConn);
            }

            // db changes
            freelancerReviewRepository.add(review);
        }
        else {
            clientReviewRepository.add(review);
        }
    }

    public void editReview(Review review, boolean isClient) {
        if (isClient) {
            // update cache
            String freelancerId = review.getId().revieweeId();
            String reviewId = review.getId().reviewId();
            Review oldReview = freelancerReviewRepository.getByReviewId(freelancerId, reviewId);

            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(review.getId().revieweeId(), jedisConn);
                leaderboardCacheRepository.updateEntryDetailsOnEdit(oldReview, review, jedisConn);
            }

            // db changes
            freelancerReviewRepository.update(review);
        }
        else {
            clientReviewRepository.update(review);
        }
    }

    public void removeReview(ReviewId id, boolean isClient) {
        if (isClient) {
            // update cache
            Review oldReview = freelancerReviewRepository.getByReviewId(id.revieweeId(), id.reviewId());

            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                freelancerCacheRepository.invalidateFreelancer(id.revieweeId(), jedisConn);
                leaderboardCacheRepository.updateEntryDetailsOnRemove(oldReview, jedisConn);
            }

            // db changes
            freelancerReviewRepository.remove(id);
        }
        else {
            clientReviewRepository.remove(id);
        }
    }
}