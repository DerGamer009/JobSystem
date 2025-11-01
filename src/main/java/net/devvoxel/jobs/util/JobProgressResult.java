package net.devvoxel.jobs.util;

public record JobProgressResult(double experienceAwarded,
                                double moneyAwarded,
                                int previousLevel,
                                int newLevel,
                                double currentExperience,
                                double requiredExperience,
                                boolean leveledUp,
                                boolean maxLevelReached) {

    public double progressFraction() {
        if (maxLevelReached) {
            return 1.0;
        }
        if (requiredExperience <= 0.0) {
            return 0.0;
        }
        return Math.min(1.0, currentExperience / requiredExperience);
    }
}
