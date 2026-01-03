package org.serratedgull.skGull.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import org.serratedgull.skGull.SkGull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffEmitSound extends Effect {

    static {
        Skript.registerEffect(EffEmitSound.class,
                "emit %string% from %entity% every %timespan% [with volume %-number%] [with pitch %-number%]",
                "stop emitting [sounds] from %entity%");
    }

    private Expression<String> soundName;
    private Expression<Entity> targetEntity;
    private Expression<ch.njol.skript.util.Timespan> interval;
    private Expression<Number> volume;
    private Expression<Number> pitch;
    private boolean stop = false;

    // Internal tracking maps
    private static final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private static final Map<UUID, String> activeSoundNames = new HashMap<>();

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern == 1) { // "stop emitting..."
            targetEntity = (Expression<Entity>) exprs[0];
            stop = true;
            return true;
        }
        soundName = (Expression<String>) exprs[0];
        targetEntity = (Expression<Entity>) exprs[1];
        interval = (Expression<ch.njol.skript.util.Timespan>) exprs[2];
        volume = (Expression<Number>) exprs[3];
        pitch = (Expression<Number>) exprs[4];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity target = targetEntity.getSingle(e);
        if (target == null) return;

        UUID uuid = target.getUniqueId();

        // 1. Handle stopping
        if (stop) {
            BukkitTask task = activeTasks.remove(uuid);
            if (task != null) task.cancel();
            activeSoundNames.remove(uuid);
            return;
        }

        String sound = soundName.getSingle(e);
        if (sound == null) return;

        // 2. THE SMART CHECK: If this exact sound is already looping, DO NOTHING.
        if (activeTasks.containsKey(uuid) && sound.equals(activeSoundNames.get(uuid))) {
            return;
        }

        ch.njol.skript.util.Timespan ts = interval.getSingle(e);
        if (ts == null) return;

        // FIXED: Using non-deprecated getTicks() and casting to long
        long ticks = Math.max(1, (long) ts.getTicks());

        float vol = volume != null ? volume.getSingle(e).floatValue() : 1.0f;
        float pit = pitch != null ? pitch.getSingle(e).floatValue() : 1.0f;

        // 3. If it's a DIFFERENT sound, cancel the old one first
        BukkitTask existingTask = activeTasks.remove(uuid);
        if (existingTask != null) existingTask.cancel();

        // 4. Start the loop using the main class instance
        BukkitTask newTask = Bukkit.getScheduler().runTaskTimer(SkGull.getInstance(), () -> {
            // Task self-cleanup logic
            if (!target.isValid() || target.isDead() || !activeTasks.containsKey(uuid)) {
                BukkitTask t = activeTasks.remove(uuid);
                if (t != null) t.cancel();
                activeSoundNames.remove(uuid);
                return;
            }
            target.getWorld().playSound(target.getLocation(), sound, vol, pit);
        }, 0L, ticks);

        activeTasks.put(uuid, newTask);
        activeSoundNames.put(uuid, sound);
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return stop ? "stop emitting sound" : "emit sound from entity";
    }

    public static void cleanupAll() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeTasks.clear();
        activeSoundNames.clear();
    }
}