/**
 * Manages plugin localization by loading and providing access to language files.
 * It automatically detects the player's client language and serves the appropriate translations.
 *
 * @author Silas Hörz
 * @version 1.0
 */
package de.roundthing;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LocaleManager {

    private final RoundThing plugin;
    private final Map<String, FileConfiguration> locales = new HashMap<>();
    private final String defaultLocale = "en_US";

    public LocaleManager(RoundThing plugin) {
        this.plugin = plugin;
        loadLocales();
    }

    private void loadLocales() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Save all default language files from the JAR if they don't exist
        saveDefaultLocale("en_US");
        saveDefaultLocale("de_DE");
        // Add any other languages you've translated here, e.g., saveDefaultLocale("es_ES");

        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles == null) {
            plugin.getLogger().severe("Could not read from lang folder!");
            return;
        }

        for (File langFile : langFiles) {
            String localeName = langFile.getName().replace(".yml", "");
            locales.put(localeName, YamlConfiguration.loadConfiguration(langFile));
            plugin.getLogger().info("Loaded locale: " + localeName);
        }
    }

    private void saveDefaultLocale(String localeName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + localeName + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + localeName + ".yml", false);
        }
    }

    public String getMessage(String locale, String key) {
        FileConfiguration config = locales.getOrDefault(locale, locales.get(defaultLocale));
        String message = config.getString(key);

        if (message == null) {
            // Fallback to the default language if the key is missing in the target language
            plugin.getLogger().warning(String.format("Missing translation key '%s' for locale '%s'.", key, locale));
            config = locales.get(defaultLocale);
            message = config.getString(key, "&cMissing translation for key: " + key);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(player.getLocale(), key));
    }

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