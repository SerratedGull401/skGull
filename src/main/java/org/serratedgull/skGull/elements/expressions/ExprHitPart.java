package org.serratedgull.skGull.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.serratedgull.skGull.elements.events.PlayerLookAtEntityEvent;
import org.serratedgull.skGull.elements.events.PlayerLookingAtEntityEvent;
import org.jetbrains.annotations.Nullable;

public class ExprHitPart extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprHitPart.class, String.class, ExpressionType.SIMPLE,
                "[the] [hit] (part|bone|section) [of [the] entity]");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected @Nullable String[] get(Event e) {
        Entity target;
        org.bukkit.util.Vector hit;

        if (e instanceof PlayerLookAtEntityEvent ev) {
            target = ev.getTargetEntity();
            hit = ev.getHitLocation();
        } else if (e instanceof PlayerLookingAtEntityEvent ev) {
            target = ev.getTargetEntity();
            hit = ev.getHitLocation();
        } else {
            return null;
        }

        double relativeHeight = hit.getY() - target.getLocation().getY();
        double percent = relativeHeight / target.getHeight();

        if (percent > 0.75) return new String[]{"head"};
        if (percent > 0.25) return new String[]{"body"};
        return new String[]{"feet"};
    }

    @Override
    public boolean isSingle() { return true; }
    @Override
    public Class<? extends String> getReturnType() { return String.class; }
    @Override
    public String toString(@Nullable Event e, boolean debug) { return "hit part"; }
}