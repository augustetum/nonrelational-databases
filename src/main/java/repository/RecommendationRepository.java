package repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendationRepository {
    private final Driver driver;

    private final String getBySkill = 
        """
            match  (client:Client { clientId:$clientId }) -[:CREATED]-> (b:Booking) -[:REQUIRES]-> (category:WorkfieldCategory)
            where b.date >= date() - duration('P15D')

            match (category) -[:HAS_PARENT*0..]-> (root:WorkfieldCategory)
            where not (root) -[:HAS_PARENT]-> (:WorkfieldCategory)

            match (descendant:WorkfieldCategory) -[:HAS_PARENT*0..]-> (root)
            where not (:WorkfieldCategory) -[:HAS_PARENT]-> (descendant)

            return collect(distinct descendant) as leaf_nodes;
        """;

    public RecommendationRepository(Driver driver) {
        this.driver = driver;
    }

    public List<String> getBySkill(String clientId) {
        // map paramenters
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);

        // execute statement
        try (Session session = driver.session()) {
            Result result = session.run(getBySkill, params);
        }

        return new ArrayList<String>();
    }
}
