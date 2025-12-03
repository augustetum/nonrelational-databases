package util.mappers;

import org.bson.Document;
import org.neo4j.driver.types.Node;

import entity.WorkfieldCategory;

public class WorkfieldCategoryMapper {
    public static WorkfieldCategory toWorkfieldCategory(Node node) {
        WorkfieldCategory category = new WorkfieldCategory();
        
        // id
        String id = node.get("id").asString();
        category.setCategoryId(id);
        
        // name
        String name = node.get("name").asString();
        category.setName(name);
        
        return category;
    }

    public static Document toDocument(WorkfieldCategory category) {
        Document document = new Document();

        // category id
        String categoryId = category.getCategoryId();
        document.append("_id", categoryId);

        // parent id
        String parentId = category.getParentId();
        document.append("parentId", parentId);

        // name
        String name = category.getName();
        document.append("name", name);

        return document;
    }

    public static WorkfieldCategory toWorkfieldCategory(Document document) {
        WorkfieldCategory category = new WorkfieldCategory();

        // category id
        String categoryId = document.getString("_id");
        category.setCategoryId(categoryId);

        // parent id
        String parentId = document.getString("parentId");
        category.setParentId(parentId);

        // name
        String name = document.getString("name");
        category.setName(name);

        return category;
    }
}
