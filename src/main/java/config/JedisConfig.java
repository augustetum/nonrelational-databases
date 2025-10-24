package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;


@Configuration
public class JedisConfig {
    @Bean
    public JedisPool jedisPool() {
        return new JedisPool( "localhost", 8090);
    }
}