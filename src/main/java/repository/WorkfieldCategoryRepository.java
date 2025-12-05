package repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import entity.WorkfieldCategory;
import util.IdentifierGenerator;
import util.mappers.WorkfieldCategoryMapper;

@Repository
public class WorkfieldCategoryRepository {
    private final Driver driver;

    public WorkfieldCategoryRepository(Driver driver) {
        this.driver = driver;      
    }

    public WorkfieldCategory getById(String id) {
        String statement =
            """
                match (n:WorkfieldCategory { id: $id })
                return n        
            """;

        // put parameters
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        // execute statement
        Node node = null;

        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (!result.hasNext()) {
                return null;
            }

            // parse result
            org.neo4j.driver.Record record = result.next();
            node = record.get("node_exists").asNode();

            return WorkfieldCategoryMapper.toWorkfieldCategory(node);
        }
    }

    public List<WorkfieldCategory> getAncestry(String id) {
        String statement = 
            """
                match (n:WorkfieldCategory {id: $id})
                match (n) -[:HAS_PARENT*0..]-> (p:WorkfieldCategory)
                RETURN collect(p) AS ancestry
            """;

        // put arguments
        Map<String, Object> params = Map.of("id", id);

        // execute statement
        try (Session session = driver.session()) {

            Result result = session.run(statement, params);

            if (!result.hasNext()) {
                return List.of();
            }

            org.neo4j.driver.Record record = result.next();
            
            // parse list of categories
            List<Node> nodes = record.get("ancestry").asList(v -> v.asNode());
            return nodes.stream().map(WorkfieldCategoryMapper::toWorkfieldCategory).toList();
        }
    }

    public Boolean existsById(String id) {
        String statement =
            """
                match (n:WorkfieldCategory { id: $id })
                return count(n) > 0 as node_exists;
            """;
        
        // put parameters
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        // execute statement
        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (!result.hasNext()) {
                return null;
            }

            // parse result
            org.neo4j.driver.Record record = result.next();
            return record.get("node_exists").asBoolean();
        }
    }

    public Boolean isDirectChildOf(String name, String parentId) {
        String statement = 
            """
                match (child:WorkfieldCategory { name: $name }) -[:HAS_PARENT]-> (parent:WorkfieldCategory { id: $parentId })
                return count(*) > 0 as is_child;
            """;

        // put parameters
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("parentId", parentId);

        // execute statement
        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (!result.hasNext()) {
                return null;
            }

            // parse result
            org.neo4j.driver.Record record = result.next();
            return record.get("is_child").asBoolean();
        }
    }

    public Boolean isChildFree(String id) {
        String statement =
            """
                match (n:WorkfieldCategory {id: $id})
                return not exists ((n) -[:HAS_CHILD]-> ()) AS is_leaf_node
            """;
        
        // put parameters
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        // execute statement
        Boolean isLeafNode = null;

        try (Session session = driver.session()) {
            Result result = session.run(statement, params);

            if (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                isLeafNode = record.get("is_leaf_node").asBoolean();
            }
        }

        return isLeafNode;
    }

    public void add(String name, String parentId) {
        String statement = 
            """
                create (child:WorkfieldCategory { id: $categoryId, name: $name })
                with child
                match (parent:WorkfieldCategory { id: $parentId })
                create (child) -[:HAS_PARENT]-> (parent)
            """;

        // put parameters
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", IdentifierGenerator.generateId());
        params.put("name", name);
        params.put("parentId", parentId);

        // execute statement
        try (Session session = driver.session()) {
            session.run(statement, params);
        }
    }
}
