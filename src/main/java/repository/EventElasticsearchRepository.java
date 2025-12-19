package repository;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import entity.Event;

@Repository
public class EventElasticsearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index:events}")
    private String indexName;

    public EventElasticsearchRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<Event> findByEventStatus(String eventStatus) {
        try {
            Query query = Query.of(q -> q
                .match(m -> m
                    .field("eventStatus")
                    .query(eventStatus)
                )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(query)
                .size(1000)
            );

            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

            List<Event> events = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Event event = mapToEvent(hit.source());
                events.add(event);
            }

            return events;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Event> getAllEvents() {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q.matchAll(m -> m))
                .size(1000)
            );

            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

            List<Event> events = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Event event = mapToEvent(hit.source());
                events.add(event);
            }

            return events;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Event mapToEvent(Map<String, Object> source) {
        Event event = new Event();

        event.setId(getStringValue(source, "id"));
        event.setEntityType(getStringValue(source, "entityType"));
        event.setEntityId(getStringValue(source, "entityId"));
        event.setEventType(getStringValue(source, "eventType"));
        event.setEventStatus(getStringValue(source, "eventStatus"));
        event.setUserId(getStringValue(source, "userId"));
        event.setDetails(getStringValue(source, "details"));

        Object timeValue = source.get("time");
        if (timeValue != null) {
            if (timeValue instanceof Long) {
                event.setTime(Instant.ofEpochMilli((Long) timeValue));
            } else if (timeValue instanceof String) {
                try {
                    event.setTime(Instant.parse((String) timeValue));
                } catch (Exception e) {
                    event.setTime(Instant.now());
                }
            }
        }

        return event;
    }

    private String getStringValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value != null ? value.toString() : null;
    }
}