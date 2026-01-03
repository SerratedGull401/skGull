package org.serratedgull.skGull.objects;

import org.bukkit.Location;

public class ContainmentZone {
    // Changing these to public allows other classes to see them without getters
    public final String name;
    public final Location l1, l2;

    public ContainmentZone(String name, Location l1, Location l2) {
        this.name = name;
        this.l1 = l1;
        this.l2 = l2;
    }

    public boolean contains(Location loc) {
        if (loc == null || !loc.getWorld().equals(l1.getWorld())) return false;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= Math.min(l1.getX(), l2.getX()) && x <= Math.max(l1.getX(), l2.getX()) &&
                y >= Math.min(l1.getY(), l2.getY()) && y <= Math.max(l1.getY(), l2.getY()) &&
                z >= Math.min(l1.getZ(), l2.getZ()) && z <= Math.max(l1.getZ(), l2.getZ());
    }
}