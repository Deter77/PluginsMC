package com.top1.epsilon121.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(new ArrayList<>(lore));
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
            lore.add(line);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return item.clone();
    }
}
