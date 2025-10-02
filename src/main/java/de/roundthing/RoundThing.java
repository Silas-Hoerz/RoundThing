package de.roundthing;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoundThing extends JavaPlugin implements Listener {

    private int particleLimit;

    public static class PlayerShapes {
        public final Map<String, ParticleCircle> circles = new HashMap<>();
        public final Map<String, ParticleSphere> spheres = new HashMap<>();
        public int currentParticleCount = 0;
    }

    private final Map<UUID, PlayerShapes> allPlayerShapes = new ConcurrentHashMap<>();
    private StorageManager storageManager;
    private LocaleManager localeManager;
    private ScheduledTask particleTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPluginConfig();

        this.storageManager = new StorageManager(this);
        this.localeManager = new LocaleManager(this);

        CircleCommand circleCommand = new CircleCommand(this, localeManager);
        SphereCommand sphereCommand = new SphereCommand(this, circleCommand, localeManager);
        AdminCommand adminCommand = new AdminCommand(this);

        getCommand("c").setExecutor(circleCommand);
        getCommand("c").setTabCompleter(new CircleTabCompleter(this, circleCommand));
        getCommand("s").setExecutor(sphereCommand);
        getCommand("s").setTabCompleter(new SphereTabCompleter(this, circleCommand));

        getCommand("roundthing").setExecutor(adminCommand);
        getCommand("roundthing").setTabCompleter(new AdminTabCompleter());

        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }

        this.particleTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, (task) -> {
            for (PlayerShapes shapes : allPlayerShapes.values()) {
                for (ParticleCircle circle : shapes.circles.values()) circle.draw();
                for (ParticleSphere sphere : shapes.spheres.values()) sphere.draw();
            }
        }, 20L, 10L);

        getLogger().info("RoundThing wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (this.particleTask != null) this.particleTask.cancel();
        for (UUID uuid : allPlayerShapes.keySet()) savePlayerData(uuid);
        allPlayerShapes.clear();
        getLogger().info("RoundThing wurde deaktiviert.");
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
        PlayerShapes shapes = new PlayerShapes();
        shapes.circles.putAll(storageManager.loadPlayerCircles(uuid));
        shapes.spheres.putAll(storageManager.loadPlayerSpheres(uuid));

        int totalParticles = 0;
        for (ParticleCircle circle : shapes.circles.values()) {
            totalParticles += circle.getParticleCount();
        }
        for (ParticleSphere sphere : shapes.spheres.values()) {
            totalParticles += sphere.getParticleCount();
        }
        shapes.currentParticleCount = totalParticles;

        allPlayerShapes.put(uuid, shapes);
        getLogger().info("Daten für Spieler " + uuid + " geladen (" + totalParticles + " Partikel).");
    }

    public void savePlayerData(UUID uuid) {
        PlayerShapes shapes = allPlayerShapes.get(uuid);
        if (shapes != null) {
            storageManager.savePlayerCircles(uuid, shapes.circles);
            storageManager.savePlayerSpheres(uuid, shapes.spheres);
            getLogger().info("Daten für Spieler " + uuid + " gespeichert.");
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

    public LocaleManager getLocaleManager() {
        return localeManager;
    }
}