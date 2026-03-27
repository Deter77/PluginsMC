package com.top1.epsilon121;

import com.top1.epsilon121.commands.RybakCommand;
import com.top1.epsilon121.managers.CooldownManager;
import com.top1.epsilon121.managers.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Epsilon121 extends JavaPlugin {
    private static Epsilon121 instance;
    private CooldownManager cooldownManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        cooldownManager = new CooldownManager(this);
        rewardManager = new RewardManager(this);

        getCommand("rybakpolow").setExecutor(new RybakCommand(this));
        getLogger().info("Plugin FoxRybak został włączony!");
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) {
            cooldownManager.saveCooldowns();
        }
        getLogger().info("Plugin FoxRybak został wyłączony!");
    }

    public static Epsilon121 getInstance() {
        return instance;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
