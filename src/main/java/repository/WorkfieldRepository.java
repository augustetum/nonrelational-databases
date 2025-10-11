package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import config.MongoDbContext;
import dto.EditWorkfieldDto;
import entity.Workfield;
import entity.WorkfieldCategory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import util.IdentifierGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
public class WorkfieldRepository {
    private final MongoCollection<Document> collection;

    public WorkfieldRepository(MongoDbContext dbContext){this.collection = dbContext.freelancers; }

    public List<Workfield> getAllWorkfields() {
        List<Document> allFreelancers = collection.find().into(new ArrayList<>()); //visi freelanceriai

        return allFreelancers.stream() //list'a convertina i stream
                .flatMap(freelancerDoc -> {
                    List<Document> workfieldDocs = freelancerDoc.getList("workfields", Document.class);
                    return workfieldDocs == null ?
                            Stream.<Document>of() : workfieldDocs.stream();
                })
                .map(this::convertDocumentToWorkfield)
                .toList();
    }

    public List<Workfield> getWorkfieldsByFreelancerId(String freelancerId) {
        Bson filter = Filters.eq("_id", freelancerId);
        Document freelancerDoc = collection.find(filter).first(); //nes tik vieno freelancer reikia
        if (freelancerDoc == null) {
            return new ArrayList<>();
        } //jei tuščias
        List<Document> workfields = freelancerDoc.getList("workfields", Document.class);
        if (workfields == null) {
            return new ArrayList<>();
        }
        return workfields.stream()
                .map(this::convertDocumentToWorkfield)
                .toList();
    }

    public List<Workfield> getAllWorkfieldsByCategory(WorkfieldCategory category) {
        Bson filter = Filters.eq("workfields.category", category.name());
        List<Document> freelancerDocs = collection.find(filter).into(new ArrayList<>());

        return freelancerDocs.stream()
                .flatMap(freelancerDoc -> {
                    List<Document> workfieldDocs = freelancerDoc.getList("workfields", Document.class);
                    return workfieldDocs == null ?
                            Stream.<Document>of() :
                            workfieldDocs.stream();
                })
                .map(this::convertDocumentToWorkfield)
                .filter(workfield -> category.equals(workfield.getCategory()))
                .toList();
    }

    public List<Workfield> getAllWorkfieldsByCategoryByFreelancerId(String freelancerId, WorkfieldCategory category) {
        List<Workfield> freelancerWorkfields = getWorkfieldsByFreelancerId(freelancerId);

        return freelancerWorkfields.stream()
                .filter(workfield -> category.equals(workfield.getCategory()))
                .toList();
    }

    public void addWorkfield(String freelancerId, Workfield workfield){
        workfield.setId(IdentifierGenerator.generateId());
        Document workfieldDoc = convertWorkfieldToDocument(workfield);

        Bson filter = Filters.eq("_id", freelancerId);
        Bson update = new Document("$push", new Document("workfields", workfieldDoc));
        collection.updateOne(filter, update);
    }

    public void editWorkfield(String freelancerId, String workfieldId, EditWorkfieldDto dto){
        Bson filter = Filters.and(
                Filters.eq("_id", freelancerId),
                Filters.eq("workfields.id", workfieldId)
        );

        Bson update = new Document("$set", new Document()
                .append("workfields.$.category", dto.getCategory().name())
                .append("workfields.$.description", dto.getDescription())
                .append("workfields.$.hourlyRate", dto.getHourlyRate())
        );

        collection.updateOne(filter, update);
    }

    public void deleteWorkfield(String freelancerId, String workfieldId){
        Bson filter = Filters.eq("_id", freelancerId);
        Bson update = new Document("$pull", new Document("workfields", new Document("id", workfieldId)));
        collection.updateOne(filter, update);
    }

    private Document convertWorkfieldToDocument(Workfield workfield) {
        return new Document()
                .append("id", workfield.getId())
                .append("category", workfield.getCategory().name())
                .append("description", workfield.getDescription())
                .append("hourlyRate", workfield.getHourlyRate());
    }

    private Workfield convertDocumentToWorkfield(Document doc) {
        Workfield workfield = new Workfield();
        workfield.setId(doc.getString("id"));
        workfield.setCategory(WorkfieldCategory.valueOf(doc.getString("category")));
        workfield.setDescription(doc.getString("description"));
        workfield.setHourlyRate(doc.getInteger("hourlyRate"));
        return workfield;
    }
}
