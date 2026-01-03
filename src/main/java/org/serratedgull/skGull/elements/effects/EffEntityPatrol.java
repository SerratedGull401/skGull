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
import org.serratedgull.skGull.SkGull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffEntityPatrol extends Effect {

    static {
        Skript.registerEffect(EffEntityPatrol.class,
                "force %entity% to (patrol|loop path) %locations% at speed %number%");
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
                path.add(l.clone().add(0.5, 0, 0.5));
            }

            // We use a different metadata key so it doesn't conflict with one-way paths
            mob.setMetadata("skgull_patrol_path", new FixedMetadataValue(SkGull.getInstance(), path));
            mob.setMetadata("skgull_path_speed", new FixedMetadataValue(SkGull.getInstance(), speed.doubleValue()));

            mob.getPathfinder().moveTo(path.get(0), speed.doubleValue());
        }
    }

    @Override
    public String toString(Event e, boolean debug) { return "force patrol"; }

    public static void startPatrolTask(SkGull plugin) {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Mob mob && mob.hasMetadata("skgull_patrol_path")) {

                        // Priority 1: If mob is fighting, stop patrolling
                        if (mob.getTarget() != null) continue;

                        List<Location> path = (List<Location>) mob.getMetadata("skgull_patrol_path").get(0).value();
                        double speed = mob.getMetadata("skgull_path_speed").get(0).asDouble();

                        Location target = path.get(0);

                        if (mob.getLocation().distanceSquared(target) < 2.25) {
                            // The "Infinite" Secret: Remove from front, add to back
                            Location reached = path.remove(0);
                            path.add(reached);

                            mob.getPathfinder().moveTo(path.get(0), speed);
                        } else {
                            // Ensure it keeps moving if it gets bumped
                            if (!mob.getPathfinder().hasPath()) {
                                mob.getPathfinder().moveTo(target, speed);
                            }
                        }
                    }
                }
            }
        }, 0L, 5L);
    }
}