import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PreDestroy;

@Component
public class MongoDbContext {
    private final MongoClient client;
    private final MongoDatabase database;

    public final MongoCollection<Document> bookings;
    public final MongoCollection<Document> clients;
    public final MongoCollection<Document> freelancers;

    public MongoDbContext(@Value("${spring.data.mongodb.uri}") String uri, @Value("${spring.data.mongodb.database}") String dbName) {
        System.out.println("ctx initialized");

        this.client = MongoClients.create(uri);
        this.database = client.getDatabase(dbName);

        this.bookings = database.getCollection("bookings");
        this.clients = database.getCollection("clients");
        this.freelancers = database.getCollection("freelancers");
    }

    @PreDestroy
    public void close() {
        client.close();
        System.out.println("ctx destroyed");
    }
}
