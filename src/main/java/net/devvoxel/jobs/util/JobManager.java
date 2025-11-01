package net.devvoxel.jobs.util;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.JobConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobManager {

    private final JobSystemPlugin plugin;
    private final Map<String, JobDefinition> jobs = new LinkedHashMap<>();
    private final Map<String, String> configuredPermissions;

    public JobManager(JobSystemPlugin plugin, JobConfig config) {
        this.plugin = plugin;
        this.configuredPermissions = config.getPermissions();
        loadJobs();
    }

    public void loadJobs() {
        jobs.clear();
        FileConfiguration config = plugin.loadJobsFile();
        if (config == null) {
            return;
        }
        if (config.isConfigurationSection("jobs")) {
            for (String id : config.getConfigurationSection("jobs").getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection("jobs." + id);
                if (section == null) {
                    continue;
                }
                String displayName = section.getString("display-name", id);
                String description = section.getString("description", "");
                String permission = section.getString("permission");
                if (permission == null || permission.isBlank()) {
                    permission = configuredPermissions.getOrDefault(id.toLowerCase(), "net.devvoxel.jobs." + id.toLowerCase());
                }
                Material icon = Material.matchMaterial(section.getString("icon", "PAPER"));
                double experiencePerAction = section.getDouble("xp-per-action", 10.0);
                double moneyPerAction = section.getDouble("money-per-action", 0.0);
                JobDefinition jobDefinition = new JobDefinition(id, displayName, description, permission, icon,
                        experiencePerAction, moneyPerAction);
                jobs.put(id.toLowerCase(), jobDefinition);
            }
        }
    }

    public boolean addJob(JobDefinition jobDefinition) {
        String key = jobDefinition.getId().toLowerCase();
        if (jobs.containsKey(key)) {
            return false;
        }
        jobs.put(key, jobDefinition);
        saveJobs();
        return true;
    }

    public boolean removeJob(String id) {
        if (jobs.remove(id.toLowerCase()) != null) {
            saveJobs();
            return true;
        }
        return false;
    }

    public JobDefinition getJob(String id) {
        return jobs.get(id.toLowerCase());
    }

    public Collection<JobDefinition> getJobs() {
        return Collections.unmodifiableCollection(jobs.values());
    }

    public void updateJob(JobDefinition jobDefinition) {
        jobs.put(jobDefinition.getId().toLowerCase(), jobDefinition);
        saveJobs();
    }

    private void saveJobs() {
        FileConfiguration config = plugin.loadJobsFile();
        config.set("jobs", null);
        for (JobDefinition definition : jobs.values()) {
            String base = "jobs." + definition.getId();
            config.set(base + ".display-name", definition.getDisplayName());
            config.set(base + ".description", definition.getDescription());
            config.set(base + ".permission", definition.getPermission());
            config.set(base + ".icon", definition.getIcon().name());
            config.set(base + ".xp-per-action", definition.getExperiencePerAction());
            config.set(base + ".money-per-action", definition.getMoneyPerAction());
        }
        try {
            plugin.saveJobsFile(config);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save jobs.yml: " + e.getMessage());
        }
    }
}
