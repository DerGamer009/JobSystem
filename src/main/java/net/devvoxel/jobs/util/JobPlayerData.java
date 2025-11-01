package net.devvoxel.jobs.util;

import java.util.UUID;

public class JobPlayerData {

    private final UUID uuid;
    private String jobId;
    private int level;
    private double experience;

    public JobPlayerData(UUID uuid, String jobId) {
        this(uuid, jobId, 1, 0.0);
    }

    public JobPlayerData(UUID uuid, String jobId, int level, double experience) {
        this.uuid = uuid;
        this.jobId = jobId == null ? null : jobId.toLowerCase();
        this.level = Math.max(1, level);
        this.experience = Math.max(0.0, experience);
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0.0, experience);
    }

    public void resetProgress() {
        this.level = 1;
        this.experience = 0.0;
    }
}
