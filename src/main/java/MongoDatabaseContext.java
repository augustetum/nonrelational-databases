import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Component
public class MongoDatabaseContext {
    private final MongoClient client;
    private final MongoDatabase database;

    public final MongoCollection<Document> bookings;
    public final MongoCollection<Document> clients;
    public final MongoCollection<Document> freelancers;

    public MongoDatabaseContext(@Value("${spring.data.mongodb.uri}") String uri, @Value("${spring.data.mongodb.database}") String dbName) {
        this.client = MongoClients.create(uri);
        this.database = client.getDatabase(dbName);

        this.bookings = database.getCollection("bookings");
        this.clients = database.getCollection("clients");
        this.freelancers = database.getCollection("freelancers");
    }
}
