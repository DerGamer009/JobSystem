package net.devvoxel.jobs.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.storage.JobStorage;
import net.devvoxel.jobs.storage.JobStorageType;
import net.devvoxel.jobs.util.JobPlayerData;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MysqlJobStorage implements JobStorage {

    private final JobSystemPlugin plugin;
    private final ConfigurationSection settings;
    private HikariDataSource dataSource;

    public MysqlJobStorage(JobSystemPlugin plugin, ConfigurationSection settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void connect() throws Exception {
        if (settings == null) {
            throw new IllegalStateException("MySQL settings missing in config.yml");
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + settings.getString("host", "localhost") + ":" + settings.getInt("port", 3306) + "/" + settings.getString("database", "jobsystem") + "?useSSL=false&autoReconnect=true");
        config.setUsername(settings.getString("username", "root"));
        config.setPassword(settings.getString("password", ""));
        config.setMaximumPoolSize(settings.getInt("pool-size", 10));
        config.setPoolName("JobSystem-MySQL");
        this.dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS jobs_players (uuid VARCHAR(36) PRIMARY KEY, job_id VARCHAR(64), job_level INT NOT NULL DEFAULT 1, job_experience DOUBLE NOT NULL DEFAULT 0)")) {
            statement.executeUpdate();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement addLevel = connection.prepareStatement("ALTER TABLE jobs_players ADD COLUMN job_level INT NOT NULL DEFAULT 1")) {
            addLevel.executeUpdate();
        } catch (SQLException ignored) {
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement addExperience = connection.prepareStatement("ALTER TABLE jobs_players ADD COLUMN job_experience DOUBLE NOT NULL DEFAULT 0")) {
            addExperience.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public JobPlayerData loadPlayer(UUID uuid) {
        String sql = "SELECT job_id, job_level, job_experience FROM jobs_players WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new JobPlayerData(
                            uuid,
                            resultSet.getString("job_id"),
                            resultSet.getInt("job_level"),
                            resultSet.getDouble("job_experience")
                    );
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to load job data for " + uuid + ": " + exception.getMessage());
        }
        return new JobPlayerData(uuid, null);
    }

    @Override
    public void savePlayer(JobPlayerData data) {
        String sql = "REPLACE INTO jobs_players(uuid, job_id, job_level, job_experience) VALUES(?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getUuid().toString());
            if (data.getJobId() == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, data.getJobId());
            }
            statement.setInt(3, data.getLevel());
            statement.setDouble(4, data.getExperience());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to save job data for " + data.getUuid() + ": " + exception.getMessage());
        }
    }

    @Override
    public void deletePlayer(UUID uuid) {
        String sql = "DELETE FROM jobs_players WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete job data for " + uuid + ": " + exception.getMessage());
        }
    }

    @Override
    public JobStorageType getType() {
        return JobStorageType.MYSQL;
    }
}
