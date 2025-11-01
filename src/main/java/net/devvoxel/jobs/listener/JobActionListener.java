package net.devvoxel.jobs.listener;

import net.devvoxel.jobs.JobSystemPlugin;
import net.devvoxel.jobs.config.ActionFeedbackSettings;
import net.devvoxel.jobs.config.JobMessageService;
import net.devvoxel.jobs.util.JobDefinition;
import net.devvoxel.jobs.util.JobPlayerData;
import net.devvoxel.jobs.util.JobProgressResult;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.Tag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JobActionListener implements Listener {

    private final JobSystemPlugin plugin;
    private final JobMessageService messages;
    private final ActionFeedbackSettings feedbackSettings;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private static final long FEEDBACK_COOLDOWN_MS = 1500L;

    public JobActionListener(JobSystemPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.feedbackSettings = plugin.getJobConfig().getActionFeedbackSettings();
    }

    private static final Set<Material> DIGGER_BLOCKS = Set.of(
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.ROOTED_DIRT,
            Material.GRASS_BLOCK,
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.MUD,
            Material.CLAY
    );

    private static final Set<Material> FARMER_EXTRA_CROPS = Set.of(
            Material.NETHER_WART,
            Material.SWEET_BERRY_BUSH,
            Material.CACTUS,
            Material.SUGAR_CANE,
            Material.BAMBOO,
            Material.PITCHER_CROP,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.ATTACHED_MELON_STEM,
            Material.ATTACHED_PUMPKIN_STEM
    );

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
        plugin.clearCachedPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        JobDefinition job = resolveJob(player);
        if (job == null) {
            return;
        }
        Block block = event.getBlock();
        switch (job.getId()) {
            case "miner":
                if (isMinerBlock(block.getType())) {
                    sendFeedback(player, job);
                }
                break;
            case "lumberjack":
                if (Tag.LOGS.isTagged(block.getType())) {
                    sendFeedback(player, job);
                }
                break;
            case "farmer":
                if (isCrop(block.getType())) {
                    sendFeedback(player, job);
                }
                break;
            case "digger":
                if (isDirt(block.getType())) {
                    sendFeedback(player, job);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        JobDefinition job = resolveJob(killer);
        if (job == null) {
            return;
        }
        if (job.getId().equals("hunter")) {
            sendFeedback(killer, job);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        Player player = event.getPlayer();
        JobDefinition job = resolveJob(player);
        if (job != null && job.getId().equals("fisherman")) {
            sendFeedback(player, job);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTill(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Material itemInHand = event.getItem() != null ? event.getItem().getType() : Material.AIR;
        if (!itemInHand.name().endsWith("_HOE")) {
            return;
        }
        Player player = event.getPlayer();
        JobDefinition job = resolveJob(player);
        if (job != null && job.getId().equals("farmer")) {
            sendFeedback(player, job);
        }
    }

    private JobDefinition resolveJob(Player player) {
        JobPlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null || data.getJobId() == null) {
            return null;
        }
        return plugin.getJobManager().getJob(data.getJobId());
    }

    private void sendFeedback(Player player, JobDefinition job) {
        JobProgressResult result = plugin.getProgressionManager().reward(player, job);
        if (result == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && now - last < FEEDBACK_COOLDOWN_MS) {
            return;
        }
        cooldowns.put(player.getUniqueId(), now);

        Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("job_name", ChatColor.stripColor(job.getDisplayName()));
        placeholders.put("money_formatted", plugin.getProgressionManager().formatMoney(result.moneyAwarded()));
        placeholders.put("xp", plugin.getProgressionManager().formatExperience(result.experienceAwarded()));
        placeholders.put("level", String.valueOf(result.newLevel()));
        placeholders.put("progress_bar", plugin.getProgressionManager().createProgressBar(result.progressFraction()));
        if (result.maxLevelReached()) {
            String maxLabel = messages.getMessage("progress-max-label");
            placeholders.put("xp_current", maxLabel);
            placeholders.put("xp_required", maxLabel);
        } else {
            placeholders.put("xp_current", plugin.getProgressionManager().formatExperience(result.currentExperience()));
            placeholders.put("xp_required", plugin.getProgressionManager().formatExperience(result.requiredExperience()));
        }

        player.sendMessage(messages.format("action-feedback", placeholders));
        if (result.leveledUp()) {
            if (result.maxLevelReached()) {
                player.sendMessage(messages.format("job-max-level", Map.of("job_name", ChatColor.stripColor(job.getDisplayName()))));
            } else {
                player.sendMessage(messages.format("job-level-up", Map.of(
                        "job_name", ChatColor.stripColor(job.getDisplayName()),
                        "level", String.valueOf(result.newLevel())
                )));
            }
        }

        if (feedbackSettings.isEnabled()) {
            try {
                Sound sound = Sound.valueOf(feedbackSettings.getSound());
                player.playSound(player.getLocation(), sound, feedbackSettings.getVolume(), feedbackSettings.getPitch());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private boolean isMinerBlock(Material material) {
        if (material == Material.ANCIENT_DEBRIS) {
            return true;
        }
        if (material.name().endsWith("_ORE")) {
            return true;
        }
        return Tag.BASE_STONE_OVERWORLD.isTagged(material)
                || Tag.BASE_STONE_NETHER.isTagged(material);
    }

    private boolean isCrop(Material material) {
        if (Tag.CROPS.isTagged(material)) {
            return true;
        }
        return FARMER_EXTRA_CROPS.contains(material);
    }

    private boolean isDirt(Material material) {
        return DIGGER_BLOCKS.contains(material);
    }
}
