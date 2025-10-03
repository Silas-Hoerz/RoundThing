/**
 * Manages the persistence of player shape data by saving to and loading from YAML files.
 * Each player's data is stored in a separate file named after their UUID.
 *
 * @author Silas Hörz
 * @version 1.0
 */
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
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final RoundThing plugin;
    private final File dataFolder;

    public StorageManager(RoundThing plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Saves all shapes (circles, spheres, lines) for a specific player to their data file.
     * This is an atomic operation that overwrites the file with the current in-memory state.
     */
    public void savePlayerShapes(UUID playerUUID, RoundThing.PlayerShapes shapes) {
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration(); // Create a fresh config

        // Save Circles
        if (shapes.circles != null && !shapes.circles.isEmpty()) {
            for (Map.Entry<String, ParticleCircle> entry : shapes.circles.entrySet()) {
                String name = entry.getKey();
                ParticleCircle circle = entry.getValue();
                String path = "circles." + name;
                config.set(path + ".world", circle.getWorld().getName());
                config.set(path + ".x", circle.getCenterX());
                config.set(path + ".y", circle.getCenterY());
                config.set(path + ".z", circle.getCenterZ());
                config.set(path + ".diameter", circle.getDiameter());
                config.set(path + ".thickness", circle.getThickness());
                config.set(path + ".color", circle.getColor().asRGB());
                config.set(path + ".rotX", circle.getRotationX());
                config.set(path + ".rotZ", circle.getRotationZ());
            }
        }

        // Save Spheres
        if (shapes.spheres != null && !shapes.spheres.isEmpty()) {
            for (Map.Entry<String, ParticleSphere> entry : shapes.spheres.entrySet()) {
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
        }

        // Save Lines
        if (shapes.lines != null && !shapes.lines.isEmpty()) {
            for (Map.Entry<String, ParticleLine> entry : shapes.lines.entrySet()) {
                String name = entry.getKey();
                ParticleLine line = entry.getValue();
                String path = "lines." + name;
                config.set(path + ".world", line.getWorld().getName());
                config.set(path + ".start.x", line.getStart().getX());
                config.set(path + ".start.y", line.getStart().getY());
                config.set(path + ".start.z", line.getStart().getZ());
                config.set(path + ".end.x", line.getEnd().getX());
                config.set(path + ".end.y", line.getEnd().getY());
                config.set(path + ".end.z", line.getEnd().getZ());
                config.set(path + ".color", line.getColor().asRGB());
            }
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data for player " + playerUUID);
            e.printStackTrace();
        }
    }

    /**
     * Loads all shapes for a specific player from their data file.
     * @return A PlayerShapes object containing all loaded shapes.
     */
    public RoundThing.PlayerShapes loadPlayerShapes(UUID playerUUID) {
        RoundThing.PlayerShapes shapes = new RoundThing.PlayerShapes();
        File playerFile = new File(dataFolder, playerUUID.toString() + ".yml");
        if (!playerFile.exists()) {
            return shapes;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        // Load Circles
        ConfigurationSection circlesSection = config.getConfigurationSection("circles");
        if (circlesSection != null) {
            for (String name : circlesSection.getKeys(false)) {
                World world = Bukkit.getWorld(circlesSection.getString(name + ".world"));
                if (world == null) continue;
                Location center = new Location(world, circlesSection.getDouble(name + ".x"), circlesSection.getDouble(name + ".y"), circlesSection.getDouble(name + ".z"));
                double diameter = circlesSection.getDouble(name + ".diameter");
                int thickness = circlesSection.getInt(name + ".thickness");
                Color color = Color.fromRGB(circlesSection.getInt(name + ".color"));
                double rotX = circlesSection.getDouble(name + ".rotX");
                double rotZ = circlesSection.getDouble(name + ".rotZ");
                shapes.circles.put(name, new ParticleCircle(center, diameter, thickness, color, rotX, rotZ));
            }
        }

        // Load Spheres
        ConfigurationSection spheresSection = config.getConfigurationSection("spheres");
        if (spheresSection != null) {
            for (String name : spheresSection.getKeys(false)) {
                World world = Bukkit.getWorld(spheresSection.getString(name + ".world"));
                if (world == null) continue;
                Location center = new Location(world, spheresSection.getDouble(name + ".x"), spheresSection.getDouble(name + ".y"), spheresSection.getDouble(name + ".z"));
                double diameter = spheresSection.getDouble(name + ".diameter");
                int thickness = spheresSection.getInt(name + ".thickness");
                Color color = Color.fromRGB(spheresSection.getInt(name + ".color"));
                shapes.spheres.put(name, new ParticleSphere(center, diameter, thickness, color));
            }
        }

        // Load Lines
        ConfigurationSection linesSection = config.getConfigurationSection("lines");
        if (linesSection != null) {
            for (String name : linesSection.getKeys(false)) {
                World world = Bukkit.getWorld(linesSection.getString(name + ".world"));
                if (world == null) continue;
                Location start = new Location(world, linesSection.getDouble(name + ".start.x"), linesSection.getDouble(name + ".start.y"), linesSection.getDouble(name + ".start.z"));
                Location end = new Location(world, linesSection.getDouble(name + ".end.x"), linesSection.getDouble(name + ".end.y"), linesSection.getDouble(name + ".end.z"));
                Color color = Color.fromRGB(linesSection.getInt(name + ".color"));
                shapes.lines.put(name, new ParticleLine(start, end, color));
            }
        }

        return shapes;
    }
}