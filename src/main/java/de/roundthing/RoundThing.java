/**
 * The main class of the RoundThing plugin. It handles plugin startup and shutdown,
 * manages player data, schedules particle rendering tasks, and registers all commands
 * and event listeners. It also provides compatibility for both Paper and Folia servers.
 *
 * @author Silas Hörz
 * @version 1.0
 */
package de.roundthing;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoundThing extends JavaPlugin implements Listener {

    private int particleLimit;
    private boolean isFoliaServer = false;
    private Object particleTask = null;

    /**
     * A container class to hold all shapes for a single player.
     */
    public static class PlayerShapes {
        public final Map<String, ParticleCircle> circles = new HashMap<>();
        public final Map<String, ParticleSphere> spheres = new HashMap<>();
        public final Map<String, ParticleLine> lines = new HashMap<>(); // ADDED: For lines
        public int currentParticleCount = 0;
    }

    private final Map<UUID, PlayerShapes> allPlayerShapes = new ConcurrentHashMap<>();
    private StorageManager storageManager;
    private LocaleManager localeManager;

    @Override
    public void onEnable() {
        // Check if the server is running Folia by looking for a Folia-specific class
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            this.isFoliaServer = true;
            getLogger().info("Folia server detected. Using GlobalRegionScheduler.");
        } catch (ClassNotFoundException e) {
            this.isFoliaServer = false;
            getLogger().info("Paper/Spigot server detected. Using BukkitScheduler.");
        }

        saveDefaultConfig();
        reloadPluginConfig();

        this.storageManager = new StorageManager(this);
        this.localeManager = new LocaleManager(this);

        // Command initializations
        CircleCommand circleCommand = new CircleCommand(this, localeManager);
        SphereCommand sphereCommand = new SphereCommand(this, circleCommand, localeManager);
        LineCommand lineCommand = new LineCommand(this, circleCommand, localeManager); // ADDED: Line command
        AdminCommand adminCommand = new AdminCommand(this);

        // Command Registrations
        getCommand("c").setExecutor(circleCommand);
        getCommand("c").setTabCompleter(new CircleTabCompleter(this, circleCommand));
        getCommand("s").setExecutor(sphereCommand);
        getCommand("s").setTabCompleter(new SphereTabCompleter(this, circleCommand));
        getCommand("l").setExecutor(lineCommand); // ADDED: Register line command
        getCommand("l").setTabCompleter(new LineTabCompleter(this, circleCommand)); // ADDED: Register line tab completer
        getCommand("roundthing").setExecutor(adminCommand);
        getCommand("roundthing").setTabCompleter(new AdminTabCompleter());

        Bukkit.getPluginManager().registerEvents(this, this);

        // Load data for any players who are already online (e.g., after a /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }

        // This is the main task that draws all particles for all shapes
        Runnable particleRunnable = () -> {
            for (PlayerShapes shapes : allPlayerShapes.values()) {
                for (ParticleCircle circle : shapes.circles.values()) circle.draw();
                for (ParticleSphere sphere : shapes.spheres.values()) sphere.draw();
                for (ParticleLine line : shapes.lines.values()) line.draw(); // ADDED: Draw lines
            }
        };

        // Start the correct scheduler based on the server type
        if (isFoliaServer) {
            this.particleTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, (task) -> particleRunnable.run(), 20L, 10L);
        } else {
            this.particleTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, particleRunnable, 20L, 10L);
        }

        getLogger().info("RoundThing has been enabled.");
    }

    @Override
    public void onDisable() {
        // Cancel the correct scheduler task based on its type
        if (this.particleTask != null) {
            if (isFoliaServer && this.particleTask instanceof ScheduledTask) {
                ((ScheduledTask) this.particleTask).cancel();
            } else if (!isFoliaServer && this.particleTask instanceof BukkitTask) {
                ((BukkitTask) this.particleTask).cancel();
            }
        }

        for (UUID uuid : allPlayerShapes.keySet()) {
            savePlayerData(uuid);
        }
        allPlayerShapes.clear();
        getLogger().info("RoundThing has been disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerData(event.getPlayer().getUniqueId());
        allPlayerShapes.remove(event.getPlayer().getUniqueId());
    }

    public void loadPlayerData(UUID uuid) {
        // UPDATED: Now loads all shape types
        PlayerShapes shapes = storageManager.loadPlayerShapes(uuid);

        // Recalculate the initial particle budget including all shape types
        int totalParticles = 0;
        for (ParticleCircle circle : shapes.circles.values()) totalParticles += circle.getParticleCount();
        for (ParticleSphere sphere : shapes.spheres.values()) totalParticles += sphere.getParticleCount();
        for (ParticleLine line : shapes.lines.values()) totalParticles += line.getParticleCount(); // ADDED: Count line particles
        shapes.currentParticleCount = totalParticles;

        allPlayerShapes.put(uuid, shapes);
        getLogger().info("Loaded data for player " + uuid + " (" + totalParticles + " particles).");
    }

    public void savePlayerData(UUID uuid) {
        PlayerShapes shapes = allPlayerShapes.get(uuid);
        if (shapes != null) {
            // UPDATED: Saves all shapes at once
            storageManager.savePlayerShapes(uuid, shapes);
            getLogger().info("Saved data for player " + uuid + ".");
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        this.particleLimit = getConfig().getInt("particle-limit", 10000);
    }

    public int getParticleLimit() {
        return this.particleLimit;
    }



    public void setParticleLimit(int limit) {
        this.particleLimit = limit;
    }

    public PlayerShapes getPlayerShapes(UUID uuid) {
        return allPlayerShapes.computeIfAbsent(uuid, k -> new PlayerShapes());
    }

    public Map<String, ParticleCircle> getPlayerCircles(UUID uuid) {
        return getPlayerShapes(uuid).circles;
    }

    public Map<String, ParticleSphere> getPlayerSpheres(UUID uuid) {
        return getPlayerShapes(uuid).spheres;
    }

    // ADDED: Getter for lines
    public Map<String, ParticleLine> getPlayerLines(UUID uuid) {
        return getPlayerShapes(uuid).lines;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }
}