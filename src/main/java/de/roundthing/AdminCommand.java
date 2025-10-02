package de.roundthing;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    private final RoundThing plugin;
    private final LocaleManager localeManager;

    public AdminCommand(RoundThing plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("roundthing.admin")) {
            sendMessage(sender, "admin_no_permission");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "admin_help_header");
            sendMessage(sender, "admin_help_setlimit");
            sendMessage(sender, "admin_help_reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlimit":
                if (args.length != 2) {
                    sendMessage(sender, "admin_usage_setlimit");
                    return true;
                }
                try {
                    int newLimit = Integer.parseInt(args[1]);
                    if (newLimit < 0) {
                        sendMessage(sender, "admin_limit_negative");
                        return true;
                    }
                    plugin.setParticleLimit(newLimit);
                    plugin.getConfig().set("particle-limit", newLimit);
                    plugin.saveConfig();

                    sendMessage(sender, "admin_limit_set", "%limit%", String.valueOf(newLimit));
                    sendMessage(sender, "admin_guidelines_header");
                    sendMessage(sender, "admin_guideline_circle");
                    sendMessage(sender, "admin_guideline_sphere");
                    sendMessage(sender, "admin_guideline_info");

                } catch (NumberFormatException e) {
                    sendMessage(sender, "invalid_number");
                }
                break;

            case "reload":
                plugin.reloadPluginConfig();
                sendMessage(sender, "admin_reload_success", "%limit%", String.valueOf(plugin.getParticleLimit()));
                break;

            default:
                sendMessage(sender, "admin_unknown_command");
                break;
        }
        return true;
    }

    // Kleine Hilfsmethode, um Nachrichten an Spieler und Konsole zu senden
    private void sendMessage(CommandSender sender, String key, String... replacements) {
        if (sender instanceof Player) {
            localeManager.sendMessage((Player) sender, key, replacements);
        } else {
            // Die Konsole hat keine Sprache, also senden wir standardmäßig Englisch
            String message = localeManager.getMessage("en_US", key);
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
            sender.sendMessage(message);
        }
    }
}