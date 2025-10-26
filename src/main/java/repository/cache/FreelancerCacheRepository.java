package repository.cache;

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
        String freelancerDataKey =  buildFreelancerKey(id);

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
        String freelancerDataKey = buildFreelancerKey(id);

        try {
            String dtoJson = objectMapper.writeValueAsString(freelancerDetailsDto);
            jedisConn.set(freelancerDataKey, dtoJson);
        }
        catch (Exception ex) {
            throw new RuntimeException("Putting object to cache was unsuccessful", ex);
        }
    }

    public void invalidateFreelancer(String freelancerId, Jedis jedisConn) {
        String freelancerKey =  buildFreelancerKey(freelancerId);
        jedisConn.del(freelancerKey);
    }

    private String buildFreelancerKey(String id) {
        return String.format("freelancer:%s", id);
    }
}
