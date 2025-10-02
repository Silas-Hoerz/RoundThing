package de.roundthing;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final RoundThing plugin;
    private final File dataFolder;

    public StorageManager(RoundThing plugin) {
        this.plugin = plugin;
        // Wir erstellen einen Unterordner für die Spielerdaten
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void savePlayerCircles(UUID playerUUID, Map<String, ParticleCircle> circles) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        // Bestehende "circles" Sektion löschen, um alte Kreise zu entfernen
        config.set("circles", null);

        for (Map.Entry<String, ParticleCircle> entry : circles.entrySet()) {
            String name = entry.getKey();
            ParticleCircle circle = entry.getValue();
            String path = "circles." + name;

            config.set(path + ".world", circle.getWorld().getName());
            config.set(path + ".x", circle.getCenterX());
            config.set(path + ".y", circle.getCenterY());
            config.set(path + ".z", circle.getCenterZ());
            config.set(path + ".diameter", circle.getDiameter());
            config.set(path + ".thickness", circle.getThickness());
            config.set(path + ".color", circle.getColor().asRGB()); // Speichern als RGB-Zahl
            config.set(path + ".rotX", circle.getRotationX());
            config.set(path + ".rotZ", circle.getRotationZ());
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte die Daten für Spieler " + playerUUID + " nicht speichern!");
            e.printStackTrace();
        }
    }

    public Map<String, ParticleCircle> loadPlayerCircles(UUID playerUUID) {
        Map<String, ParticleCircle> circles = new HashMap<>();
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");

        if (!playerFile.exists()) {
            return circles; // Keine Datei, keine Kreise
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        ConfigurationSection circlesSection = config.getConfigurationSection("circles");

        if (circlesSection == null) {
            return circles; // Keine "circles" Sektion
        }

        for (String name : circlesSection.getKeys(false)) {
            String path = name;
            World world = Bukkit.getWorld(circlesSection.getString(path + ".world"));
            if (world == null) continue; // Welt nicht geladen, Kreis überspringen

            double x = circlesSection.getDouble(path + ".x");
            double y = circlesSection.getDouble(path + ".y");
            double z = circlesSection.getDouble(path + ".z");
            double diameter = circlesSection.getDouble(path + ".diameter");
            int thickness = circlesSection.getInt(path + ".thickness");
            Color color = Color.fromRGB(circlesSection.getInt(path + ".color"));
            double rotX = circlesSection.getDouble(path + ".rotX");
            double rotZ = circlesSection.getDouble(path + ".rotZ");

            Location center = new Location(world, x, y, z);
            ParticleCircle circle = new ParticleCircle(center, diameter, thickness, color, rotX, rotZ);
            circles.put(name, circle);
        }
        return circles;
    }

    public void savePlayerSpheres(UUID playerUUID, Map<String, ParticleSphere> spheres) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        config.set("spheres", null); // Alte Kugel-Daten löschen

        for (Map.Entry<String, ParticleSphere> entry : spheres.entrySet()) {
            String name = entry.getKey();
            ParticleSphere sphere = entry.getValue();
            String path = "spheres." + name;

            config.set(path + ".world", sphere.getWorld().getName());
            config.set(path + ".x", sphere.getCenterX());
            config.set(path + ".y", sphere.getCenterY());
            config.set(path + ".z", sphere.getCenterZ());
            config.set(path + ".diameter", sphere.getDiameter());
            config.set(path + ".thickness", sphere.getThickness());
            config.set(path + ".color", sphere.getColor().asRGB());
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte Kugel-Daten für Spieler " + playerUUID + " nicht speichern!");
            e.printStackTrace();
        }
    }

    // NEUE METHODE
    public Map<String, ParticleSphere> loadPlayerSpheres(UUID playerUUID) {
        Map<String, ParticleSphere> spheres = new HashMap<>();
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        if (!playerFile.exists()) return spheres;

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        ConfigurationSection spheresSection = config.getConfigurationSection("spheres");
        if (spheresSection == null) return spheres;

        for (String name : spheresSection.getKeys(false)) {
            String path = name;
            World world = Bukkit.getWorld(spheresSection.getString(path + ".world"));
            if (world == null) continue;

            double x = spheresSection.getDouble(path + ".x");
            double y = spheresSection.getDouble(path + ".y");
            double z = spheresSection.getDouble(path + ".z");
            double diameter = spheresSection.getDouble(path + ".diameter");
            int thickness = spheresSection.getInt(path + ".thickness");
            Color color = Color.fromRGB(spheresSection.getInt(path + ".color"));

            Location center = new Location(world, x, y, z);
            ParticleSphere sphere = new ParticleSphere(center, diameter, thickness, color);
            spheres.put(name, sphere);
        }
        return spheres;
    }

}