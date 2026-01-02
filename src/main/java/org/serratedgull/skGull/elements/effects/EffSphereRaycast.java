package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.util.Kleenean;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sphere raycast effect that detects entities and blocks with a thick ray
 *
 * Syntax: sphere raycast from %location% with radius %number% for %number% blocks [and store [result] in %-objects%]
 *
 * Examples:
 * - sphere raycast from player with radius 1.5 for 20 blocks
 * - sphere raycast from player's eye location with radius 0.5 for 10 blocks and store result in {_hit}
 */
public class EffSphereRaycast extends Effect {

    static {
        Skript.registerEffect(EffSphereRaycast.class,
                        "sphere raycast from %location% with radius %number% for %number% blocks [and store [result] in %-objects%]");
    }

    private Expression<Location> location;
    private Expression<Number> radius;
    private Expression<Number> distance;
    private Variable<?> resultVariable;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        location = (Expression<Location>) exprs[0];
        radius = (Expression<Number>) exprs[1];
        distance = (Expression<Number>) exprs[2];

        if (exprs[3] != null) {
            if (exprs[3] instanceof Variable) {
                resultVariable = (Variable<?>) exprs[3];
            } else {
                Skript.error("The result storage must be a variable");
                return false;
            }
        }

        return true;
    }

    @Override
    protected void execute(Event e) {
        Location loc = location.getSingle(e);
        Number rad = radius.getSingle(e);
        Number dist = distance.getSingle(e);

        if (loc == null || rad == null || dist == null) return;

        double radiusValue = rad.doubleValue();
        double distanceValue = dist.doubleValue();
        Vector direction = loc.getDirection().normalize();

        // 1. Identify the source entity safely from the event
        // We check if the location has an entity, or if the event itself has one (like a player)
        final Entity source = (e instanceof org.bukkit.event.entity.EntityEvent)
                ? ((org.bukkit.event.entity.EntityEvent) e).getEntity()
                : (e instanceof org.bukkit.event.player.PlayerEvent)
                ? ((org.bukkit.event.player.PlayerEvent) e).getPlayer()
                : null;

        // 2. Raycast for entities - This ignores the 'source'
        RayTraceResult entityResult = loc.getWorld().rayTraceEntities(
                loc,
                direction,
                distanceValue,
                radiusValue,
                entity -> (entity instanceof LivingEntity && !entity.equals(source))
        );

        // 3. Raycast for blocks
        RayTraceResult blockResult = loc.getWorld().rayTraceBlocks(
                loc,
                direction,
                distanceValue,
                FluidCollisionMode.NEVER,
                false
        );

        // 4. Calculate closest hit
        RayTraceResult finalResult = null;
        if (entityResult != null && blockResult != null) {
            double entityDist = entityResult.getHitPosition().distance(loc.toVector());
            double blockDist = blockResult.getHitPosition().distance(loc.toVector());
            finalResult = (entityDist < blockDist) ? entityResult : blockResult;
        } else {
            finalResult = (entityResult != null) ? entityResult : blockResult;
        }

        // 5. Apply the result
        if (resultVariable != null) {
            if (finalResult != null) {
                Location hitLocation = finalResult.getHitPosition().toLocation(loc.getWorld());
                resultVariable.change(e, new Object[]{hitLocation}, Changer.ChangeMode.SET);
            } else {
                resultVariable.change(e, null, Changer.ChangeMode.DELETE);
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "sphere raycast from " + location.toString(e, debug) +
                " with radius " + radius.toString(e, debug) +
                " for " + distance.toString(e, debug) + " blocks";
    }
}