package config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

public class Neo4JConfig {
    @Configuration
    public class Neo4jConfig {

        @Value("${neo4j.uri:bolt://localhost:7687}")
        private String uri;

        @Value("${neo4j.username:neo4j}")
        private String username;

        @Value("${neo4j.password:darbsciu_rankuciu_klubas}")
        private String password;

        @Bean
        public Driver neo4jDriver() {
            return GraphDatabase.driver(uri,
                    AuthTokens.basic(username, password));
        }

        @PreDestroy
        public void closeDriver(@Autowired Driver driver) {
            driver.close();
        }
    }

}
