package de.roundthing;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class LineCommand implements CommandExecutor {

    private final RoundThing plugin;
    private final CircleCommand circleCommand;
    private final LocaleManager localeManager;

    public LineCommand(RoundThing plugin, CircleCommand circleCommand, LocaleManager localeManager) {
        this.plugin = plugin;
        this.circleCommand = circleCommand;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("en_US", "command_from_console"));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create": handleCreate(player, args); break;
            case "delete": handleDelete(player, args); break;
            case "list": handleList(player); break;
            case "help": default: sendHelpMessage(player); break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        // NEUE STRUKTUR: /l create <name> <x1> <y1> <z1> <x2> <y2> <z2> [color]
        if (args.length < 8) {
            sendHelpMessage(player);
            return;
        }
        try {
            String name = args[1];
            UUID uuid = player.getUniqueId();

            Location start = new Location(player.getWorld(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
            Location end = new Location(player.getWorld(), Double.parseDouble(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7]));
            Color color = Color.LIME;

            if (args.length >= 9) {
                color = circleCommand.parseColor(args[8]);
                if (color == null) {
                    localeManager.sendMessage(player, "invalid_color");
                    return;
                }
            }

            ParticleLine testLine = new ParticleLine(start, end, color);
            int newParticleCount = testLine.getParticleCount();
            RoundThing.PlayerShapes playerShapes = plugin.getPlayerShapes(uuid);

            int oldParticleCount = 0;
            if (playerShapes.lines.containsKey(name)) {
                oldParticleCount = playerShapes.lines.get(name).getParticleCount();
            }

            if (playerShapes.currentParticleCount - oldParticleCount + newParticleCount > plugin.getParticleLimit()) {
                localeManager.sendMessage(player, "budget_exceeded", "%new%", String.valueOf(newParticleCount), "%current%", String.valueOf(playerShapes.currentParticleCount - oldParticleCount), "%limit%", String.valueOf(plugin.getParticleLimit()));
                return;
            }

            playerShapes.lines.put(name, testLine);
            playerShapes.currentParticleCount = playerShapes.currentParticleCount - oldParticleCount + newParticleCount;
            plugin.savePlayerData(uuid);
            localeManager.sendMessage(player, "line_success", "%name%", name);

        } catch (NumberFormatException e) {
            localeManager.sendMessage(player, "invalid_number");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length != 2) {
            sendHelpMessage(player);
            return;
        }
        UUID uuid = player.getUniqueId();
        String name = args[1];
        RoundThing.PlayerShapes playerShapes = plugin.getPlayerShapes(uuid);
        Map<String, ParticleLine> playerLines = playerShapes.lines;

        if (name.equalsIgnoreCase("all")) {
            if (playerLines.isEmpty()) {
                localeManager.sendMessage(player, "line_list_empty");
                return;
            }
            playerShapes.currentParticleCount -= playerLines.values().stream().mapToInt(ParticleLine::getParticleCount).sum();
            playerLines.clear();
            localeManager.sendMessage(player, "line_deleted_all");
        } else {
            ParticleLine removedLine = playerLines.remove(name);
            if (removedLine == null) {
                localeManager.sendMessage(player, "line_not_found");
                return;
            } else {
                playerShapes.currentParticleCount -= removedLine.getParticleCount();
                localeManager.sendMessage(player, "line_deleted", "%name%", name);
            }
        }
        plugin.savePlayerData(uuid);
    }

    private void handleList(Player player) {
        Map<String, ParticleLine> playerLines = plugin.getPlayerShapes(player.getUniqueId()).lines;
        if (playerLines.isEmpty()) {
            localeManager.sendMessage(player, "line_list_empty");
            return;
        }

        localeManager.sendMessage(player, "line_list_header");
        for (String name : playerLines.keySet()) {
            player.sendMessage(ChatColor.AQUA + "- " + name);
        }
    }

    private void sendHelpMessage(Player player) {
        localeManager.sendMessage(player, "help_header");
        localeManager.sendMessage(player, "help_create_line");
        localeManager.sendMessage(player, "help_create_desc");
        localeManager.sendMessage(player, "help_delete");
        localeManager.sendMessage(player, "help_delete_desc");
        localeManager.sendMessage(player, "help_list");
        localeManager.sendMessage(player, "help_list_desc");
        localeManager.sendMessage(player, "help_help");
        localeManager.sendMessage(player, "help_help_desc");
        localeManager.sendMessage(player, "help_footer");
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try { Double.parseDouble(str); return true; }
        catch (NumberFormatException e) { return false; }
    }
}