package com.top1.epsilon121.managers;

import com.top1.epsilon121.Epsilon121;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CooldownManager {
    private final Epsilon121 plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;
    private final File cooldownFile;
    private final FileConfiguration cooldownConfig;

    public CooldownManager(Epsilon121 plugin) {
        this.plugin = plugin;
        this.cooldownMillis = plugin.getConfig().getLong("cooldown-hours", 12L) * 60L * 60L * 1000L;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        if (!cooldownFile.exists()) {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Nie udało się utworzyć cooldowns.yml: " + e.getMessage());
            }
        }

        this.cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);
        loadCooldowns();
    }

    public boolean hasCooldown(UUID playerId) {
        Long expiresAt = cooldowns.get(playerId);
        if (expiresAt == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expiresAt) {
            cooldowns.remove(playerId);
            return false;
        }

        return true;
    }

    public String getCooldownFormatted(UUID playerId) {
        Long expiresAt = cooldowns.get(playerId);
        if (expiresAt == null) {
            return "0h 0m";
        }

        long remaining = Math.max(0L, expiresAt - System.currentTimeMillis());
        Duration duration = Duration.ofMillis(remaining);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return hours + "h " + minutes + "m";
    }

    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownMillis);
    }

    public void saveCooldowns() {
        cooldownConfig.set("players", null);
        for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
            cooldownConfig.set("players." + entry.getKey(), entry.getValue());
        }

        try {
            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udało się zapisać cooldownów: " + e.getMessage());
        }
    }

    private void loadCooldowns() {
        if (!cooldownConfig.isConfigurationSection("players")) {
            return;
        }

        for (String key : cooldownConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long expiresAt = cooldownConfig.getLong("players." + key);
                if (expiresAt > System.currentTimeMillis()) {
                    cooldowns.put(uuid, expiresAt);
                }
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Pominięto niepoprawny UUID w cooldowns.yml: " + key);
            }
        }
    }
}
