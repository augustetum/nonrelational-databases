package repository;

import java.util.List;

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
        String freelancerDataKey = String.format("freelancers:%s", id);

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
        String freelancerDataKey = String.format("freelancers:%s", id);

        try {
            String dtoJson = objectMapper.writeValueAsString(freelancerDetailsDto);
            jedisConn.setex(freelancerDataKey, 30, dtoJson); // TODO: remove ttl
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
            
            List<String> cachedIds = jedisConn.lrange(leaderboardKey, 0, -1);
            return cachedIds;
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
        try { 
            jedisConn.del(leaderboardKey); // TODO: should it be deleted?
            jedisConn.rpush(leaderboardKey, freelancerIds.toArray(new String[0]));
            jedisConn.expire(leaderboardKey, 30); // TODO: remove after invalidation is in place
        }
        catch (Exception ex) {
            throw new RuntimeException("Putting object to cache was unsuccessful", ex);
        }
    }
}
