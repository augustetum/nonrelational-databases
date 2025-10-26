package service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import dto.FreelancerRatingLeaderboardDto;
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
            freelancerReviewRepository.add(review);

            // update cache
            ReviewId reviewId = review.getId();
            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                // invalidate freelancer details
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);
                
                // change leaderboard
                // TODO: add transaction?
                FreelancerRatingLeaderboardDto dto = leaderboardCacheRepository.getLeaderboardEntry(reviewId.revieweeId(), jedisConn);
                
                // re-calculate average rating
                BigDecimal reviewRatingBigDecimal = review.getRating();
                BigDecimal avgRatingBigDecimal = dto.getRating();
                
                if (avgRatingBigDecimal == null) {
                    avgRatingBigDecimal = BigDecimal.ZERO;
                }

                int reviewNum = dto.getReviewNum();

                double reviewRating = reviewRatingBigDecimal.doubleValue();
                double avgRating = avgRatingBigDecimal.doubleValue();

                int updatedReviewNum = reviewNum + 1;
                double updatedAvgRating = (avgRating * reviewNum + reviewRating) / updatedReviewNum;

                BigDecimal updatedAvgRatingBigDecimal = BigDecimal.valueOf(updatedAvgRating);

                dto.setRating(updatedAvgRatingBigDecimal);
                dto.setReviewNum(updatedReviewNum);

                leaderboardCacheRepository.updateAverageRatingLeaderboard(dto, jedisConn);
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
                // invalidate freelancer details
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);

                // change leaderboard
                // TODO: add transaction?
                FreelancerRatingLeaderboardDto dto = leaderboardCacheRepository.getLeaderboardEntry(reviewId.revieweeId(), jedisConn);
                
                // re-calculate average rating
                BigDecimal reviewRatingBigDecimal = review.getRating();
                BigDecimal avgRatingBigDecimal = dto.getRating();
                
                if (avgRatingBigDecimal == null) {
                    avgRatingBigDecimal = BigDecimal.ZERO;
                }

                int reviewNum = dto.getReviewNum();

                double reviewRating = reviewRatingBigDecimal.doubleValue();
                double avgRating = avgRatingBigDecimal.doubleValue();
                double updatedAvgRating = (avgRating * reviewNum + reviewRating) / reviewNum;

                BigDecimal updatedAvgRatingBigDecimal = BigDecimal.valueOf(updatedAvgRating);

                dto.setRating(updatedAvgRatingBigDecimal);

                leaderboardCacheRepository.updateAverageRatingLeaderboard(dto, jedisConn);
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