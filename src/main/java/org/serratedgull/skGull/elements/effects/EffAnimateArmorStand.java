package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;
import org.serratedgull.skGull.SkGull;

public class EffAnimateArmorStand extends Effect {

    static {
        // Indices: 0:head, 1:body, 2:left arm, 3:right arm, 4:left leg, 5:right leg
        Skript.registerEffect(EffAnimateArmorStand.class,
                "animate %entity%'s [part] %number% to %number%, %number%, %number% over %number% ticks");
    }

    private Expression<Entity> standExpr;
    private Expression<Number> partIndexExpr;
    private Expression<Number> xExpr, yExpr, zExpr, tickExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        standExpr = (Expression<Entity>) exprs[0];
        partIndexExpr = (Expression<Number>) exprs[1];
        xExpr = (Expression<Number>) exprs[2];
        yExpr = (Expression<Number>) exprs[3];
        zExpr = (Expression<Number>) exprs[4];
        tickExpr = (Expression<Number>) exprs[5];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity entity = standExpr.getSingle(e);
        if (!(entity instanceof ArmorStand stand)) return;

        int partIndex = partIndexExpr.getSingle(e).intValue();
        // Convert degrees from Skript to Radians for Minecraft
        double targetX = Math.toRadians(xExpr.getSingle(e).doubleValue());
        double targetY = Math.toRadians(yExpr.getSingle(e).doubleValue());
        double targetZ = Math.toRadians(zExpr.getSingle(e).doubleValue());
        int ticks = tickExpr.getSingle(e).intValue();

        if (ticks <= 0) ticks = 1;

        final EulerAngle startPos = getPose(stand, partIndex);
        final int finalTicks = ticks;

        new org.bukkit.scheduler.BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (elapsed >= finalTicks || stand.isDead()) {
                    this.cancel();
                    return;
                }

                double pct = (double) elapsed / finalTicks;
                double curX = startPos.getX() + (targetX - startPos.getX()) * pct;
                double curY = startPos.getY() + (targetY - startPos.getY()) * pct;
                double curZ = startPos.getZ() + (targetZ - startPos.getZ()) * pct;

                EulerAngle current = new EulerAngle(curX, curY, curZ);
                setPose(stand, partIndex, current);

                elapsed++;
            }
        }.runTaskTimer(SkGull.getInstance(), 0L, 1L);
    }

    private EulerAngle getPose(ArmorStand stand, int index) {
        return switch (index) {
            case 0 -> stand.getHeadPose();
            case 1 -> stand.getBodyPose();
            case 2 -> stand.getLeftArmPose();
            case 3 -> stand.getRightArmPose();
            case 4 -> stand.getLeftLegPose();
            case 5 -> stand.getRightLegPose();
            default -> stand.getHeadPose();
        };
    }

    private void setPose(ArmorStand stand, int index, EulerAngle angle) {
        switch (index) {
            case 0 -> stand.setHeadPose(angle);
            case 1 -> stand.setBodyPose(angle);
            case 2 -> stand.setLeftArmPose(angle);
            case 3 -> stand.setRightArmPose(angle);
            case 4 -> stand.setLeftLegPose(angle);
            case 5 -> stand.setRightLegPose(angle);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) { return "animate armor stand"; }
}