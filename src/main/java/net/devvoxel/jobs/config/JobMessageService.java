package net.devvoxel.jobs.config;

import net.devvoxel.jobs.JobSystemPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JobMessageService {

    private final JobSystemPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    private final String language;

    public JobMessageService(JobSystemPlugin plugin, String language) {
        this.plugin = plugin;
        this.language = language.toLowerCase();
        loadMessages();
    }

    private void loadMessages() {
        File messageFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(messageFile);
            if (config.isConfigurationSection("messages")) {
                for (String key : config.getConfigurationSection("messages").getKeys(true)) {
                    messages.put(key, ChatColor.translateAlternateColorCodes('&', config.getString("messages." + key, key)));
                }
            }
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            plugin.getLogger().severe("Unable to load messages for language " + language + ": " + e.getMessage());
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String format(String key) {
        return format(key, java.util.Collections.emptyMap());
    }

    public String format(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.getJobConfig().getPrefix() + message);
    }

    public String formatRaw(String message) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getJobConfig().getPrefix() + message);
    }

    public String formatWithoutPrefix(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
