package net.devvoxel.jobs.util;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.ProgressionSettings;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class JobProgressionManager {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat EXPERIENCE_FORMAT = new DecimalFormat("#,##0.##");

    private final JobSystemPlugin plugin;
    private final ProgressionSettings settings;

    public JobProgressionManager(JobSystemPlugin plugin, ProgressionSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public JobProgressResult reward(Player player, JobDefinition jobDefinition) {
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data.getJobId() == null || !data.getJobId().equalsIgnoreCase(jobDefinition.getId())) {
            return null;
        }

        int previousLevel = data.getLevel();
        int level = previousLevel;
        double experience = data.getExperience();
        boolean leveledUp = false;
        boolean maxLevelReached = settings.hasMaxLevel() && level >= settings.getMaxLevel();

        double experienceAwarded = jobDefinition.getExperiencePerAction();
        if (!maxLevelReached) {
            experience += experienceAwarded;
            double required = settings.getRequiredExperienceForLevel(level);
            while (experience >= required && (!settings.hasMaxLevel() || level < settings.getMaxLevel())) {
                experience -= required;
                level++;
                leveledUp = true;
                if (settings.hasMaxLevel() && level >= settings.getMaxLevel()) {
                    maxLevelReached = true;
                    experience = 0.0;
                    break;
                }
                required = settings.getRequiredExperienceForLevel(level);
            }
        } else {
            experienceAwarded = 0.0;
        }

        data.setLevel(level);
        data.setExperience(experience);
        plugin.savePlayerData(data);

        double requiredExperience = settings.getRequiredExperienceForLevel(level);
        if (maxLevelReached) {
            experience = requiredExperience;
        }

        double moneyAwarded = 0.0;
        Economy economy = plugin.getEconomy();
        double configuredReward = jobDefinition.getMoneyPerAction();
        if (economy != null && configuredReward > 0) {
            economy.depositPlayer(player, configuredReward);
            moneyAwarded = configuredReward;
        }

        return new JobProgressResult(experienceAwarded, moneyAwarded, previousLevel, level,
                experience, requiredExperience, leveledUp, maxLevelReached);
    }

    public String createProgressBar(double progress) {
        int segments = settings.getProgressBarLength();
        int completed = (int) Math.round(progress * segments);
        completed = Math.max(0, Math.min(segments, completed));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            if (i < completed) {
                builder.append("§a|");
            } else {
                builder.append("§7|");
            }
        }
        return builder.toString();
    }

    public ProgressionSettings getSettings() {
        return settings;
    }

    public String formatMoney(double amount) {
        Economy economy = plugin.getEconomy();
        if (economy != null) {
            return economy.format(amount);
        }
        return MONEY_FORMAT.format(amount);
    }

    public String formatExperience(double amount) {
        return EXPERIENCE_FORMAT.format(amount);
    }
}
