package com.hanwoo.reach;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class Reach extends JavaPlugin implements Listener {
    private final Set<Entity> exclude = new HashSet<>();
    private final Set<UUID> throwExclude = new HashSet<>();
    private final Map<UUID, Integer> playerReach = new HashMap<>();
    private Integer defaultReach;

    @Override
    public void onEnable() {
        try {
            Attack.load(this);
        } catch (Exception e) {
            end(e);
        }
        if (!this.isEnabled()) return;
        saveDefaultConfig();
        if (getConfig().contains("default")) defaultReach = getConfig().getInt("default", 0);
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
        if (!(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) return;
        if (throwExclude.contains(e.getPlayer().getUniqueId())) {
            throwExclude.remove(e.getPlayer().getUniqueId());
            return;
        }
        if (!hasReach(e.getPlayer().getUniqueId()) && defaultReach == null) return;
        Player player = e.getPlayer();
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Predicate<Entity> filter = (entity) -> !entity.equals(player);
        RayTraceResult result = player.getWorld().rayTrace(eyeLocation, direction,
                hasReach(e.getPlayer().getUniqueId()) ? getReach(player.getUniqueId()) : defaultReach,
                FluidCollisionMode.NEVER, false, 0, filter);
        if (result == null) return;
        Entity entity = result.getHitEntity();
        if (entity == null) return;
        if (exclude.contains(entity)) {
            exclude.remove(entity);
            return;
        }
        if (entity.getPassengers().contains(player)) return;
        Attack.attack(player, entity);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        exclude.add(e.getEntity());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> exclude.remove(e.getEntity()));
    }

    @EventHandler
    public void onThrow(PlayerDropItemEvent e) {
        throwExclude.add(e.getPlayer().getUniqueId());
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

    public Integer getDefaultReach() {
        return defaultReach;
    }

    public void setDefaultReach(Integer defaultReach) {
        this.defaultReach = defaultReach;
    }
}
