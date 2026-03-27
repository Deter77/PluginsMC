package com.top1.epsilon121.gui;

import com.top1.epsilon121.Epsilon121;
import com.top1.epsilon121.managers.CooldownManager;
import com.top1.epsilon121.utils.ItemBuilder;
import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FishingGUI implements Listener {
    private final Epsilon121 plugin;
    private final Player player;
    private final Inventory inventory;
    private final Set<Integer> fishingSlots;
    private final List<ItemStack> rewardItems;
    private int clicksRemaining;

    public FishingGUI(Epsilon121 plugin, Player player) {
        this.fishingSlots = new HashSet<>(Arrays.asList(2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42));
        this.rewardItems = new ArrayList<>();
        this.plugin = plugin;
        this.player = player;

        String guiName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui-name", "&fMenu Połowu"));
        this.inventory = Bukkit.createInventory(null, 54, guiName);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.clicksRemaining = getClicksAllowed();
        initializeGUI();
    }

    private void initializeGUI() {
        ItemStack fishingSpot = new ItemBuilder(Material.BONE_MEAL)
                .setCustomModelData(450001)
                .setName(ChatColor.AQUA + "Kliknij, aby łowić")
                .addLore(ChatColor.GRAY + "Pozostałe kliknięcia: " + clicksRemaining)
                .build();

        for (int slot : fishingSlots) {
            inventory.setItem(slot, fishingSpot);
        }

        String infoMaterial = plugin.getConfig().getString("info-item.material", "EMERALD");
        int infoCustomModelData = plugin.getConfig().getInt("info-item.custom-model-data", 1010);
        String infoName = plugin.getConfig().getString("info-item.name", "&bInfo");
        List<String> infoLore = plugin.getConfig().getStringList("info-item.lore");

        ItemBuilder infoBuilder = new ItemBuilder(Material.valueOf(infoMaterial))
                .setCustomModelData(infoCustomModelData)
                .setName(ChatColor.translateAlternateColorCodes('&', infoName));

        for (String line : infoLore) {
            infoBuilder.addLore(ChatColor.translateAlternateColorCodes('&', line));
        }

        inventory.setItem(49, infoBuilder.build());
    }

    public void open() {
        CooldownManager cooldownManager = plugin.getCooldownManager();

        if (cooldownManager.hasCooldown(player.getUniqueId())) {
            if (hasFishingVoucher()) {
                player.sendMessage(ChatColor.GREEN + "Użyto bonu na połów!");
                consumeFishingVoucher();
            } else {
                player.sendMessage(ChatColor.RED + "Musisz poczekać przed kolejnym połowem: " + cooldownManager.getCooldownFormatted(player.getUniqueId()));
                return;
            }
        }

        player.openInventory(inventory);
    }

    private int getClicksAllowed() {
        if (player.hasPermission("rybak.fox")) return 10;
        if (player.hasPermission("rybak.mvip")) return 8;
        if (player.hasPermission("rybak.svip")) return 7;
        if (player.hasPermission("rybak.vip")) return 6;
        if (player.hasPermission("rybak.player")) return 5;
        return 5;
    }

    private boolean hasFishingVoucher() {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BONE_MEAL && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 250001) {
                return true;
            }
        }
        return false;
    }

    private void consumeFishingVoucher() {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.BONE_MEAL && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 250001) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        if (event.getWhoClicked() != player) return;

        event.setCancelled(true);

        if (clicksRemaining <= 0) {
            return;
        }

        int slot = event.getRawSlot();
        if (fishingSlots.contains(slot)) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() != Material.BONE_MEAL) return;

            ItemStack reward = plugin.getRewardManager().getRandomReward();
            if (reward != null) {
                inventory.setItem(slot, reward);
                rewardItems.add(reward.clone());
                clicksRemaining--;
                updateFishingSpots();

                if (clicksRemaining <= 0) {
                    Bukkit.getScheduler().runTaskLater(plugin, (Runnable) player::closeInventory, 10L);
                }
            }
        }
    }

    private void updateFishingSpots() {
        ItemStack fishingSpot = new ItemBuilder(Material.BONE_MEAL)
                .setCustomModelData(450001)
                .setName(ChatColor.AQUA + "Kliknij, aby łowić")
                .addLore(ChatColor.GRAY + "Pozostałe kliknięcia: " + clicksRemaining)
                .build();

        for (int slot : fishingSlots) {
            if (inventory.getItem(slot) != null && inventory.getItem(slot).getType() == Material.BONE_MEAL) {
                inventory.setItem(slot, fishingSpot);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != inventory) return;
        if (event.getPlayer() != player) return;

        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);

        if (!rewardItems.isEmpty()) {
            for (ItemStack reward : rewardItems) {
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(reward);
                if (!left.isEmpty()) {
                    for (ItemStack leftOver : left.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftOver);
                    }
                }
            }
            player.sendMessage(ChatColor.GREEN + "Zakończyłeś połów");
            plugin.getCooldownManager().setCooldown(player.getUniqueId());
        }
    }
}
