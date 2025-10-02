package de.roundthing;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocaleManager {

    private final RoundThing plugin;
    private final Map<String, FileConfiguration> locales = new HashMap<>();
    private final String defaultLocale = "en_US";

    public LocaleManager(RoundThing plugin) {
        this.plugin = plugin;
        // Lade die Sprachdateien, die wir im JAR haben
        loadLocale("en_US");
        loadLocale("de_DE");
    }

    private void loadLocale(String localeName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + localeName + ".yml");
        if (!langFile.exists()) {
            // Kopiert die Standard-Sprachdatei aus dem JAR in den Plugin-Ordner
            plugin.saveResource("lang/" + localeName + ".yml", false);
        }
        locales.put(localeName, YamlConfiguration.loadConfiguration(langFile));
    }

    public String getMessage(String locale, String key) {
        FileConfiguration config = locales.getOrDefault(locale, locales.get(defaultLocale));
        String message = config.getString(key);

        if (message == null) {
            // Fallback auf die Standardsprache, wenn der Schlüssel in der Zielsprache fehlt
            config = locales.get(defaultLocale);
            message = config.getString(key, "&cMissing translation for key: " + key);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(player.getLocale(), key));
    }

    // Nützliche Methode, um Platzhalter wie %name% zu ersetzen
    public void sendMessage(Player player, String key, String... replacements) {
        String message = getMessage(player.getLocale(), key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        player.sendMessage(message);
    }
}