package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dto.LeaderboardDetailsDto;
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
            ReviewId reviewId = review.getId();
            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                // invalidate freelancer details
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);
                
                // change leaderboard
                // TODO: add transaction?
                if (leaderboardCacheRepository.leaderboardExists(jedisConn))
                {
                    LeaderboardDetailsDto cachedDto = leaderboardCacheRepository.getLeaderboardEntry(reviewId.revieweeId(), jedisConn);
                    
                    // re-calculate average rating
                    BigDecimal reviewRatingBigDecimal = review.getRating();
                    BigDecimal avgRatingBigDecimal = cachedDto.getRating();
                    
                    if (avgRatingBigDecimal == null) {
                        avgRatingBigDecimal = BigDecimal.ZERO;
                    }

                    int reviewNum = cachedDto.getReviewNum();

                    double reviewRating = reviewRatingBigDecimal.doubleValue();
                    double avgRating = avgRatingBigDecimal.doubleValue();

                    int updatedReviewNum = reviewNum + 1;
                    double updatedAvgRating = (avgRating * reviewNum + reviewRating) / updatedReviewNum;

                    BigDecimal updatedAvgRatingBigDecimal = BigDecimal.valueOf(updatedAvgRating);

                    cachedDto.setRating(updatedAvgRatingBigDecimal);
                    cachedDto.setReviewNum(updatedReviewNum);

                    leaderboardCacheRepository.updateAverageRatingLeaderboard(reviewId.revieweeId(), updatedAvgRatingBigDecimal, reviewNum, jedisConn);
                }
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
            ReviewId reviewId = review.getId();
            Review oldReview = freelancerReviewRepository.getByReviewId(reviewId.revieweeId(), reviewId.reviewId());

            try (Jedis jedisConn = freelancerCacheRepository.getJedisConnection()) {
                // invalidate freelancer details
                freelancerCacheRepository.invalidateFreelancer(reviewId.revieweeId(), jedisConn);

                // change leaderboard
                // TODO: add transaction?

                if (leaderboardCacheRepository.leaderboardExists(jedisConn))
                {
                    LeaderboardDetailsDto cachedDto = leaderboardCacheRepository.getLeaderboardEntry(reviewId.revieweeId(), jedisConn);
                    
                    // re-calculate average rating
                    BigDecimal reviewRatingBigDecimal = review.getRating();
                    BigDecimal oldReviewRatingBigDecimal = oldReview.getRating();
                    BigDecimal avgRatingBigDecimal = cachedDto.getRating();

                    if (avgRatingBigDecimal == null) {
                        avgRatingBigDecimal = BigDecimal.ZERO;
                    }

                    int reviewNum = cachedDto.getReviewNum();

                    double reviewRating = reviewRatingBigDecimal.doubleValue();
                    double oldReviewRating = oldReviewRatingBigDecimal.doubleValue(); 
                    double avgRating = avgRatingBigDecimal.doubleValue();

                    double updatedAvgRating = (avgRating * reviewNum - oldReviewRating + reviewRating) / reviewNum;

                    BigDecimal updatedAvgRatingBigDecimal = BigDecimal.valueOf(updatedAvgRating);

                    leaderboardCacheRepository.updateAverageRatingLeaderboard(reviewId.revieweeId(), updatedAvgRatingBigDecimal, reviewNum, jedisConn);
                }
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

                // change leaderboard
                // TODO: add transaction?
                if (leaderboardCacheRepository.leaderboardExists(jedisConn))
                {
                    LeaderboardDetailsDto cachedDto = leaderboardCacheRepository.getLeaderboardEntry(id.revieweeId(), jedisConn);
                    
                    // re-calculate average rating
                    BigDecimal oldReviewRatingBigDecimal = oldReview.getRating();
                    BigDecimal avgRatingBigDecimal = cachedDto.getRating();

                    if (avgRatingBigDecimal == null) {
                        avgRatingBigDecimal = BigDecimal.ZERO;
                    }

                    int reviewNum = cachedDto.getReviewNum();

                    double oldReviewRating = oldReviewRatingBigDecimal.doubleValue(); 
                    double avgRating = avgRatingBigDecimal.doubleValue();

                    int updatedReviewNum = reviewNum - 1;
                    double updatedAvgRating = 0;
                    
                    if (updatedReviewNum != 0)
                    {    
                        updatedAvgRating = (avgRating * reviewNum - oldReviewRating) / updatedReviewNum;
                    }

                    BigDecimal updatedAvgRatingBigDecimal = null;

                    if (updatedReviewNum != 0) {
                        updatedAvgRatingBigDecimal = BigDecimal.valueOf(updatedAvgRating);
                    }
                    
                    cachedDto.setRating(updatedAvgRatingBigDecimal);
                    cachedDto.setReviewNum(updatedReviewNum);
                    
                    leaderboardCacheRepository.updateAverageRatingLeaderboard(id.revieweeId(), updatedAvgRatingBigDecimal, reviewNum, jedisConn);
                }
            }

            // db changes
            freelancerReviewRepository.remove(id);
        }
        else {
            clientReviewRepository.remove(id);
        }
    }
}