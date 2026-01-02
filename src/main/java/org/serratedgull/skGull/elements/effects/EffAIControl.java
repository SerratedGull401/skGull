package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Effect to enable or disable entity AI
 *
 * Syntax:
 * - (enable|turn on) [the] ai of %entities%
 * - (disable|turn off) [the] ai of %entities%
 *
 * Examples:
 * - disable ai of target entity
 * - enable the ai of all zombies in radius 10
 */
public class EffAIControl extends Effect {

    static {
        Skript.registerEffect(EffAIControl.class,
                        "(enable|turn on) [the] ai of %entities%",
                        "(disable|turn off) [the] ai of %entities%");
    }

    private Expression<Entity> entities;
    private boolean enable;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        enable = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity[] entityArray = entities.getArray(e);

        for (Entity entity : entityArray) {
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                mob.setAware(enable);
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return (enable ? "enable" : "disable") + " ai of " + entities.toString(e, debug);
    }
}