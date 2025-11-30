package repository;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import config.MongoDbContext;
import entity.WorkfieldCategory;
import util.IdentifierGenerator;
import util.mappers.WorkfieldCategoryMapper;

@Repository
public class WorkfieldCategoryRepository {
    private final MongoCollection<Document> collection;
    private final Driver driver;

    private final String insertWorkfieldCategoryNeo;

    public WorkfieldCategoryRepository(MongoDbContext dbContext, Driver driver) {
        this.collection = dbContext.workfieldCategories;
        this.driver = driver;        

        this.insertWorkfieldCategoryNeo = 
            """
                create (child:WorkfieldCategory { id:$categoryId, name:$name })
                with child
                match (parent:WorkfieldCategory { id:$parentId })
                create (child) -[:HAS_PARENT]-> (parent)
            """;
    }

    public void add(WorkfieldCategory category) {
        addToMongo(category);
        addToNeo(category);
    }

    private void addToMongo(WorkfieldCategory category){
        category.setCategoryId(IdentifierGenerator.generateId());
        Document document = WorkfieldCategoryMapper.toDocument(category);
        
        collection.insertOne(document);
    }

    private void addToNeo(WorkfieldCategory category) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", category.getCategoryId());
        params.put("parentId", category.getParentId());
        params.put("name", category.getName());

        try (Session session = driver.session()) {
            session.run(insertWorkfieldCategoryNeo, params);
        }
    }

    public boolean existsByName(String name) {
        return collection.countDocuments(Filters.eq("name", name)) > 0;
    }

    public boolean existsById(String id) {
        return collection.countDocuments(Filters.eq("_id", id)) > 0;
    }

    public WorkfieldCategory findById(String id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        
        if (doc == null) {
            return null;
        }

        return WorkfieldCategoryMapper.toWorkfieldCategory(doc);
    }
}
