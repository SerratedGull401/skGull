package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.serratedgull.skGull.managers.ZoneManager;
import org.jetbrains.annotations.Nullable;

public class EffDeleteZone extends Effect {

    static {
        Skript.registerEffect(EffDeleteZone.class, "(delete|remove) containment zone %string%");
    }

    private Expression<String> name;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        name = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event e) {
        String n = name.getSingle(e);
        if (n != null) {
            ZoneManager.delete(n); // Calls the delete method in your manager
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "delete zone " + name.toString(e, debug);
    }
}