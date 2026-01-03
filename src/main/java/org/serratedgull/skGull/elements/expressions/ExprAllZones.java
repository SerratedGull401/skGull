package org.serratedgull.skGull.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.serratedgull.skGull.managers.ZoneManager;
import org.jetbrains.annotations.Nullable;

public class ExprAllZones extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprAllZones.class, String.class, ExpressionType.SIMPLE, "[all] [active] containment zones");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected @Nullable String[] get(Event e) {
        // Convert the List<String> from Manager to a String[] for Skript
        return ZoneManager.getZoneNames().toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false; // Returns a list
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "all containment zones";
    }
}