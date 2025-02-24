package de.dergamer09.jobSystem.Commands;

import de.dergamer09.jobSystem.JobSystem;
import de.dergamer09.jobSystem.Listeners.JobListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobCommand implements CommandExecutor
{

    private final JobSystem plugin;

    public JobCommand(JobSystem plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            new JobListener(plugin).openJobGUI(player);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern verwendet werden.");
        return false;
    }
}
