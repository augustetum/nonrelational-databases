package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import config.MongoDbContext;
import entity.WorkfieldCategory;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import util.IdentifierGenerator;

@Repository
public class WorkfieldCategoryRepository {
    private final MongoCollection<Document> collection;

    public WorkfieldCategoryRepository(MongoDbContext dbContext) {
        this.collection = dbContext.workfieldCategories;
    }

    public void add(WorkfieldCategory workfieldCategory){
        workfieldCategory.setId(IdentifierGenerator.generateId());
        Document workfieldCategoryDoc = workfieldCategoryToDocument(workfieldCategory);
        collection.insertOne(workfieldCategoryDoc);
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
        return documentToWorkfieldCategory(doc);
    }

    private Document workfieldCategoryToDocument(WorkfieldCategory workfieldCategory){
        Document document = new Document();
        document.append("_id", workfieldCategory.getId());
        document.append("name", workfieldCategory.getName());
        return document;
    }

    private WorkfieldCategory documentToWorkfieldCategory(Document doc) {
        WorkfieldCategory category = new WorkfieldCategory();
        category.setId(doc.getString("_id"));
        category.setName(doc.getString("name"));
        return category;
    }
}
