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
import org.jetbrains.annotations.Nullable;

public class EffPathfind extends Effect {

    static {
        Skript.registerEffect(EffPathfind.class,
                "pathfind %entity% to %location% [at speed %-number%]",
                "make %entity% pathfind to %location% [at speed %-number%]");
    }

    private Expression<Entity> entityExpr;
    private Expression<Location> locExpr;
    private Expression<Number> speedExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entityExpr = (Expression<Entity>) exprs[0];
        locExpr = (Expression<Location>) exprs[1];
        speedExpr = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity entity = entityExpr.getSingle(e);
        Location loc = locExpr.getSingle(e);
        double speed = (speedExpr != null) ? speedExpr.getSingle(e).doubleValue() : 1.0;

        if (entity instanceof Mob mob && loc != null) {
            // 1. Clear current path to prevent conflict
            mob.getPathfinder().stopPathfinding();

            // 2. Start the new path
            mob.getPathfinder().moveTo(loc, speed);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "pathfind " + entityExpr.toString(e, debug) + " to " + locExpr.toString(e, debug);
    }
}