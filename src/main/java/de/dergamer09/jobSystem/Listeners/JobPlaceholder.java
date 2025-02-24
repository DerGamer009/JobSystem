package de.dergamer09.jobSystem.Listeners;

import de.dergamer09.jobSystem.JobSystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class JobPlaceholder extends PlaceholderExpansion
{

    private final JobSystem plugin;

    public JobPlaceholder(JobSystem plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier()
    {
        return "jobsystem";
    }
    @Override
    public String getAuthor()
    {
        return "dergamer09";
    }

    @Override
    public String getVersion()
    {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier)
    {
        if (player == null)
        {
            return "";
        }

        if (identifier.equals("job"))
        {
            return plugin.getPlayerJob(player.getUniqueId());
        }

        return null;
    }


}
