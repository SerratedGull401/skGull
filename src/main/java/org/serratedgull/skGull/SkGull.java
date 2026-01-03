package org.serratedgull.skGull;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.serratedgull.skGull.elements.effects.EffEntityPatrol;
import org.serratedgull.skGull.elements.events.PlayerLookingAtEntityEvent;
import org.serratedgull.skGull.elements.events.PlayerLookAtEntityEvent;
import org.serratedgull.skGull.elements.effects.EffEntityDoorPower;
import org.serratedgull.skGull.elements.effects.EffSetEntityPath; // Added Import
import org.serratedgull.skGull.managers.ZoneManager;
import org.serratedgull.skGull.listeners.MoveListener;
import org.serratedgull.skGull.elements.events.EvtPlayerSeeEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkGull extends JavaPlugin {

    private static SkGull instance;
    private static SkriptAddon addon;

    private final Map<UUID, TargetInfo> lookCache = new HashMap<>();
    private record TargetInfo(UUID targetId, double lastDistance) {}

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize Zone Manager
        ZoneManager.setup(this);

        // 2. Register Native Bukkit Listeners
        Bukkit.getPluginManager().registerEvents(new MoveListener(), this);

        // 3. Register Skript Addon
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("org.serratedgull.skGull", "elements");
            getLogger().info("skGull elements loaded successfully!");
        } catch (IOException e) {
            getLogger().severe("Failed to load skGull classes!");
            e.printStackTrace();
            return;
        }

        // 4. Start Background Schedulers
        startLookAtEntityScheduler();
        // --- NEW PATHFINDING & DOOR TASKS ---
        // These are the loops that actually handle the "Interpolation" and "Door Checking"
        EffSetEntityPath.startPathTask(this);
        EffEntityDoorPower.startDoorTask(this);
        EffEntityPatrol.startPatrolTask(this);

        getLogger().info("skGull v1.2.0 (Interpolated Paths & Doors) enabled!");
    }

    private void startLookAtEntityScheduler() {
        final double SCAN_DISTANCE = 100.0;
        final double ENTITY_RADIUS = 0.5;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var result = player.getWorld().rayTraceEntities(
                        player.getEyeLocation(),
                        player.getEyeLocation().getDirection(),
                        SCAN_DISTANCE,
                        ENTITY_RADIUS,
                        entity -> (entity instanceof LivingEntity && !entity.equals(player))
                );

                Entity target = (result != null) ? result.getHitEntity() : null;
                UUID playerId = player.getUniqueId();
                TargetInfo previous = lookCache.get(playerId);

                if (target != null) {
                    double currentDist = player.getEyeLocation().distance(target.getLocation());
                    UUID currentTargetId = target.getUniqueId();

                    Bukkit.getPluginManager().callEvent(new PlayerLookingAtEntityEvent(player, target, result.getHitPosition()));

                    boolean changedTarget = (previous == null || !currentTargetId.equals(previous.targetId()));
                    boolean movedCloser = (previous != null && (previous.lastDistance() - currentDist) > 0.5);

                    if (changedTarget || movedCloser) {
                        Bukkit.getPluginManager().callEvent(new PlayerLookAtEntityEvent(player, target, result.getHitPosition()));
                        lookCache.put(playerId, new TargetInfo(currentTargetId, currentDist));
                    }
                } else {
                    lookCache.remove(playerId);
                }
            }
        }, 0L, 2L);
    }

    @Override
    public void onDisable() {
        lookCache.clear();
        org.serratedgull.skGull.elements.effects.EffEmitSound.cleanupAll();
        getLogger().info("skGull disabled.");
    }

    public static SkGull getInstance() { return instance; }
    public static SkriptAddon getAddonInstance() { return addon; }
}