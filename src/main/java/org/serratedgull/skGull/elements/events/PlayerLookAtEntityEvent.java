package org.serratedgull.skGull.elements.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * This MUST be in its own file named PlayerLookAtEntityEvent.java
 * and it MUST be public so SkGull.java and ExprAIState.java can see it.
 */
public class PlayerLookAtEntityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity targetEntity;
    private final Vector hitLocation;

    public PlayerLookAtEntityEvent(Player player, Entity targetEntity, Vector hitLocation) {
        this.player = player;
        this.targetEntity = targetEntity;
        this.hitLocation = hitLocation;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public Vector getHitLocation() {
        return hitLocation;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}