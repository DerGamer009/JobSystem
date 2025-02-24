package de.dergamer09.jobSystem;

import de.dergamer09.jobSystem.Commands.JobCommand;
import de.dergamer09.jobSystem.Listeners.JobListener;
import de.dergamer09.jobSystem.Listeners.JobPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public final class JobSystem extends JavaPlugin
{

    private Connection connection;
    private final HashMap<UUID, String> playerJobs = new HashMap<>();
    private String prefix;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadCnofigValues();
        getLogger().info(prefix + " Plugin has been enabled!");
        Bukkit.getPluginManager().registerEvents(new JobListener(this), this);
        getCommand("jobs").setExecutor(new JobCommand(this));
        connectToDatabase();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
        {
            new JobPlaceholder(this).register();
        }
    }

    @Override
    public void onDisable()
    {
        getLogger().info(prefix + " Plugin has been disabled!");
        disconnectDatabase();
    }

    private void  loadCnofigValues()
    {
        FileConfiguration config = getConfig();
        prefix = config.getString("prefix", "[JobSystem]");
    }

    private void connectToDatabase()
    {
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/database.db");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS jobs (uuid TEXT PRIMARY KEY, job TEXT)");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void disconnectDatabase()
    {
        try {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void setPlayerJob(UUID uuid, String job)
    {
        try {
            var statement = connection.prepareStatement("REPLACE INTO jobs (uuid, job) VALUES (?, ?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, job);
            statement.executeUpdate();
            playerJobs.put(uuid, job);
        }  catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public String getPlayerJob(UUID uuid)
    {
        try {
            var statement = connection.prepareStatement("SELECT job FROM jobs WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            var resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getString("job");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return "Arbeitslos";
    }
}
