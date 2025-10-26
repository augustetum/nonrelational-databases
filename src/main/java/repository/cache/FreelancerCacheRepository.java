package repository.cache;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.FreelancerDetailsDto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public class FreelancerCacheRepository extends CacheRepository {
    
    public FreelancerCacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        super(jedisPool, objectMapper);
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
