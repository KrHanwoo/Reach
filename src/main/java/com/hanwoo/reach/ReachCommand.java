package com.hanwoo.reach;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReachCommand implements CommandExecutor, TabCompleter {
    private final Reach plugin;

    public ReachCommand(Reach plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return sendMessage(sender, ChatColor.RED + "Usage: /reach <player> [(number)|reset]");
        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null)
            return sendMessage(sender, ChatColor.RED + "No player was found");
        if (args.length < 2)
            return sendMessage(sender, plugin.hasReach(player.getUniqueId()) ?
                    player.getName() + "'s reach distance is " + plugin.getReach(player.getUniqueId()) :
                    player.getName() + " does not have a reach distance set");
        if (args[1].equalsIgnoreCase("reset")) {
            plugin.getPlayerReach().remove(player.getUniqueId());
            plugin.getConfig().set(player.getUniqueId().toString(), null);
            return sendMessage(sender, "Reset " + player.getName() + "'s reach distance");
        }
        int reach;
        try {
            reach = Integer.parseInt(args[1]);
        } catch (Exception e) {
            return sendMessage(sender, ChatColor.RED + "Invalid number");
        }
        if (reach < 0) return sendMessage(sender, ChatColor.RED + "Reach distance cannot be negative");
        if (reach > 100) return sendMessage(sender, ChatColor.RED + "Reach distance cannot be greater than 100");
        plugin.getPlayerReach().put(player.getUniqueId(), reach);
        sender.sendMessage("Set " + player.getName() + "'s reach distance to " + reach);
        plugin.getConfig().set(player.getUniqueId().toString(), reach);
        return true;
    }

    private boolean sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Bukkit.getOnlinePlayers().stream()
                .map(HumanEntity::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        if (args.length == 2) return Collections.singletonList("reset");
        return Collections.emptyList();
    }
}
