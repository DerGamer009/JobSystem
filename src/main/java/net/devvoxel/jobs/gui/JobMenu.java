package net.devvoxel.jobs.gui;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.GuiSettings;
import net.devvoxel.jobs.config.JobMessageService;
import net.devvoxel.jobs.util.JobDefinition;
import net.devvoxel.jobs.util.JobPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JobMenu implements Listener {

    private final JobSystemPlugin plugin;
    private final Player player;
    private final GuiSettings settings;
    private final JobMessageService messages;
    private Inventory inventory;

    public JobMenu(JobSystemPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.settings = plugin.getJobConfig().getGuiSettings();
        this.messages = plugin.getMessageService();
    }

    public void open() {
        player.sendMessage(messages.format("animation-start"));
        new org.bukkit.scheduler.BukkitRunnable() {
            private final AtomicInteger tick = new AtomicInteger();

            @Override
            public void run() {
                if (tick.getAndIncrement() >= settings.getAnimationDelay()) {
                    cancel();
                    createInventory();
                    Bukkit.getPluginManager().registerEvents(JobMenu.this, plugin);
                    player.openInventory(inventory);
                    player.sendMessage(messages.format("animation-finish"));
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void createInventory() {
        int size = Math.max(9, settings.getSize());
        if (size % 9 != 0) {
            size += 9 - (size % 9);
        }
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', settings.getTitle()));
        decorateInventory();
        populateJobs();
        addUtilityButtons();
    }

    private void decorateInventory() {
        Material material = Material.matchMaterial(settings.getDecorativeMaterial());
        if (material == null) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler.clone());
        }
    }

    private void populateJobs() {
        int slot = 10;
        for (JobDefinition job : plugin.getJobManager().getJobs()) {
            if (slot >= inventory.getSize()) {
                break;
            }
            ItemStack itemStack = new ItemStack(job.getIcon());
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', job.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.translateAlternateColorCodes('&', job.getDescription()));
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Permission: " + ChatColor.WHITE + job.getPermission());
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
            inventory.setItem(slot, itemStack);
            slot++;
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
        }
    }

    private void addUtilityButtons() {
        ItemStack join = new ItemStack(Material.EMERALD);
        ItemMeta joinMeta = join.getItemMeta();
        if (joinMeta != null) {
            joinMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.getMessage("join-button")));
            joinMeta.setLore(List.of(ChatColor.GRAY + "Select a job item first."));
            join.setItemMeta(joinMeta);
        }
        ItemStack leave = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leave.getItemMeta();
        if (leaveMeta != null) {
            leaveMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.getMessage("leave-button")));
            leaveMeta.setLore(List.of(ChatColor.GRAY + "Return to freelancing."));
            leave.setItemMeta(leaveMeta);
        }
        int joinSlot = Math.max(0, inventory.getSize() - 6);
        int leaveSlot = Math.max(0, inventory.getSize() - 4);
        inventory.setItem(joinSlot, join);
        inventory.setItem(leaveSlot, leave);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTopInventory().equals(inventory)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player clicker = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        if (current.getType() == Material.EMERALD) {
            handleJoin(clicker);
            clicker.closeInventory();
            return;
        }
        if (current.getType() == Material.BARRIER) {
            handleLeave(clicker);
            clicker.closeInventory();
            return;
        }

        ItemMeta meta = current.getItemMeta();
        if (meta != null && meta.getDisplayName() != null) {
            JobDefinition job = plugin.getJobManager().getJobs().stream()
                    .filter(j -> ChatColor.stripColor(j.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(meta.getDisplayName())))
                    .findFirst().orElse(null);
            if (job != null) {
                clicker.setMetadata("jobs-selected", new org.bukkit.metadata.FixedMetadataValue(plugin, job.getId()));
                clicker.sendMessage(messages.format("job-selected", Map.of("job_name", ChatColor.stripColor(job.getDisplayName()))));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        player.removeMetadata("jobs-selected", plugin);
        HandlerList.unregisterAll(this);
    }

    private void handleJoin(Player player) {
        if (!player.hasMetadata("jobs-selected")) {
            player.sendMessage(messages.format("no-job-selected"));
            return;
        }
        String jobId = player.getMetadata("jobs-selected").get(0).asString();
        JobDefinition definition = plugin.getJobManager().getJob(jobId);
        if (definition == null) {
            player.sendMessage(messages.format("job-not-found"));
            return;
        }
        if (!player.hasPermission(definition.getPermission())) {
            player.sendMessage(messages.format("job-permission-missing", Map.of("permission", definition.getPermission())));
            return;
        }
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        data.setJobId(jobId);
        plugin.savePlayerData(data);
        player.sendMessage(messages.format("job-joined", Map.of("job_name", ChatColor.stripColor(definition.getDisplayName()))));
        playSelectSound(player);
        player.removeMetadata("jobs-selected", plugin);
        if (!plugin.isStorageAvailable()) {
            player.sendMessage(messages.format("storage-disabled"));
        }
    }

    private void handleLeave(Player player) {
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        data.setJobId(null);
        plugin.savePlayerData(data);
        player.sendMessage(messages.format("job-unjoined"));
        playSelectSound(player);
        player.removeMetadata("jobs-selected", plugin);
        if (!plugin.isStorageAvailable()) {
            player.sendMessage(messages.format("storage-disabled"));
        }
    }

    private void playSelectSound(Player player) {
        try {
            Sound sound = Sound.valueOf(plugin.getJobConfig().getActionFeedbackSettings().getSound());
            player.playSound(player.getLocation(), sound, plugin.getJobConfig().getActionFeedbackSettings().getVolume(), plugin.getJobConfig().getActionFeedbackSettings().getPitch());
        } catch (IllegalArgumentException ignored) {
        }
    }

}
