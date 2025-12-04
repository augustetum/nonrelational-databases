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

    public RecommendationRepository(Driver driver) {
        this.driver = driver;
    }

    public List<String> getByWorkfieldCategory(String clientId, int limit) {
        String statement =
            """
                match  (client:Client { clientId:$clientId })-[:CREATED]->(b:Booking)-[:REQUIRES]->(category:WorkfieldCategory)
                where b.date >= date() - duration('P15D')

                match (category)-[:HAS_PARENT*0..]->(root:WorkfieldCategory)
                where not (root)-[:HAS_PARENT]->(:WorkfieldCategory)

                match (descendant:WorkfieldCategory)-[:HAS_PARENT*0..]->(root)
                where not (:WorkfieldCategory)-[:HAS_PARENT]->(descendant)
                with client, collect(distinct descendant) as recommended_categories

                match (freelancer:Freelancer)-[:CAN_DO]->(fcategory:WorkfieldCategory)
                where freelancer.city = client.city and fcategory in recommended_categories
                return collect(distinct freelancer.freelancerId) as recommended_freelancers
                limit $recommendation_limit
            """;

        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("recommendation_limit", limit);

        // execute statement
        List<String> freelancerIds = new ArrayList<>();

        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (result.hasNext()) {
                var record = result.next();
                freelancerIds = record.get("recommended_freelancers").asList(Value::asString);
            }
        }

        return freelancerIds;
    }

    public List<String> getBySimilarClients(String clientId, int limit) {
        String statement =         
            """
                match (client:Client { clientId:$clientId } )

                // get categories booked by client
                match (client)-[:CREATED]->(:Booking)-[:REQUIRES]->(category:WorkfieldCategory)
                with client, collect(distinct category) as client_categories

                // get all ancestors of client booked categories
                unwind client_categories as client_categories_unwinded
                match (client_categories_unwinded)-[:HAS_PARENT*0..]->(ancestor)
                with client, client_categories, collect(distinct ancestor) as client_ancestors

                // get categories booked by other client
                match (other:Client)
                where other <> client
                match (other)-[:CREATED]->(:Booking)-[:REQUIRES]->(otherCategory:WorkfieldCategory)
                with client, client_categories, client_ancestors, other, collect(distinct otherCategory) as other_categories

                // get all ancestors of other client booked categories
                unwind other_categories as other_categories_unwinded
                match (other_categories_unwinded)-[:HAS_PARENT*0..]->(otherAncestor)
                with client, client_categories, client_ancestors, other, other_categories, collect(distinct otherAncestor) as other_ancestors

                // calculate similarity
                with client,
                    other,
                    size([x in client_categories where x in other_categories]) as category_overlap,
                    size([a in client_ancestors where a in other_ancestors]) as ancestor_overlap,
                    client_categories, other_categories
                with client, other, category_overlap, ancestor_overlap,
                    (tofloat(category_overlap) * 0.7 + tofloat(ancestor_overlap) * 0.3) as similarity,
                    client_categories, other_categories

                where similarity > 0

                // categories booked by other clients but not by specified client
                with client, other, category_overlap, ancestor_overlap, similarity,
                    [c in other_categories where not c in client_categories] as new_categories_for_client

                // get freelancers in the same city that can do recommended categories
                unwind new_categories_for_client as recommended_category
                match (freelancer:Freelancer)-[:CAN_DO]->(fcategory:WorkfieldCategory)
                where freelancer.city = client.city and fcategory = recommended_category
                with client, other, category_overlap, ancestor_overlap, similarity, collect(distinct freelancer.freelancerId) as recommended_freelancers

                return recommended_freelancers
                limit $recommendation_limit;
            """;

        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("recommendation_limit", limit);

        // execute statement
        List<String> freelancerIds = new ArrayList<>();
        
        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (result.hasNext()) {
                var record = result.next();
                freelancerIds = record.get("recommended_freelancers").asList(Value::asString);
            }
        }

        return freelancerIds;
    }
}
