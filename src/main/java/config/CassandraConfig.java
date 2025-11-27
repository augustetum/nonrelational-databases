package config;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.oss.driver.api.core.CqlSession;

import jakarta.annotation.PreDestroy;

@Configuration
public class CassandraConfig {

    @Value("${cassandra.contact-points:127.0.0.1}")
    private String contactPoints;

    @Value("${cassandra.port:9042}")
    private int port;

    @Value("${cassandra.keyspace:darbsciu_rankuciu_klubas}")
    private String keyspace;

    @Value("${cassandra.datacenter:datacenter1}")
    private String datacenter;

    @Bean
    public CqlSession cqlSession() {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace)
                .build();
    }

    @PreDestroy
    public void cleanup() {
        if (cqlSession() != null) {
            cqlSession().close();
        }
    }
}