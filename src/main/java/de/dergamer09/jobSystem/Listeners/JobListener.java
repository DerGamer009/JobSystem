package de.dergamer09.jobSystem.Listeners;

import de.dergamer09.jobSystem.JobSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class JobListener implements Listener
{

    private final JobSystem plugin;

    public JobListener(JobSystem plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String job = plugin.getPlayerJob(uuid);
        player.sendMessage(ChatColor.GREEN + "Dein aktueller Job: " + job);
    }

    public void openJobGUI(Player player)
    {
        // Two rows to have a little more space for additional jobs
        Inventory jobmenu = Bukkit.createInventory(null, 18, "Wähle deinen Job");

        ItemStack miner = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta minerMeta = miner.getItemMeta();
        minerMeta.setDisplayName("Miner");
        miner.setItemMeta(minerMeta);

        ItemStack farmer = new ItemStack(Material.WHEAT);
        ItemMeta farmerMeta = farmer.getItemMeta();
        farmerMeta.setDisplayName("Farmer");
        farmer.setItemMeta(farmerMeta);

        ItemStack lumberjack = new ItemStack(Material.IRON_AXE);
        ItemMeta lumberjackMeta = lumberjack.getItemMeta();
        lumberjackMeta.setDisplayName("Holzfäller");
        lumberjack.setItemMeta(lumberjackMeta);

        ItemStack fisherman = new ItemStack(Material.FISHING_ROD);
        ItemMeta fishermanMeta = fisherman.getItemMeta();
        fishermanMeta.setDisplayName("Fischer");
        fisherman.setItemMeta(fishermanMeta);

        ItemStack builder = new ItemStack(Material.BRICKS);
        ItemMeta builderMeta = builder.getItemMeta();
        builderMeta.setDisplayName("Baumeister");
        builder.setItemMeta(builderMeta);

        ItemStack hunter = new ItemStack(Material.BOW);
        ItemMeta hunterMeta = hunter.getItemMeta();
        hunterMeta.setDisplayName("Jäger");
        hunter.setItemMeta(hunterMeta);

        ItemStack alchemist = new ItemStack(Material.BREWING_STAND);
        ItemMeta alchemistMeta = alchemist.getItemMeta();
        alchemistMeta.setDisplayName("Alchemist");
        alchemist.setItemMeta(alchemistMeta);

        ItemStack enchanter = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta enchanterMeta = enchanter.getItemMeta();
        enchanterMeta.setDisplayName("Verzauberer");
        enchanter.setItemMeta(enchanterMeta);

        jobmenu.setItem(0, miner);
        jobmenu.setItem(1, farmer);
        jobmenu.setItem(2, lumberjack);
        jobmenu.setItem(3, fisherman);
        jobmenu.setItem(4, builder);
        jobmenu.setItem(5, hunter);
        jobmenu.setItem(6, alchemist);
        jobmenu.setItem(7, enchanter);

        player.openInventory(jobmenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getView().getTitle().equals("Wähle deinen Job"))
        {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.hasItemMeta())
            {
                String job = clickedItem.getItemMeta().getDisplayName();
                plugin.setPlayerJob(player.getUniqueId(), job);
                player.sendMessage("Du hast den Job " + job + " gewählt.");
            }
        }
    }

}
