package org.serratedgull.skGull.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serratedgull.skGull.SkGull;
import org.serratedgull.skGull.objects.ContainmentZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZoneManager {
    private static final List<ContainmentZone> zones = new ArrayList<>();
    private static File file;
    private static FileConfiguration config;

    public static void setup(SkGull plugin) {
        file = new File(plugin.getDataFolder(), "zones.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadZones();
    }

    public static void addZone(String name, Location l1, Location l2) {
        delete(name);
        zones.add(new ContainmentZone(name, l1, l2));
        saveZoneToConfig(name, l1, l2);
    }

    private static void saveZoneToConfig(String name, Location l1, Location l2) {
        config.set("zones." + name + ".world", l1.getWorld().getName());
        config.set("zones." + name + ".l1", l1.toVector());
        config.set("zones." + name + ".l2", l2.toVector());
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadZones() {
        if (config == null || !config.contains("zones")) return;
        zones.clear();

        for (String name : config.getConfigurationSection("zones").getKeys(false)) {
            String worldName = config.getString("zones." + name + ".world");
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            // Using var to check for null vectors before converting to location
            var vec1 = config.getVector("zones." + name + ".l1");
            var vec2 = config.getVector("zones." + name + ".l2");

            if (vec1 != null && vec2 != null) {
                zones.add(new ContainmentZone(name, vec1.toLocation(world), vec2.toLocation(world)));
            }
        }
    }

    public static void delete(String name) {
        zones.removeIf(zone -> zone.name.equalsIgnoreCase(name));
        if (config.contains("zones." + name)) {
            config.set("zones." + name, null);
            try { config.save(file); } catch (IOException ignored) {}
        }
    }

    // New method to get a list of names for Skript
    public static List<String> getZoneNames() {
        List<String> names = new ArrayList<>();
        for (ContainmentZone zone : zones) {
            names.add(zone.name);
        }
        return names;
    }

    public static List<ContainmentZone> getZones() { return zones; }
}