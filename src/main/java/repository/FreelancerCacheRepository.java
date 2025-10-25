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

    public FreelancerDetailsDto getFreelancerDetails(String id) {
        String freelancerDataKey = String.format("freelancers:%s", id);

        try (Jedis jedis = jedisPool.getResource()) {
            String cachedData = jedis.get(freelancerDataKey);

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

    public void setFreelancerDetails(FreelancerDetailsDto freelancerDetailsDto) {
        String id = freelancerDetailsDto.getId();
        String freelancerDataKey = String.format("freelancers:%s", id);

        try (Jedis jedis = jedisPool.getResource()) {
            String dtoJson = objectMapper.writeValueAsString(freelancerDetailsDto);
            jedis.set(freelancerDataKey, dtoJson);
        }
        catch (Exception ex) {
            throw new RuntimeException("Retrieving object from cache was unsuccessful", ex);
        }
    }
}
