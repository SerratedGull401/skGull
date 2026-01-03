package org.serratedgull.skGull.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class EvtZoneBoundary extends SkriptEvent {

    static {
        // Register the event syntax
        Skript.registerEvent("Zone Boundary", EvtZoneBoundary.class, EvtZoneBoundaryEvent.class,
                "zone (1:enter|2:exit) [of %string%]");

        // Modern registration using Lambdas and public fields
        EventValues.registerEventValue(EvtZoneBoundaryEvent.class, Entity.class, e -> e.entity, 0);
        EventValues.registerEventValue(EvtZoneBoundaryEvent.class, String.class, e -> e.zoneName, 0);
    }

    private Literal<String> requiredZone;
    private int type; // 1 for enter, 2 for exit

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        type = parseResult.hasTag("1") ? 1 : 2;
        requiredZone = (Literal<String>) args[0];
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof EvtZoneBoundaryEvent ev)) return false;

        // Check if the trigger (enter vs exit) matches the script
        if (type == 1 && !ev.entering) return false;
        if (type == 2 && ev.entering) return false;

        // Check if the zone name matches (if one was specified)
        if (requiredZone != null) {
            String name = requiredZone.getSingle(e);
            return name != null && ev.zoneName.equalsIgnoreCase(name);
        }

        return true;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        String action = (type == 1 ? "enter" : "exit");
        String zone = (requiredZone != null ? " of " + requiredZone.toString(e, debug) : "");
        return "zone " + action + zone;
    }

    // --- The actual Bukkit Event class ---
    // Moved HandlerList to be accessible and ensured fields are public final
    public static class EvtZoneBoundaryEvent extends Event {
        private static final HandlerList handlers = new HandlerList();

        public final Entity entity;
        public final String zoneName;
        public final boolean entering;

        public EvtZoneBoundaryEvent(Entity entity, String zoneName, boolean entering) {
            this.entity = entity;
            this.zoneName = zoneName;
            this.entering = entering;
        }

        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }
    }
}