package org.serratedgull.skGull.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.serratedgull.skGull.managers.ZoneManager;
import org.serratedgull.skGull.objects.ContainmentZone;
import org.serratedgull.skGull.elements.events.EvtZoneBoundary;


public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        // Check if they actually moved to a different block to save CPU
        if (to == null || (from.getBlockX() == to.getBlockX() &&
                from.getBlockZ() == to.getBlockZ() &&
                from.getBlockY() == to.getBlockY())) return;

        // Loop through the list of zone objects
        for (ContainmentZone zone : ZoneManager.getZones()) {
            boolean wasIn = zone.contains(from);
            boolean isIn = zone.contains(to);

            if (wasIn != isIn) {
                // Call the internal static event class
                Bukkit.getPluginManager().callEvent(new EvtZoneBoundary.EvtZoneBoundaryEvent(
                        e.getPlayer(),
                        zone.name,
                        isIn
                ));
            }
        }
    }
}