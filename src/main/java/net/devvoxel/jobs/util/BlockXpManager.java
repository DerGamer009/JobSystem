package net.devvoxel.jobs.util;

import net.devvoxel.jobs.JobSystemPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BlockXpManager {

    private final JobSystemPlugin plugin;
    private final Map<String, Map<Material, Double>> blockXpMap = new ConcurrentHashMap<>();

    public BlockXpManager(JobSystemPlugin plugin) {
        this.plugin = plugin;
        loadBlockXp();
    }

    public void loadBlockXp() {
        blockXpMap.clear();
        FileConfiguration config = loadBlocksFile();
        if (config == null) {
            return;
        }

        for (String jobId : config.getKeys(false)) {
            ConfigurationSection jobSection = config.getConfigurationSection(jobId);
            if (jobSection == null) {
                continue;
            }

            Map<Material, Double> materialXpMap = new HashMap<>();
            for (String materialName : jobSection.getKeys(false)) {
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    double xp = jobSection.getDouble(materialName, 0.0);
                    if (xp > 0.0) {
                        materialXpMap.put(material, xp);
                    }
                } else {
                    plugin.getLogger().warning("Unknown material in blocks.yml: " + materialName + " for job " + jobId);
                }
            }

            if (!materialXpMap.isEmpty()) {
                blockXpMap.put(jobId.toLowerCase(), materialXpMap);
            }
        }
    }

    /**
     * Gets the XP value for a specific block and job.
     * Returns null if no specific XP is configured, so the default job XP can be used.
     *
     * @param jobId The job ID
     * @param material The block material
     * @return The XP value for this block, or null if not configured
     */
    public Double getBlockXp(String jobId, Material material) {
        if (jobId == null || material == null) {
            return null;
        }
        Map<Material, Double> jobBlocks = blockXpMap.get(jobId.toLowerCase());
        if (jobBlocks == null) {
            return null;
        }
        return jobBlocks.get(material);
    }

    /**
     * Checks if a specific block has custom XP configured for a job.
     *
     * @param jobId The job ID
     * @param material The block material
     * @return true if custom XP is configured, false otherwise
     */
    public boolean hasBlockXp(String jobId, Material material) {
        return getBlockXp(jobId, material) != null;
    }

    private FileConfiguration loadBlocksFile() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.yml");
        if (!blocksFile.exists()) {
            plugin.saveResource("blocks.yml", false);
        }
        FileConfiguration blocksConfig = new YamlConfiguration();
        try {
            blocksConfig.load(blocksFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load blocks.yml", e);
            return null;
        }
        return blocksConfig;
    }

    public void saveBlocksFile(FileConfiguration config) throws IOException {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.yml");
        File parent = blocksFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        config.save(blocksFile);
    }
}

