package net.devvoxel.jobs.config;

import net.devvoxel.jobs.storage.JobStorageType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class JobConfig {

    private final FileConfiguration config;

    public JobConfig(FileConfiguration config) {
        this.config = config;
    }

    public String getPrefix() {
        return config.getString("prefix", "");
    }

    public String getDefaultLanguage() {
        return config.getString("default-language", "en");
    }

    public JobStorageType getStorageType() {
        String type = config.getString("storage.type", "SQLITE");
        return JobStorageType.valueOf(type.toUpperCase());
    }

    public Map<String, String> getPermissions() {
        Map<String, String> permissions = new HashMap<>();
        if (config.isConfigurationSection("permissions")) {
            for (String key : config.getConfigurationSection("permissions").getKeys(false)) {
                permissions.put(key.toLowerCase(), config.getString("permissions." + key));
            }
        }
        return permissions;
    }

    public StorageSettings getStorageSettings() {
        return new StorageSettings(
                config.getConfigurationSection("storage.mysql"),
                config.getConfigurationSection("storage.sqlite"),
                config.getConfigurationSection("storage.mongodb")
        );
    }

    public GuiSettings getGuiSettings() {
        return new GuiSettings(
                config.getString("gui.title", "Jobs"),
                config.getInt("gui.size", 45),
                config.getInt("gui.open-animation-delay", 4),
                config.getString("gui.decorative-material", "GRAY_STAINED_GLASS_PANE")
        );
    }

    public ActionFeedbackSettings getActionFeedbackSettings() {
        return new ActionFeedbackSettings(
                config.getBoolean("action-feedback.enabled", true),
                config.getString("action-feedback.sound", "ENTITY_EXPERIENCE_ORB_PICKUP"),
                (float) config.getDouble("action-feedback.volume", 0.5),
                (float) config.getDouble("action-feedback.pitch", 1.5)
        );
    }

    public ProgressionSettings getProgressionSettings() {
        return new ProgressionSettings(
                config.getDouble("progression.base-xp", 100.0),
                config.getDouble("progression.xp-increment", 50.0),
                config.getInt("progression.max-level", 50),
                config.getInt("progression.progress-bar-length", 20)
        );
    }

    public boolean isAutoJoinEnabled() {
        return config.getBoolean("auto-join.enabled", false);
    }

    public boolean isAutoLeaveEnabled() {
        return config.getBoolean("auto-leave.enabled", false);
    }
}
