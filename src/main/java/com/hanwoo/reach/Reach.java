package com.hanwoo.reach;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class Reach extends JavaPlugin implements Listener {
    private final Set<Entity> exclude = new HashSet<>();
    private final Map<UUID, Integer> playerReach = new HashMap<>();

    @Override
    public void onEnable() {
        try {
            Attack.load(this);
        } catch (Exception e) {
            end(e);
        }
        if (!this.isEnabled()) return;
        saveDefaultConfig();
        for (String key : getConfig().getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (Exception e) {
                continue;
            }
            playerReach.put(uuid, getConfig().getInt(key, 0));
        }
        ReachCommand reachCommand = new ReachCommand(this);
        Objects.requireNonNull(getCommand("reach")).setExecutor(reachCommand);
        Objects.requireNonNull(getCommand("reach")).setTabCompleter(reachCommand);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public void end(Exception e) {
        Bukkit.getLogger().log(Level.SEVERE, "Failed to load Reach!");
        e.printStackTrace();
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!hasReach(e.getPlayer().getUniqueId())) return;
        if (!(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) return;
        Player player = e.getPlayer();
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Predicate<Entity> filter = (entity) -> !entity.equals(player);
        RayTraceResult result = player.getWorld().rayTrace(eyeLocation, direction, getReach(player.getUniqueId()), FluidCollisionMode.NEVER, false, 0, filter);
        if (result == null) return;
        Entity target = result.getHitEntity();
        if (target == null) return;
        if (exclude.contains(target))
            exclude.remove(target);
        else
            Attack.attack(player, target);

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        exclude.add(e.getEntity());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> exclude.remove(e.getEntity()));
    }

    public Map<UUID, Integer> getPlayerReach() {
        return playerReach;
    }

    public boolean hasReach(UUID uuid) {
        return playerReach.containsKey(uuid);
    }

    public int getReach(UUID uuid) {
        return playerReach.getOrDefault(uuid, 0);
    }
}
