package net.devvoxel.jobs.storage.impl;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.storage.JobStorage;
import net.devvoxel.jobs.storage.JobStorageType;
import net.devvoxel.jobs.util.JobPlayerData;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SqliteJobStorage implements JobStorage {

    private final JobSystemPlugin plugin;
    private final ConfigurationSection settings;
    private Connection connection;

    public SqliteJobStorage(JobSystemPlugin plugin, ConfigurationSection settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void connect() throws Exception {
        if (settings == null) {
            throw new IllegalStateException("SQLite settings missing in config.yml");
        }
        File databaseFile = new File(plugin.getDataFolder(), settings.getString("file", "jobs.db"));
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS jobs_players (uuid TEXT PRIMARY KEY, job_id TEXT)");
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public JobPlayerData loadPlayer(UUID uuid) {
        String sql = "SELECT job_id FROM jobs_players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new JobPlayerData(uuid, resultSet.getString("job_id"));
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to load job data for " + uuid + ": " + exception.getMessage());
        }
        return new JobPlayerData(uuid, null);
    }

    @Override
    public void savePlayer(JobPlayerData data) {
        String sql = "REPLACE INTO jobs_players(uuid, job_id) VALUES(?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getUuid().toString());
            if (data.getJobId() == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, data.getJobId());
            }
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to save job data for " + data.getUuid() + ": " + exception.getMessage());
        }
    }

    @Override
    public void deletePlayer(UUID uuid) {
        String sql = "DELETE FROM jobs_players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete job data for " + uuid + ": " + exception.getMessage());
        }
    }

    @Override
    public JobStorageType getType() {
        return JobStorageType.SQLITE;
    }
}
