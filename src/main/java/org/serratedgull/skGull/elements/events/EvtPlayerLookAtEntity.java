package org.serratedgull.skGull.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;

public class EvtPlayerLookAtEntity extends SkriptEvent {

    static {
        Skript.registerEvent("Player Look At Entity", EvtPlayerLookAtEntity.class, PlayerLookAtEntityEvent.class,
                        "player look[ing] at [%-entitytype%] [within %-number% [blocks]]")
                .description("Triggers when a player starts looking at an entity within a certain distance.")
                .since("1.0.0");

        EventValues.registerEventValue(PlayerLookAtEntityEvent.class, Player.class,
                PlayerLookAtEntityEvent::getPlayer, 0);
        EventValues.registerEventValue(PlayerLookAtEntityEvent.class, Entity.class,
                PlayerLookAtEntityEvent::getTargetEntity, 0);
        EventValues.registerEventValue(PlayerLookAtEntityEvent.class, Vector.class,
                PlayerLookAtEntityEvent::getHitLocation, 0);
    }

    private Literal<?> entityType;
    private Literal<Number> maxDistance;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        entityType = args[0];
        maxDistance = (Literal<Number>) args[1];
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof PlayerLookAtEntityEvent || e instanceof PlayerLookingAtEntityEvent)) return false;

        // Determine which event we are dealing with
        Player player;
        Entity target;
        if (e instanceof PlayerLookAtEntityEvent ev) {
            player = ev.getPlayer();
            target = ev.getTargetEntity();
        } else {
            PlayerLookingAtEntityEvent ev = (PlayerLookingAtEntityEvent) e;
            player = ev.getPlayer();
            target = ev.getTargetEntity();
        }

        // DISTANCE LOGIC
        double actual = player.getEyeLocation().distance(target.getLocation());
        // Use the user's distance, OR default to 5 blocks if they didn't specify one
        double limit = (maxDistance != null) ? maxDistance.getSingle().doubleValue() : 5.0;

        if (actual > limit) return false;

        // ENTITY TYPE LOGIC
        if (entityType == null) return true;
        return entityType.check(e, type -> {
            if (type instanceof EntityType et) return et.isInstance(target);
            if (type instanceof EntityData ed) return ed.isInstance(target);
            return false;
        });
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        String type = (entityType != null ? entityType.toString(e, debug) : "entity");
        String dist = (maxDistance != null ? " within " + maxDistance.toString(e, debug) + " blocks" : "");
        return "player look at " + type + dist;
    }
}