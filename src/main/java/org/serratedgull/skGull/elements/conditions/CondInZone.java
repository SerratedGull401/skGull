package org.serratedgull.skGull.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class CondInZone extends Condition {

    static {
        Skript.registerCondition(CondInZone.class,
                "%entity% is (within|in) zone %location% to %location%");
    }

    private Expression<Entity> entity;
    private Expression<Location> loc1;
    private Expression<Location> loc2;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entity = (Expression<Entity>) exprs[0];
        loc1 = (Expression<Location>) exprs[1];
        loc2 = (Expression<Location>) exprs[2];
        return true;
    }

    @Override
    public boolean check(Event e) {
        Entity ent = entity.getSingle(e);
        Location l1 = loc1.getSingle(e);
        Location l2 = loc2.getSingle(e);
        if (ent == null || l1 == null || l2 == null) return false;

        Location p = ent.getLocation();

        // Math to check if point is inside cuboid
        return p.getX() >= Math.min(l1.getX(), l2.getX()) && p.getX() <= Math.max(l1.getX(), l2.getX()) &&
                p.getY() >= Math.min(l1.getY(), l2.getY()) && p.getY() <= Math.max(l1.getY(), l2.getY()) &&
                p.getZ() >= Math.min(l1.getZ(), l2.getZ()) && p.getZ() <= Math.max(l1.getZ(), l2.getZ());
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "entity is in zone";
    }
}