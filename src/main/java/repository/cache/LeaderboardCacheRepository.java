package repository.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.FreelancerJobsCompletedLeaderboardDto;
import dto.FreelancerRatingLeaderboardDto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.resps.Tuple;

@Repository
public class LeaderboardCacheRepository extends CacheRepository {
    
    public LeaderboardCacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        super(jedisPool, objectMapper);
    }

    public List<FreelancerRatingLeaderboardDto> getAverageRatingLeaderboard(int limit, int skip, Jedis jedisConn) {
        // check if leaderboard exists in cache
        String leaderboardKey = buildLeaderboardKey("averageRating");
        if (!jedisConn.exists(leaderboardKey)) {
            return null;
        }
        
        // retrieve leaderboard
        List<Tuple> leaderboard = jedisConn.zrevrangeWithScores(leaderboardKey, skip, skip + limit);
        
        // retrieve leaderboard details
        Pipeline pipeline = jedisConn.pipelined();

        Map<String, Response<Map<String, String>>> leaderboardEntries = new HashMap<>();

        for (Tuple pair : leaderboard) {
            String freelancerId = pair.getElement();
            String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);

            leaderboardEntries.put(freelancerId, pipeline.hgetAll(entryKey));
        }

        pipeline.sync();

        // combine data
        List<FreelancerRatingLeaderboardDto> leaderboardDetails = new ArrayList<>();

        for (Tuple pair : leaderboard) {            
            FreelancerRatingLeaderboardDto dto = new FreelancerRatingLeaderboardDto();
            
            // set freelancer id
            String freelancerId = pair.getElement();
            dto.setId(freelancerId);

            // retrieve entry details
            Response<Map<String, String>> entryInfo = leaderboardEntries.get(freelancerId);

            // set freelancer first name
            String firstName = entryInfo.get().get("firstName");
            dto.setFirstName(firstName);
            
            // set freelancer last name
            String lastName = entryInfo.get().get("lastName");
            dto.setLastName(lastName);

            // set freelancer average rating
            double rating = pair.getScore();
            BigDecimal ratingBigDecimal = null;
            
            if (rating != -1) {
                ratingBigDecimal = BigDecimal.valueOf(rating);
            }

            dto.setRating(ratingBigDecimal);
            
            // set freelancer review number
            String reviewNumStr = entryInfo.get().get("reviewNum");
            int reviewNum = Integer.valueOf(reviewNumStr);
            dto.setReviewNum(reviewNum);

            leaderboardDetails.add(dto);
        }

        return leaderboardDetails;
    }

    // public List<FreelancerJobsCompletedLeaderboardDto> getJobsCompletedLeaderboard(int limit, int skip, Jedis jedisConn) {
    //     String leaderboardKey = buildLeaderboardKey("averageRating");
        
    //     // Pipeline pipeline = jedisConn.pipelined();

    //     // TODO: 
    // }

    public void setAverageRatingLeaderboard(List<FreelancerRatingLeaderboardDto> leaderboardDetails, Jedis jedisConn) {
        Map<String, Double> ratings = new HashMap<>();
        
        for (FreelancerRatingLeaderboardDto dto : leaderboardDetails) {
            String freelancerId = dto.getId();
            
            BigDecimal ratingBigDecimal = dto.getRating();
            double rating = -1;

            if (ratingBigDecimal != null) {
                rating = ratingBigDecimal.doubleValue();
            }

            ratings.put(freelancerId, rating);

            // add freelancer details
            Map<String, String> hash = new HashMap<>();

            hash.put("firstName", dto.getFirstName());
            hash.put("lastName", dto.getLastName());
            hash.put("rating", String.valueOf(rating));
            hash.put("reviewNum", String.valueOf(dto.getReviewNum()));
            
            String entryKey = buildLeaderboardEntryKey("averageRating", freelancerId);
            jedisConn.hset(entryKey, hash);
        }

        String leaderboardKey = buildLeaderboardKey("averageRating");
        jedisConn.zadd(leaderboardKey, ratings);
    }

    public void setJobsCompletedLeaderboard(List<FreelancerJobsCompletedLeaderboardDto> leaderboardDetails, Jedis jedisConn) {
        Map<String, Double> jobsCompletedMap = new HashMap<>();
        
        for (FreelancerJobsCompletedLeaderboardDto dto : leaderboardDetails) {
            // append to leaderboard structure
            String freelancerId = dto.getId();

            int jobsCompletedInt = dto.getJobsCompleted();
            Double jobsCompleted = (double)jobsCompletedInt;

            jobsCompletedMap.put(freelancerId, jobsCompleted);

            // add freelancer details
            Map<String, String> hash = new HashMap<>();

            hash.put("firstName", dto.getFirstName());
            hash.put("lastName", dto.getLastName());
            hash.put("jobsCompleted", String.valueOf(jobsCompleted));
            
            String entryKey = buildLeaderboardEntryKey("jobCompleted", freelancerId);
            jedisConn.hset(entryKey, hash);
        }

        String leaderboardKey = buildLeaderboardKey("averageRating");
        jedisConn.zadd(leaderboardKey, jobsCompletedMap);
    }

    private String buildLeaderboardKey(String sortBy) {
        return String.format("leaderboard:%s", sortBy);
    }

    private String buildLeaderboardEntryKey(String sortBy, String freelancerId) {
        return String.format("leaderboard:%s:%s", sortBy, freelancerId);
    }
}
