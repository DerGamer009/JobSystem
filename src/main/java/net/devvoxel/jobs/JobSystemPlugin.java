package net.devvoxel.jobs;

import net.devvoxel.jobs.command.JobsCommand;
import net.devvoxel.jobs.config.JobConfig;
import net.devvoxel.jobs.config.JobMessageService;
import net.devvoxel.jobs.listener.JobActionListener;
import net.devvoxel.jobs.storage.JobStorage;
import net.devvoxel.jobs.storage.JobStorageFactory;
import net.devvoxel.jobs.storage.JobStorageType;
import net.devvoxel.jobs.util.JobManager;
import net.devvoxel.jobs.util.JobPlayerData;
import net.devvoxel.jobs.util.JobProgressionManager;
import net.devvoxel.jobs.util.VersionSupport;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class JobSystemPlugin extends JavaPlugin {

    private JobConfig jobConfig;
    private JobMessageService messageService;
    private JobManager jobManager;
    private JobProgressionManager progressionManager;
    private JobStorage storage;
    private final Map<UUID, JobPlayerData> localCache = new ConcurrentHashMap<>();
    private Economy economy;
    private boolean warnedEconomyMissing;

    @Override
    public void onEnable() {
        if (!VersionSupport.isSupportedServer()) {
            getLogger().severe("Unsupported Minecraft version " + Bukkit.getMinecraftVersion()
                    + ". Supported range: " + VersionSupport.getSupportedRange());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        saveResource("messages_en.yml", false);
        saveResource("messages_de.yml", false);
        saveResource("jobs.yml", false);

        setupEconomy();
        reloadServices();

        if (storage != null) {
            getLogger().info("JobSystem enabled using " + storage.getType() + " storage.");
        }

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.disconnect();
        }
    }

    public void reloadServices() {
        setupEconomy();
        reloadConfig();
        this.jobConfig = new JobConfig(getConfig());
        this.messageService = new JobMessageService(this, jobConfig.getDefaultLanguage());
        this.jobManager = new JobManager(this, jobConfig);
        this.progressionManager = new JobProgressionManager(this, jobConfig.getProgressionSettings());
        this.localCache.clear();

        setupStorage();
        warnEconomyIfMissing();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            this.economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> registration = getServer().getServicesManager().getRegistration(Economy.class);
        if (registration != null) {
            this.economy = registration.getProvider();
            this.warnedEconomyMissing = false;
        } else {
            this.economy = null;
        }
    }

    private void warnEconomyIfMissing() {
        if (economy != null || messageService == null || warnedEconomyMissing) {
            return;
        }
        String message = messageService.formatWithoutPrefix("economy-missing", Map.of());
        getLogger().warning(ChatColor.stripColor(message));
        warnedEconomyMissing = true;
    }

    private void setupStorage() {
        if (this.storage != null) {
            this.storage.disconnect();
        }

        JobStorageType type = jobConfig.getStorageType();
        try {
            this.storage = JobStorageFactory.createStorage(this, jobConfig);
            if (this.storage != null) {
                this.storage.connect();
                String message = messageService.formatWithoutPrefix("storage-connected", Map.of("storage_type", type.name()));
                getLogger().info(ChatColor.stripColor(message));
            }
        } catch (Exception ex) {
            String message = messageService.formatWithoutPrefix("storage-failed", Map.of("storage_type", type.name()));
            getLogger().severe(ChatColor.stripColor(message));
            getLogger().log(Level.SEVERE, "Failed to connect to storage backend", ex);
            if (this.storage != null) {
                this.storage.disconnect();
                this.storage = null;
            }
        }
    }

    private void registerCommands() {
        JobsCommand jobsCommand = new JobsCommand(this);
        if (getCommand("jobs") != null) {
            getCommand("jobs").setExecutor(jobsCommand);
            getCommand("jobs").setTabCompleter(jobsCommand);
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JobActionListener(this), this);
    }

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public JobMessageService getMessageService() {
        return messageService;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public JobProgressionManager getProgressionManager() {
        return progressionManager;
    }

    public JobStorage getStorage() {
        return storage;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isStorageAvailable() {
        return storage != null;
    }

    public void saveJobsFile(FileConfiguration config) throws IOException {
        File jobsFile = new File(getDataFolder(), "jobs.yml");
        File parent = jobsFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        config.save(jobsFile);
    }

    public FileConfiguration loadJobsFile() {
        File jobsFile = new File(getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            saveResource("jobs.yml", false);
        }
        FileConfiguration jobsConfig = new YamlConfiguration();
        try {
            jobsConfig.load(jobsFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Could not load jobs.yml", e);
        }
        return jobsConfig;
    }

    public JobPlayerData getPlayerData(java.util.UUID uuid) {
        JobPlayerData cached = localCache.get(uuid);
        if (cached != null) {
            return cached;
        }
        JobPlayerData data;
        if (storage != null) {
            try {
                data = storage.loadPlayer(uuid);
            } catch (Exception exception) {
                getLogger().log(Level.SEVERE, "Failed to load data for " + uuid + " from storage", exception);
                data = null;
            }
            if (data == null) {
                data = new JobPlayerData(uuid, null);
            }
        } else {
            data = new JobPlayerData(uuid, null);
        }
        localCache.put(uuid, data);
        return data;
    }

    public void savePlayerData(JobPlayerData data) {
        localCache.put(data.getUuid(), data);
        if (storage != null) {
            try {
                storage.savePlayer(data);
            } catch (Exception exception) {
                getLogger().log(Level.SEVERE, "Failed to persist data for " + data.getUuid() + " to storage", exception);
            }
        }
    }

    public void clearCachedPlayer(UUID uuid) {
        localCache.remove(uuid);
    }
}
