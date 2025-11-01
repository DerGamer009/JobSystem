package net.devvoxel.jobs.config;

public class ProgressionSettings {

    private final double baseExperience;
    private final double experienceIncrement;
    private final int maxLevel;
    private final int progressBarLength;

    public ProgressionSettings(double baseExperience, double experienceIncrement, int maxLevel, int progressBarLength) {
        this.baseExperience = Math.max(1.0, baseExperience);
        this.experienceIncrement = Math.max(0.0, experienceIncrement);
        this.maxLevel = Math.max(0, maxLevel);
        this.progressBarLength = Math.max(1, progressBarLength);
    }

    public double getBaseExperience() {
        return baseExperience;
    }

    public double getExperienceIncrement() {
        return experienceIncrement;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean hasMaxLevel() {
        return maxLevel > 0;
    }

    public int getProgressBarLength() {
        return progressBarLength;
    }

    public double getRequiredExperienceForLevel(int level) {
        int effectiveLevel = Math.max(1, level);
        return baseExperience + (effectiveLevel - 1) * experienceIncrement;
    }
}
