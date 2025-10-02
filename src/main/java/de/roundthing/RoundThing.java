package de.roundthing;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask; // WICHTIGER IMPORT

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoundThing extends JavaPlugin implements Listener {

    private int particleLimit;
    private boolean isFolia = false;

    // KORREKTUR: Die Variable muss vom Typ "Object" sein, um beide Task-Typen zu halten.
    private Object particleTask = null;

    public static class PlayerShapes {
        public final Map<String, ParticleCircle> circles = new HashMap<>();
        public final Map<String, ParticleSphere> spheres = new HashMap<>();
        public int currentParticleCount = 0;
    }

    private final Map<UUID, PlayerShapes> allPlayerShapes = new ConcurrentHashMap<>();
    private StorageManager storageManager;
    private LocaleManager localeManager;

    @Override
    public void onEnable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            this.isFolia = true;
            getLogger().info("Folia-Server erkannt. Nutze den GlobalRegionScheduler.");
        } catch (ClassNotFoundException e) {
            this.isFolia = false;
            getLogger().info("Paper/Spigot-Server erkannt. Nutze den BukkitScheduler.");
        }

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

        Runnable particleRunnable = () -> {
            for (PlayerShapes shapes : allPlayerShapes.values()) {
                for (ParticleCircle circle : shapes.circles.values()) circle.draw();
                for (ParticleSphere sphere : shapes.spheres.values()) sphere.draw();
            }
        };

        if (isFolia) {
            // KORREKTUR: Der Task wird korrekt der "Object"-Variable zugewiesen.
            this.particleTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, (task) -> particleRunnable.run(), 20L, 10L);
        } else {
            // KORREKTUR: Der Task wird korrekt der "Object"-Variable zugewiesen.
            this.particleTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, particleRunnable, 20L, 10L);
        }

        getLogger().info("RoundThing wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (this.particleTask != null) {
            // KORREKTUR: Wir prüfen den Typ und casten ihn korrekt, um cancel() aufzurufen.
            if (isFolia && this.particleTask instanceof ScheduledTask) {
                ((ScheduledTask) this.particleTask).cancel();
            } else if (!isFolia && this.particleTask instanceof BukkitTask) {
                ((BukkitTask) this.particleTask).cancel();
            }
        }

        for (UUID uuid : allPlayerShapes.keySet()) savePlayerData(uuid);
        allPlayerShapes.clear();
        getLogger().info("RoundThing wurde deaktiviert.");
    }

    // --- Der Rest der Klasse ist unverändert ---
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