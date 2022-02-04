package com.hanwoo.reach;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class Attack {
    private static Reach plugin;

    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static Class<?> craftPlayerClass;
    private static Class<?> craftEntityClass;
    private static Class<?> entityClass;
    private static Method playerHandle;
    private static Method entityHandle;

    public static void load(Reach plugin) throws Exception {
        Attack.plugin = plugin;
        craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
        craftEntityClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftEntity");
        try {
            entityClass = Class.forName("net.minecraft.world.entity.Entity");
        } catch (ClassNotFoundException e) {
            entityClass = Class.forName("net.minecraft.server." + VERSION + ".Entity");
        }
        playerHandle = craftPlayerClass.getDeclaredMethod("getHandle");
        entityHandle = craftEntityClass.getDeclaredMethod("getHandle");
    }

    public static void attack(Player player, Entity entity) {
        try {
            Object nmsPlayer = playerHandle.invoke(craftPlayerClass.cast(player));
            Object nmsEntity = entityHandle.invoke(craftEntityClass.cast(entity));

            Method attack = nmsPlayer.getClass().getDeclaredMethod("attack", entityClass);
            attack.invoke(nmsPlayer, nmsEntity);
        } catch (Exception e) {
            plugin.end(e);
        }
    }
}
