package util.mappers;

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
}
