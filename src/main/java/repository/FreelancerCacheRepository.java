package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.FreelancerDetailsDto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public class FreelancerCacheRepository {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public FreelancerCacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    public Jedis getJedisConnection() {
        return jedisPool.getResource();
    }

    public FreelancerDetailsDto getFreelancerDetails(String id, Jedis jedisConn) {
        String freelancerDataKey = String.format("freelancer:%s", id);

        try {
            String cachedData = jedisConn.get(freelancerDataKey);

            if (cachedData == null) {
                return null;
            }

            FreelancerDetailsDto dto = objectMapper.readValue(cachedData, FreelancerDetailsDto.class);
            return dto;
        }
        catch (Exception ex) {
            throw new RuntimeException("Retrieving object from cache was unsuccessful", ex);
        }
    }

    public void setFreelancerDetails(FreelancerDetailsDto freelancerDetailsDto, Jedis jedisConn) {
        String id = freelancerDetailsDto.getId();
        String freelancerDataKey = String.format("freelancer:%s", id);

        try {
            String dtoJson = objectMapper.writeValueAsString(freelancerDetailsDto);
            jedisConn.set(freelancerDataKey, dtoJson);
        }
        catch (Exception ex) {
            throw new RuntimeException("Putting object to cache was unsuccessful", ex);
        }
    }

    public List<String> getLeaderboardFreelancerIds(String sortBy, int limit, int skip, Jedis jedisConn) {
        String leaderboardKey = String.format("leaderboard:%s:%d:%d", sortBy, limit, skip);

        try { 
            if (!jedisConn.exists(leaderboardKey)) {
                return null;
            }
            
            List<String> cachedIdsList = new ArrayList<>();
            Set<String> cachedIdsSet = jedisConn.smembers(leaderboardKey);
            cachedIdsList.addAll(cachedIdsSet);

            return cachedIdsList;
        }
        catch (Exception ex) {
            throw new RuntimeException("Retrieving object from cache was unsuccessful", ex);
        }
    }

    public void setLeaderboardFreelancerIds(String sortBy, int limit, int skip, List<String> freelancerIds, Jedis jedisConn) {
        if (freelancerIds == null) {
            return;
        }

        String leaderboardKey = String.format("leaderboard:%s:%d:%d", sortBy, limit, skip);

        // add leaderboard entry
        jedisConn.del(leaderboardKey); // TODO: should it be deleted?
        jedisConn.sadd(leaderboardKey, freelancerIds.toArray(new String[0]));
        jedisConn.expire(leaderboardKey, 120);

        // add entries for invalidation
        for (String id : freelancerIds) {
            setLeaderboardInvalidation(id, leaderboardKey, jedisConn);
        }
    }

    private void setLeaderboardInvalidation(String freelancerId, String leaderboardKey, Jedis jedisConn) {
        String invalidationKey = String.format("invalidation:%s", freelancerId);
        jedisConn.sadd(invalidationKey, leaderboardKey);
        jedisConn.expire(invalidationKey, 120);
    }

    public void invalidateFreelancer(String freelancerId, Jedis jedisConn) {
        // TODO: add transaction?
        String invalidationKey = String.format("invalidation:%s", freelancerId);
        Set<String> leaderboardKeys = jedisConn.smembers(invalidationKey);
        
        for (String key : leaderboardKeys) {
            jedisConn.del(key);
        }
        jedisConn.del(invalidationKey);

        String freelancerKey = String.format("freelancer:%s", freelancerId);
        jedisConn.del(freelancerKey);
    }
}
