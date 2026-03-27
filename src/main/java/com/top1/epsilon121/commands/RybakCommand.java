package com.top1.epsilon121.commands;

import com.top1.epsilon121.Epsilon121;
import com.top1.epsilon121.gui.FishingGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RybakCommand implements CommandExecutor {
    private final Epsilon121 plugin;

    public RybakCommand(Epsilon121 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rybak.admin")) {
            sender.sendMessage(ChatColor.RED + "Nie masz uprawnień do tej komendy.");
            return true;
        }

        if (args.length != 2 || !args[1].equalsIgnoreCase("open")) {
            sender.sendMessage(ChatColor.YELLOW + "Użycie: /rybakpolow <gracz> open");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Nie znaleziono gracza: " + args[0]);
            return true;
        }

        new FishingGUI(plugin, target).open();
        sender.sendMessage(ChatColor.GREEN + "Otwarto GUI połowu dla: " + target.getName());
        return true;
    }
}
