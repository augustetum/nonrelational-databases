package repository.cache;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public abstract class CacheRepository {
    protected final JedisPool jedisPool;
    protected final ObjectMapper objectMapper;

    public CacheRepository(JedisPool jedisPool, ObjectMapper objectMapper) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }
    
    public Jedis getJedisConnection() {
        return jedisPool.getResource();
    }
}
