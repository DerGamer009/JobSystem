package net.devvoxel.jobs.storage;

import net.devvoxel.jobs.util.JobPlayerData;

import java.util.UUID;

public interface JobStorage {

    void connect() throws Exception;

    void disconnect();

    JobPlayerData loadPlayer(UUID uuid);

    void savePlayer(JobPlayerData data);

    void deletePlayer(UUID uuid);

    JobStorageType getType();
}
