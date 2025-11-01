package net.devvoxel.jobs.storage.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.storage.JobStorage;
import net.devvoxel.jobs.storage.JobStorageType;
import net.devvoxel.jobs.util.JobPlayerData;
import org.bson.Document;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class MongoJobStorage implements JobStorage {

    private final JobSystemPlugin plugin;
    private final ConfigurationSection settings;
    private MongoClient client;
    private MongoCollection<Document> collection;

    public MongoJobStorage(JobSystemPlugin plugin, ConfigurationSection settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void connect() throws Exception {
        if (settings == null) {
            throw new IllegalStateException("MongoDB settings missing in config.yml");
        }
        String connectionString = settings.getString("connection-string", "mongodb://localhost:27017");
        this.client = MongoClients.create(connectionString);
        MongoDatabase database = client.getDatabase(settings.getString("database", "jobsystem"));
        this.collection = database.getCollection(settings.getString("collection", "player_jobs"));
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public JobPlayerData loadPlayer(UUID uuid) {
        Document document = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document != null) {
            int level = document.getInteger("job_level", 1);
            Number experienceNumber = document.get("job_experience", Number.class);
            double experience = experienceNumber == null ? 0.0 : experienceNumber.doubleValue();
            return new JobPlayerData(uuid, document.getString("job_id"), level, experience);
        }
        return new JobPlayerData(uuid, null);
    }

    @Override
    public void savePlayer(JobPlayerData data) {
        Document document = new Document("uuid", data.getUuid().toString())
                .append("job_id", data.getJobId())
                .append("job_level", data.getLevel())
                .append("job_experience", data.getExperience());
        collection.replaceOne(Filters.eq("uuid", data.getUuid().toString()), document, new ReplaceOptions().upsert(true));
    }

    @Override
    public void deletePlayer(UUID uuid) {
        collection.deleteOne(Filters.eq("uuid", uuid.toString()));
    }

    @Override
    public JobStorageType getType() {
        return JobStorageType.MONGODB;
    }
}
