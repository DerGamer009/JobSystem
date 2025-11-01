package net.devvoxel.jobs.storage;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.JobConfig;
import net.devvoxel.jobs.config.StorageSettings;
import net.devvoxel.jobs.storage.impl.MongoJobStorage;
import net.devvoxel.jobs.storage.impl.MysqlJobStorage;
import net.devvoxel.jobs.storage.impl.SqliteJobStorage;

public class JobStorageFactory {

    private JobStorageFactory() {
    }

    public static JobStorage createStorage(JobSystemPlugin plugin, JobConfig config) {
        JobStorageType type = config.getStorageType();
        StorageSettings settings = config.getStorageSettings();
        return switch (type) {
            case MYSQL -> new MysqlJobStorage(plugin, settings.getMysql());
            case SQLITE -> new SqliteJobStorage(plugin, settings.getSqlite());
            case MONGODB -> new MongoJobStorage(plugin, settings.getMongodb());
        };
    }
}
