package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.serratedgull.skGull.managers.ZoneManager;

public class EffCreateZone extends Effect {
    static {
        Skript.registerEffect(EffCreateZone.class, "create [a] [new] containment zone named %string% from %location% to %location%");
    }

    private Expression<String> name;
    private Expression<Location> l1, l2;

    @Override
    public boolean init(Expression<?>[] exprs, int mp, Kleenean kd, ParseResult pr) {
        name = (Expression<String>) exprs[0];
        l1 = (Expression<Location>) exprs[1];
        l2 = (Expression<Location>) exprs[2];
        return true;
    }

    @Override
    protected void execute(Event e) {
        ZoneManager.addZone(name.getSingle(e), l1.getSingle(e), l2.getSingle(e));
    }

    @Override public String toString(Event e, boolean d) { return "create zone"; }
}