package com.top1.epsilon121.managers;

import com.top1.epsilon121.Epsilon121;
import com.top1.epsilon121.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class RewardManager {
    private final Epsilon121 plugin;
    private final List<RewardItem> rewards = new ArrayList<>();
    private final Random random = new Random();

    public RewardManager(Epsilon121 plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public ItemStack getRandomReward() {
        if (rewards.isEmpty()) {
            return null;
        }

        int totalWeight = rewards.stream().mapToInt(RewardItem::chance).sum();
        if (totalWeight <= 0) {
            return rewards.get(random.nextInt(rewards.size())).item().clone();
        }

        int roll = random.nextInt(totalWeight) + 1;
        int current = 0;
        for (RewardItem reward : rewards) {
            current += reward.chance();
            if (roll <= current) {
                return reward.item().clone();
            }
        }

        return rewards.get(rewards.size() - 1).item().clone();
    }

    private void loadRewards() {
        rewards.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("rewards");
        if (section == null) {
            plugin.getLogger().warning("Brak sekcji rewards w config.yml.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSec = section.getConfigurationSection(key);
            if (rewardSec == null) {
                continue;
            }

            Material material;
            try {
                material = Material.valueOf(rewardSec.getString("material", "STONE").toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Niepoprawny material dla nagrody " + key + ". Pominięto.");
                continue;
            }

            String name = ChatColor.translateAlternateColorCodes('&', rewardSec.getString("name", "&fNagroda"));
            List<String> lore = rewardSec.getStringList("lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .toList();
            int modelData = rewardSec.getInt("custom-model-data", 0);
            int amount = Math.max(1, rewardSec.getInt("amount", 1));
            int chance = Math.max(1, rewardSec.getInt("chance", 1));

            ItemBuilder builder = new ItemBuilder(material)
                    .setName(name)
                    .setLore(lore)
                    .setAmount(amount)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES);

            if (modelData > 0) {
                builder.setCustomModelData(modelData);
            }

            for (String enchantLine : rewardSec.getStringList("enchantments")) {
                String[] split = enchantLine.split(";");
                if (split.length != 2) {
                    continue;
                }

                Enchantment enchantment = Enchantment.getByName(split[0].toUpperCase());
                if (enchantment == null) {
                    continue;
                }

                try {
                    int level = Integer.parseInt(split[1]);
                    builder.addEnchant(enchantment, Math.max(1, level));
                } catch (NumberFormatException ignored) {
                }
            }

            rewards.add(new RewardItem(builder.build(), chance));
        }
    }

    private record RewardItem(ItemStack item, int chance) {}
}
