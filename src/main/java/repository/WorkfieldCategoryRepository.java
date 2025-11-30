package repository;

import com.mongodb.client.MongoCollection;
import entity.WorkfieldCategory;
import org.bson.Document;
import util.IdentifierGenerator;

public class WorkfieldCategoryRepository {
    private final MongoCollection<Document> collection;

    protected WorkfieldCategoryRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void add(WorkfieldCategory workfieldCategory){
        workfieldCategory.setId(IdentifierGenerator.generateId());
        Document workfieldCategoryDoc = workfieldCategoryToDocument(workfieldCategory);
        collection.insertOne(workfieldCategoryDoc);
    }

    public Document workfieldCategoryToDocument(WorkfieldCategory workfieldCategory){
        Document document = new Document();
        document.append("id_", workfieldCategory.getId());
        document.append("name", workfieldCategory.getName());
        return document;
    }
}
