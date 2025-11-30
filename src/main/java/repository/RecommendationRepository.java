package repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendationRepository {
    private final Driver driver;

    private final String getByWorkfieldCategory = 
        """
            match  (client:Client { clientId:$clientId }) -[:CREATED]-> (b:Booking) -[:REQUIRES]-> (category:WorkfieldCategory)
            where b.date >= date() - duration('P15D')

            match (category) -[:HAS_PARENT*0..]-> (root:WorkfieldCategory)
            where not (root) -[:HAS_PARENT]-> (:WorkfieldCategory)

            match (descendant:WorkfieldCategory) -[:HAS_PARENT*0..]-> (root)
            where not (:WorkfieldCategory) -[:HAS_PARENT]-> (descendant)
            with client, collect(distinct descendant) as recommended_categories

            match (freelancer:Freelancer) -[:CAN_DO]-> (fcategory:WorkfieldCategory)
            where freelancer.city = client.city and fcategory in recommended_categories
            return collect(distinct freelancer.freelancerId) as recommended_freelancers
            limit $recommendation_limit
        """;

    private final String getBySimilarClients = 
        """
            match (client:Client { clientId:$clientId})-[:CREATED]->(:Booking)-[:REQUIRES]->(category:WorkfieldCategory)
            with distinct client, collect(distinct category) as client_categories

            match (oclient:Client)-[:CREATED]->(:Booking)-[:REQUIRES]->(shared:WorkfieldCategory)
            where oclient <> client and shared in client_categories

            match (oclient) -[:CREATED]-> (:Booking) -[:REQUIRES]-> (ocategory:WorkfieldCategory)
            where not ocategory in client_categories
            with client, collect(distinct ocategory) as recommended_categories

            match (freelancer:Freelancer) -[:CAN_DO]-> (fcategory:WorkfieldCategory)
            where freelancer.city = client.city and fcategory in recommended_categories
            return collect(distinct freelancer.freelancerId) as recommended_freelancers
            limit $recommendation_limit
        """;

    public RecommendationRepository(Driver driver) {
        this.driver = driver;
    }

    public List<String> getByWorkfieldCategory(String clientId, int limit) {
        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("recommendation_limit", limit);

        // execute statement
        List<String> freelancerIds = new ArrayList<>();

        try (Session session = driver.session()) {
            Result result = session.run(getByWorkfieldCategory, params);

            if (result.hasNext()) {
                var record = result.next();
                freelancerIds = record.get("recommended_freelancers").asList(Value::asString);
            }
        }

        return freelancerIds;
    }

    public List<String> getBySimilarClients(String clientId, int limit) {
        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("recommendation_limit", limit);

        // execute statement
        List<String> freelancerIds = new ArrayList<>();
        
        try (Session session = driver.session()) {
            Result result = session.run(getBySimilarClients, params);

            if (result.hasNext()) {
                var record = result.next();
                freelancerIds = record.get("recommended_freelancers").asList(Value::asString);
            }
        }

        return freelancerIds;
    }
}
