package org.serratedgull.skGull.elements.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PlayerLookingAtEntityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity targetEntity;
    private final Vector hitLocation;

    public PlayerLookingAtEntityEvent(Player player, Entity targetEntity, Vector hitLocation) {
        this.player = player;
        this.targetEntity = targetEntity;
        this.hitLocation = hitLocation;
    }

    public Player getPlayer() { return player; }
    public Entity getTargetEntity() { return targetEntity; }
    public Vector getHitLocation() { return hitLocation; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}