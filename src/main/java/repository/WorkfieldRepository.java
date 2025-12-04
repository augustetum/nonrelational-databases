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
import util.mappers.WorkfieldMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
public class WorkfieldRepository {
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> workfieldCollection;
    private final Neo4JRepository neo4JRepository;

    public WorkfieldRepository(MongoDbContext dbContext, Neo4JRepository neo4JRepository) {
        this.collection = dbContext.freelancers;
        this.workfieldCollection = dbContext.workfieldCategories;
        this.neo4JRepository = neo4JRepository;
    }

    public List<Workfield> getAllWorkfields() {
        List<Document> allFreelancers = collection.find().into(new ArrayList<>());

        return allFreelancers.stream()
                .flatMap(freelancerDoc -> {
                    List<Document> workfieldDocs = freelancerDoc.getList("workfields", Document.class);
                    return workfieldDocs == null ? Stream.<Document>of() : workfieldDocs.stream();
                })
                .map(workfield -> WorkfieldMapper.toWorkfield(workfield))
                .toList();
    }

    public List<Workfield> getWorkfieldsByFreelancerId(String freelancerId) {
        Bson filter = Filters.eq("_id", freelancerId);
        Document freelancerDoc = collection.find(filter).first();
        
        if (freelancerDoc == null) {
            return List.of();
        }

        List<Document> workfields = freelancerDoc.getList("workfields", Document.class);
        if (workfields == null) {
            return List.of();
        }

        return workfields.stream()
                .map(workfield -> WorkfieldMapper.toWorkfield(workfield))
                .toList();
    }

    //Patvarkyta (tikiuosi)
    public List<Workfield> getAllWorkfieldsByCategory(String categoryId) {
         Bson filter = Filters.eq("workfields.categoryId", categoryId);
         List<Document> freelancerDocs = collection.find(filter).into(new ArrayList<>());

         return freelancerDocs.stream()
                 .flatMap(freelancerDoc -> {
                     List<Document> workfieldDocs = freelancerDoc.getList("workfields", Document.class);
                     return workfieldDocs == null ? Stream.<Document>of() : workfieldDocs.stream();
                 })
                 .map(workfield -> WorkfieldMapper.toWorkfield(workfield))
                 .filter(workfield -> categoryId.equals(workfield.getCategoryId()))
                 .toList();
    }

    // TODO: make get by id
    //public List<Workfield> getAllWorkfieldsByCategoryByFreelancerId(String freelancerId, WorkfieldCategory category) {
    //     List<Workfield> freelancerWorkfields = getWorkfieldsByFreelancerId(freelancerId);

    //     return freelancerWorkfields.stream()
    //             .filter(workfield -> category.equals(workfield.getCategory()))
    //             .toList();
    // }

    public void addWorkfield(String freelancerId, Workfield workfield) {
        workfield.setId(IdentifierGenerator.generateId());
        Document workfieldDoc = WorkfieldMapper.toDocument(workfield);

        Bson filter = Filters.eq("_id", freelancerId);
        Bson update = new Document("$push", new Document("workfields", workfieldDoc));
        neo4JRepository.addWorkfieldForFreelancer(freelancerId, workfield.getCategoryId());
        collection.updateOne(filter, update);
    }

    public void editWorkfield(String freelancerId, String workfieldId, EditWorkfieldDto dto) {
        Bson filter = Filters.and(
                Filters.eq("_id", freelancerId),
                Filters.eq("workfields.id", workfieldId));

        Bson update = new Document("$set", new Document()
                .append("workfields.$.category", dto.getCategoryId())
                .append("workfields.$.description", dto.getDescription())
                .append("workfields.$.hourlyRate", dto.getHourlyRate()));

        collection.updateOne(filter, update);
    }

    public void deleteWorkfield(String freelancerId, String workfieldId) {
        Bson filter = Filters.eq("_id", freelancerId);
        Bson update = new Document("$pull", new Document("workfields", new Document("id", workfieldId)));
        collection.updateOne(filter, update);
    }
}
