package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import config.MongoDbContext;
import entity.Workfield;
import entity.WorkfieldCategory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

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



    private Workfield convertDocumentToWorkfield(Document doc) {
        Workfield workfield = new Workfield();
        workfield.setId(doc.getString("id"));
        workfield.setCategory(WorkfieldCategory.valueOf(doc.getString("category")));
        workfield.setDescription(doc.getString("description"));
        workfield.setHourlyRate(doc.getInteger("number"));
        return workfield;
    }
}
