package util.mappers;

import org.bson.Document;

import entity.Workfield;

public class WorkfieldMapper {
    public static Workfield toWorkfield(Document document) {
        Workfield workfield = new Workfield();

        // id
        String id = document.getString("id");
        workfield.setId(id);

        // category id
        String categoryId = document.getString("categoryId");
        workfield.setCategoryId(categoryId);

        // description
        String description = document.getString("description");
        workfield.setDescription(description);

        // hourly rate
        Integer hourlyRate = document.getInteger("hourlyRate");
        workfield.setHourlyRate(hourlyRate);

        return workfield;
    }

    public static Document toDocument(Workfield workfield) {
        return new Document()
            .append("id", workfield.getId())
            .append("categoryId", workfield.getCategoryId())
            .append("description", workfield.getDescription())
            .append("hourlyRate", workfield.getHourlyRate());
    }
}
