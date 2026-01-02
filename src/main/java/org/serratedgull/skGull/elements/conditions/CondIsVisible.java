package org.serratedgull.skGull.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

public class CondIsVisible extends Condition {

    static {
        Skript.registerCondition(CondIsVisible.class,
                "%entity% is visible to %player%",
                "%entity% is(n't| not) visible to %player%");
    }

    private Expression<Entity> target;
    private Expression<Player> observer;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        target = (Expression<Entity>) exprs[0];
        observer = (Expression<Player>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        Entity t = target.getSingle(e);
        Player p = observer.getSingle(e);
        if (t == null || p == null) return isNegated();

        // Raytrace from eye to eye
        RayTraceResult result = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                t.getLocation().add(0, t.getHeight() * 0.8, 0).subtract(p.getEyeLocation()).toVector(),
                p.getEyeLocation().distance(t.getLocation()),
                org.bukkit.FluidCollisionMode.NEVER,
                true // ignore pass-through blocks like grass
        );

        // If result is null, it means no blocks hit -> Target is visible
        boolean visible = (result == null);
        return isNegated() ? !visible : visible;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return target.toString(e, debug) + " is visible to " + observer.toString(e, debug);
    }
}