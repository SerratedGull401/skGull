package org.serratedgull.skGull.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerSeeEntity extends SkriptEvent {

    static {
        Skript.registerEvent("Player See Entity", EvtPlayerSeeEntity.class, PlayerSeeEntityEvent.class,
                        "player (see|witness) [a|an] [%-entitytype%] [within %-number% [blocks]]")
                .description("Fires when an entity enters the player's field of view (FOV).")
                .since("1.0.0");

        EventValues.registerEventValue(PlayerSeeEntityEvent.class, Player.class,
                PlayerSeeEntityEvent::getPlayer, 0);
        EventValues.registerEventValue(PlayerSeeEntityEvent.class, Entity.class,
                PlayerSeeEntityEvent::getEntity, 0);
    }

    private Literal<?> entityType;
    private Literal<Number> maxDistance;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        entityType = args[0];
        maxDistance = (Literal<Number>) args[1];
        startTask();
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (!(e instanceof PlayerSeeEntityEvent ev)) return false;

        Player player = ev.getPlayer();
        Entity target = ev.getEntity();

        double limit = (maxDistance != null) ? maxDistance.getSingle().doubleValue() : 30.0;
        if (player.getEyeLocation().distance(target.getLocation()) > limit) return false;

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
        return "player see " + type + dist;
    }

    public static class PlayerSeeEntityEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final Entity seen;

        public PlayerSeeEntityEvent(Player player, Entity seen) {
            this.player = player;
            this.seen = seen;
        }

        public Player getPlayer() { return player; }
        public Entity getEntity() { return seen; }

        @NotNull
        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    private static boolean taskStarted = false;
    private static void startTask() {
        if (taskStarted) return;
        taskStarted = true;

        Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("skGull"), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Vector eyeDir = p.getLocation().getDirection();
                org.bukkit.Location eyeLoc = p.getEyeLocation();

                for (Entity entity : p.getNearbyEntities(30, 30, 30)) {
                    if (p == entity) continue;

                    // 1. Check three points: Feet, Center, and Head
                    // This ensures that even if only the zombie's head is in the corner, it triggers.
                    double height = entity.getHeight();
                    Vector[] points = {
                            entity.getLocation().toVector(), // Feet
                            entity.getLocation().add(0, height / 2.0, 0).toVector(), // Center
                            entity.getLocation().add(0, height, 0).toVector() // Head
                    };

                    boolean seen = false;
                    for (Vector point : points) {
                        Vector toPoint = point.subtract(eyeLoc.toVector()).normalize();
                        double dot = eyeDir.dot(toPoint);

                        // 0.5 is roughly 120 degrees total.
                        // This is wide enough to catch the corners of a 70 FOV screen.
                        if (dot > 0.5) {
                            if (p.hasLineOfSight(entity)) {
                                seen = true;
                                break;
                            }
                        }
                    }

                    if (seen) {
                        Bukkit.getPluginManager().callEvent(new PlayerSeeEntityEvent(p, entity));
                    }
                }
            }
        }, 20L, 2L);
    }
}