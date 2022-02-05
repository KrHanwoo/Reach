package com.hanwoo.reach;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReachCommand implements CommandExecutor, TabCompleter {
    private final Reach plugin;

    public ReachCommand(Reach plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return sendMessage(sender, ChatColor.RED + "Usage: /reach (default|player)");
        if (args[0].equalsIgnoreCase("default")) {
            if (args.length < 2) return sendMessage(sender, plugin.getDefaultReach() == null ?
                    "No default reach set" :
                    "The default reach is " + plugin.getDefaultReach()
            );
            if (args[1].equalsIgnoreCase("reset")) {
                plugin.setDefaultReach(null);
                plugin.getConfig().set("default", null);
                return sendMessage(sender, "Reset default reach");
            }
            int reach;
            try {
                reach = Integer.parseInt(args[1]);
            } catch (Exception e) {
                return sendMessage(sender, ChatColor.RED + "Invalid number");
            }
            if (reach < 0) return sendMessage(sender, ChatColor.RED + "Reach cannot be negative");
            if (reach > 100) return sendMessage(sender, ChatColor.RED + "Reach cannot be greater than 100");
            plugin.setDefaultReach(reach);
            sender.sendMessage("Set default reach to " + reach);
            plugin.getConfig().set("default", reach);

        } else if (args[0].equalsIgnoreCase("player")) {
            if (args.length < 2)
                return sendMessage(sender, ChatColor.RED + "Usage: /reach player <player> [number|reset]");
            boolean wildcard = args[1].equalsIgnoreCase("*");
            Player player = wildcard ? null : Bukkit.getPlayerExact(args[1]);
            if (player == null && !wildcard)
                return sendMessage(sender, ChatColor.RED + "No player was found");
            if (args.length < 3)
                if (wildcard)
                    return sendMessage(sender, ChatColor.RED + "Usage: /reach player * [number|reset]");
                else
                    return sendMessage(sender, plugin.hasReach(player.getUniqueId()) ?
                            player.getName() + "'s reach is " + plugin.getReach(player.getUniqueId()) :
                            player.getName() + " does not have a reach set");
            if (args[2].equalsIgnoreCase("reset")) {
                if (wildcard) {
                    plugin.getPlayerReach().clear();
                    plugin.getConfig().getKeys(false).forEach(k -> {
                        if (!k.equals("default")) plugin.getConfig().set(k, null);
                    });
                    return sendMessage(sender, "Reset everyone's reach");
                } else {
                    plugin.getPlayerReach().remove(player.getUniqueId());
                    plugin.getConfig().set(player.getUniqueId().toString(), null);
                    return sendMessage(sender, "Reset " + player.getName() + "'s reach");
                }
            }
            int reach;
            try {
                reach = Integer.parseInt(args[2]);
            } catch (Exception e) {
                return sendMessage(sender, ChatColor.RED + "Invalid number");
            }
            if (reach < 0) return sendMessage(sender, ChatColor.RED + "Reach cannot be negative");
            if (reach > 100) return sendMessage(sender, ChatColor.RED + "Reach cannot be greater than 100");
            if (wildcard) {
                plugin.getPlayerReach().forEach((key, val) -> plugin.getPlayerReach().put(key, reach));
                plugin.getConfig().getKeys(false).forEach(k -> {
                    if (!k.equals("default")) plugin.getConfig().set(k, reach);
                });
                Bukkit.getOnlinePlayers().forEach(p -> plugin.getConfig().set(p.getUniqueId().toString(), reach));
                return sendMessage(sender, "Set everyone's reach to " + reach);
            }
            plugin.getPlayerReach().put(player.getUniqueId(), reach);
            plugin.getConfig().set(player.getUniqueId().toString(), reach);
            sender.sendMessage("Set " + player.getName() + "'s reach to " + reach);
        } else {
            return sendMessage(sender, ChatColor.RED + "Usage: /reach (default|player)");
        }
        return true;
    }

    private boolean sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("default")) return Collections.singletonList("reset");
        if (args.length == 2 && args[0].equalsIgnoreCase("player"))
            return Stream.concat(
                            Bukkit.getOnlinePlayers().stream()
                                    .map(HumanEntity::getName)
                                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())),
                            Stream.of("*")
                                    .filter(n -> args[1].isEmpty()))
                    .collect(Collectors.toList());
        if (args.length == 3 && args[0].equalsIgnoreCase("player")) return Collections.singletonList("reset");
        if (args.length == 1)
            return Stream.of("default", "player").filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
