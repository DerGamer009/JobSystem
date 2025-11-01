package net.devvoxel.jobs.util;

import java.util.UUID;

public class JobPlayerData {

    private final UUID uuid;
    private String jobId;

    public JobPlayerData(UUID uuid, String jobId) {
        this.uuid = uuid;
        this.jobId = jobId == null ? null : jobId.toLowerCase();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.toLowerCase();
    }
}
