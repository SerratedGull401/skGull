package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;
import org.serratedgull.skGull.SkGull;

public class EffEntityDoorPower extends Effect {

    static {
        Skript.registerEffect(EffEntityDoorPower.class,
                "allow %entity% to (pass|open) doors",
                "disallow %entity% from (passing|opening) doors");
    }

    private Expression<Entity> entityExpr;
    private boolean allow;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entityExpr = (Expression<Entity>) exprs[0];
        allow = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity entity = entityExpr.getSingle(e);
        if (entity instanceof Mob mob) {
            if (allow) {
                // 1. Metadata for the physical door-opening task
                mob.setMetadata("skgull_open_doors", new FixedMetadataValue(SkGull.getInstance(), true));

                // 2. INCREASE FOLLOW RANGE
                // This is the "Smart" part. By increasing this, the AI looks much further
                // for a path, allowing it to see that a closet is a dead end.
                AttributeInstance followRange = mob.getAttribute(Attribute.FOLLOW_RANGE);
                if (followRange != null) {
                    followRange.setBaseValue(100.0);
                }

                // 3. AI Flags
                // Tells the pathfinder that doors are valid nodes to walk through.
                mob.getPathfinder().setCanOpenDoors(true);
                mob.getPathfinder().setCanPassDoors(true);
            } else {
                mob.removeMetadata("skgull_open_doors", SkGull.getInstance());
                mob.getPathfinder().setCanOpenDoors(false);
                mob.getPathfinder().setCanPassDoors(false);
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return (allow ? "allow " : "disallow ") + "door power for " + entityExpr.toString(e, debug);
    }

    public static void startDoorTask(SkGull plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Mob mob && mob.hasMetadata("skgull_open_doors")) {

                        // We scan a 3x3x2 cube in front of the mob.
                        // This ensures the door opens even if the mob is strafing or approaching at an angle.
                        Location checkLoc = mob.getLocation().add(mob.getLocation().getDirection().multiply(1.5));

                        for (int x = -1; x <= 1; x++) {
                            for (int y = 0; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    Block b = checkLoc.clone().add(x, y, z).getBlock();
                                    if (b.getBlockData() instanceof Door door && !door.isOpen()) {
                                        door.setOpen(true);
                                        b.setBlockData(door);

                                        b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1.2f);

                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            if (b.getBlockData() instanceof Door d) {
                                                d.setOpen(false);
                                                b.setBlockData(d);
                                                b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
                                            }
                                        }, 50L); // Keep open for 2.5s
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 5L);
    }
}