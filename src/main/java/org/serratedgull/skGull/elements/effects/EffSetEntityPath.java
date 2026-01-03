package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.serratedgull.skGull.SkGull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffSetEntityPath extends Effect {

    static {
        Skript.registerEffect(EffSetEntityPath.class,
                "force %entity% to follow path %locations% at speed %number%");
    }

    private Expression<Entity> entityExpr;
    private Expression<Location> locsExpr;
    private Expression<Number> speedExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entityExpr = (Expression<Entity>) exprs[0];
        locsExpr = (Expression<Location>) exprs[1];
        speedExpr = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity entity = entityExpr.getSingle(e);
        Location[] locArray = locsExpr.getAll(e);
        Number speed = speedExpr.getSingle(e);

        if (entity instanceof Mob mob && locArray.length > 0) {
            List<Location> path = new ArrayList<>();
            for (Location l : locArray) {
                path.add(l.clone().add(0.5, 0, 0.5)); // Keep centering
            }

            mob.setTarget(null);
            mob.getPathfinder().stopPathfinding();

            mob.setMetadata("skgull_custom_path", new FixedMetadataValue(SkGull.getInstance(), path));
            mob.setMetadata("skgull_path_speed", new FixedMetadataValue(SkGull.getInstance(), speed.doubleValue()));

            // Start moving
            mob.getPathfinder().moveTo(path.get(0), speed.doubleValue());
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "interpolated path follow";
    }

    public static void startPathTask(SkGull plugin) {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                for (org.bukkit.entity.Entity entity : world.getEntities()) {
                    if (entity instanceof org.bukkit.entity.Mob mob && mob.hasMetadata("skgull_custom_path")) {

                        // 1. Check if the mob is currently in "Combat Mode"
                        // If he has a target, we skip the pathing logic entirely.
                        if (mob.getTarget() != null) continue;

                        @SuppressWarnings("unchecked")
                        List<org.bukkit.Location> path = (List<org.bukkit.Location>) mob.getMetadata("skgull_custom_path").get(0).value();
                        if (path == null || path.isEmpty()) {
                            mob.removeMetadata("skgull_custom_path", plugin);
                            continue;
                        }

                        double speed = mob.getMetadata("skgull_path_speed").get(0).asDouble();
                        org.bukkit.Location target = path.get(0);

                        // 2. Simple Path Logic
                        double distSq = mob.getLocation().distanceSquared(target);

                        // Switch to next node if within 1.5 blocks (smooth transition)
                        if (distSq < 2.25) {
                            path.remove(0);
                            if (!path.isEmpty()) {
                                mob.getPathfinder().moveTo(path.get(0), speed);
                            }
                        } else {
                            // Keep moving toward the current node
                            mob.getPathfinder().moveTo(target, speed);
                        }
                    }
                }
            }
        }, 0L, 5L); // Standard 4-times-a-second check (very stable)
    }
}