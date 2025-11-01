package net.devvoxel.jobs.command;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.JobMessageService;
import net.devvoxel.jobs.gui.JobMenu;
import net.devvoxel.jobs.util.JobDefinition;
import net.devvoxel.jobs.util.JobManager;
import net.devvoxel.jobs.util.JobPlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class JobsCommand implements TabExecutor {

    private final JobSystemPlugin plugin;
    private final JobMessageService messageService;

    public JobsCommand(JobSystemPlugin plugin) {
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messageService.format("player-only"));
                return true;
            }
            new JobMenu(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join":
                handleJoin(sender, args);
                break;
            case "unjoin":
            case "leave":
                handleUnjoin(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "reload":
                if (!sender.hasPermission("net.devvoxel.jobs.manage")) {
                    sender.sendMessage(messageService.format("no-permission"));
                    return true;
                }
                plugin.reloadServices();
                sender.sendMessage(messageService.format("reload"));
                break;
            case "create":
                handleCreate(sender, args);
                break;
            case "edit":
                handleEdit(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            default:
                sender.sendMessage(messageService.format("unknown-subcommand"));
        }
        return true;
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.format("player-only"));
            return;
        }
        if (!sender.hasPermission("net.devvoxel.jobs.command")) {
            sender.sendMessage(messageService.format("no-permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(messageService.format("usage.join"));
            return;
        }
        String jobId = args[1].toLowerCase();
        JobDefinition jobDefinition = plugin.getJobManager().getJob(jobId);
        if (jobDefinition == null) {
            sender.sendMessage(messageService.format("job-not-found"));
            return;
        }
        if (!player.hasPermission(jobDefinition.getPermission())) {
            sender.sendMessage(messageService.format("job-permission-missing", Map.of("permission", jobDefinition.getPermission())));
            return;
        }
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (jobId.equalsIgnoreCase(data.getJobId())) {
            sender.sendMessage(messageService.format("job-already-joined"));
            return;
        }
        data.setJobId(jobId);
        plugin.savePlayerData(data);
        sender.sendMessage(messageService.format("job-joined", Map.of("job_name", ChatColor.stripColor(jobDefinition.getDisplayName()))));
        if (!plugin.isStorageAvailable()) {
            sender.sendMessage(messageService.format("storage-disabled"));
        }
    }

    private void handleUnjoin(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageService.format("player-only"));
            return;
        }
        if (!sender.hasPermission("net.devvoxel.jobs.command")) {
            sender.sendMessage(messageService.format("no-permission"));
            return;
        }
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        data.setJobId(null);
        plugin.savePlayerData(data);
        sender.sendMessage(messageService.format("job-unjoined"));
        if (!plugin.isStorageAvailable()) {
            sender.sendMessage(messageService.format("storage-disabled"));
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("net.devvoxel.jobs.manage")) {
            sender.sendMessage(messageService.format("no-permission"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messageService.format("usage.create"));
            return;
        }
        String id = args[1].toLowerCase();
        JobManager manager = plugin.getJobManager();
        if (manager.getJob(id) != null) {
            sender.sendMessage(messageService.format("job-exists"));
            return;
        }
        String displayName = ChatColor.translateAlternateColorCodes('&', args[2].replace('_', ' '));
        String permission = args[3];
        JobDefinition definition = new JobDefinition(id, displayName, "", permission, org.bukkit.Material.PAPER);
        if (manager.addJob(definition)) {
            sender.sendMessage(messageService.format("job-created", Map.of("job_id", id)));
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("net.devvoxel.jobs.manage")) {
            sender.sendMessage(messageService.format("no-permission"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messageService.format("usage.edit"));
            return;
        }
        String id = args[1].toLowerCase();
        JobDefinition definition = plugin.getJobManager().getJob(id);
        if (definition == null) {
            sender.sendMessage(messageService.format("job-not-found"));
            return;
        }
        String field = args[2].toLowerCase();
        String value = Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).collect(Collectors.joining(" "));
        switch (field) {
            case "displayname":
                definition.setDisplayName(ChatColor.translateAlternateColorCodes('&', value));
                break;
            case "description":
                definition.setDescription(ChatColor.translateAlternateColorCodes('&', value));
                break;
            case "permission":
                definition.setPermission(value);
                break;
            case "icon":
                org.bukkit.Material icon = org.bukkit.Material.matchMaterial(value.toUpperCase());
                if (icon != null) {
                    definition.setIcon(icon);
                }
                break;
            default:
                sender.sendMessage(messageService.format("unknown-subcommand"));
                return;
        }
        plugin.getJobManager().updateJob(definition);
        sender.sendMessage(messageService.format("job-updated", Map.of("job_id", id, "field", field)));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("net.devvoxel.jobs.manage")) {
            sender.sendMessage(messageService.format("no-permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(messageService.format("usage.delete"));
            return;
        }
        String id = args[1].toLowerCase();
        if (plugin.getJobManager().removeJob(id)) {
            sender.sendMessage(messageService.format("job-deleted", Map.of("job_id", id)));
        } else {
            sender.sendMessage(messageService.format("job-not-found"));
        }
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(messageService.format("jobs-list-header"));
        for (JobDefinition definition : plugin.getJobManager().getJobs()) {
            sender.sendMessage(messageService.formatWithoutPrefix("job-list-entry", Map.of(
                    "job_id", definition.getId(),
                    "job_name", ChatColor.stripColor(definition.getDisplayName())
            )));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(Arrays.asList("join", "unjoin", "leave", "list"));
            if (sender.hasPermission("net.devvoxel.jobs.manage")) {
                base.addAll(Arrays.asList("create", "edit", "delete", "reload"));
            }
            return base.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return plugin.getJobManager().getJobs().stream()
                    .map(JobDefinition::getId)
                    .filter(id -> id.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && Arrays.asList("edit", "delete").contains(args[0].toLowerCase())) {
            return plugin.getJobManager().getJobs().stream()
                    .map(JobDefinition::getId)
                    .filter(id -> id.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            return Arrays.asList("displayName", "description", "permission", "icon").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
