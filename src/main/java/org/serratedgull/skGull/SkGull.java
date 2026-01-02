package org.serratedgull.skGull;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.serratedgull.skGull.elements.events.PlayerLookingAtEntityEvent;
import org.serratedgull.skGull.profiler.SkriptProfiler;
import org.serratedgull.skGull.elements.events.PlayerLookAtEntityEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkGull extends JavaPlugin {

    private static SkGull instance;
    private static SkriptAddon addon;
    private SkriptProfiler profiler;

    // Cache to prevent the event from firing every single tick while looking
// Top of SkGull class
    private final Map<UUID, TargetInfo> lookCache = new HashMap<>();

    private record TargetInfo(UUID targetId, double lastDistance) {}

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

                    // Fire Continuous Event (Always fires)
                    Bukkit.getPluginManager().callEvent(new PlayerLookingAtEntityEvent(player, target, result.getHitPosition()));

                    // Logic: Fire "Once" if target changed OR if player significantly changed distance
                    // We check if they moved more than 0.5 blocks to avoid spamming
                    boolean changedTarget = (previous == null || !currentTargetId.equals(previous.targetId()));
                    boolean movedCloser = (previous != null && (previous.lastDistance() - currentDist) > 0.5);

                    if (changedTarget || movedCloser) {
                        PlayerLookAtEntityEvent onceEvent = new PlayerLookAtEntityEvent(player, target, result.getHitPosition());
                        Bukkit.getPluginManager().callEvent(onceEvent);

                        lookCache.put(playerId, new TargetInfo(currentTargetId, currentDist));
                    }
                } else {
                    lookCache.remove(playerId);
                }
            }
        }, 0L, 2L);
    }

    @Override
    public void onEnable() {
        instance = this;
        addon = Skript.registerAddon(this);
        profiler = new SkriptProfiler();
        ProfileCommand profileCmd = new ProfileCommand(profiler);
        getCommand("skriptprofile").setExecutor(profileCmd);
        getCommand("skriptprofile").setTabCompleter(profileCmd);

        try {
            // This line automatically registers your Events/Expressions/Effects
            // provided they have the proper 'static' registration blocks.
            addon.loadClasses("org.serratedgull.skGull", "elements");
            getLogger().info("skGull classes loaded successfully!");
        } catch (IOException e) {
            getLogger().severe("Failed to load skGull classes!");
            e.printStackTrace();
            return;
        }

        startLookAtEntityScheduler();
        getLogger().info("skGull v1.0.1 enabled!");
    }

    @Override
    public void onDisable() {
        if (profiler != null) profiler.stop();
        lookCache.clear();
    }

    public static SkGull getInstance() { return instance; }
    public static SkriptAddon getAddonInstance() { return addon; }
}