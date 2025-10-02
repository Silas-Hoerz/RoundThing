package de.roundthing;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CircleCommand implements CommandExecutor {

    private final RoundThing plugin;
    private final LocaleManager localeManager;
    private final Map<String, Color> colorMap = new HashMap<>();

    public CircleCommand(RoundThing plugin, LocaleManager localeManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        populateColorMap();
    }

    private void populateColorMap() {
        for (Field field : Color.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(Color.class)) {
                try {
                    Color color = (Color) field.get(null);
                    colorMap.put(field.getName().toLowerCase(), color);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        colorMap.put("rot", Color.RED);
        colorMap.put("grün", Color.GREEN);
        colorMap.put("blau", Color.BLUE);
        colorMap.put("gelb", Color.YELLOW);
        colorMap.put("lila", Color.PURPLE);
        colorMap.put("weiss", Color.WHITE);
        colorMap.put("weiß", Color.WHITE);
    }

    public Map<String, Color> getColorMap() {
        return this.colorMap;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("en_US", "command_from_console"));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sendeHilfe(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create": handleCreate(player, args); break;
            case "delete": handleDelete(player, args); break;
            case "list": handleList(player); break;
            case "help": default: sendeHilfe(player); break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            sendeHilfe(player);
            return;
        }
        try {
            String name = args[1];
            double durchmesser = Double.parseDouble(args[2]);
            UUID uuid = player.getUniqueId();
            Location mittelpunkt = player.getLocation();
            int dicke = 1;
            Color farbe = Color.LIME;
            double rotX = 0.0, rotZ = 0.0;
            int currentIndex = 3;

            if (args.length > currentIndex && isNumeric(args[currentIndex])) {
                dicke = Integer.parseInt(args[currentIndex++]);
            }
            if (args.length > currentIndex + 2 && isNumeric(args[currentIndex]) && isNumeric(args[currentIndex + 1]) && isNumeric(args[currentIndex + 2])) {
                mittelpunkt = new Location(player.getWorld(), Double.parseDouble(args[currentIndex]), Double.parseDouble(args[currentIndex + 1]), Double.parseDouble(args[currentIndex + 2]));
                currentIndex += 3;
            }
            if (args.length > currentIndex && !isNumeric(args[currentIndex])) {
                farbe = parseFarbe(args[currentIndex++]);
                if (farbe == null) {
                    localeManager.sendMessage(player, "invalid_color");
                    String farbenListe = String.join(", ", colorMap.keySet());
                    localeManager.sendMessage(player, "available_colors", "%colors%", farbenListe);
                    return;
                }
            }
            if (args.length > currentIndex) rotX = Double.parseDouble(args[currentIndex++]);
            if (args.length > currentIndex) rotZ = Double.parseDouble(args[currentIndex]);
            if (rotX < -90 || rotX > 90 || rotZ < -90 || rotZ > 90) {
                localeManager.sendMessage(player, "rotation_out_of_bounds");
                return;
            }

            // --- PARTIKEL BUDGET PRÜFUNG ---
            ParticleCircle testKreis = new ParticleCircle(mittelpunkt, durchmesser, dicke, farbe, rotX, rotZ);
            int neuePartikel = testKreis.getParticleCount();
            RoundThing.PlayerShapes playerShapes = plugin.getPlayerShapes(uuid);

            int altePartikel = 0;
            if (playerShapes.circles.containsKey(name)) {
                altePartikel = playerShapes.circles.get(name).getParticleCount();
            }

            if (playerShapes.currentParticleCount - altePartikel + neuePartikel > plugin.getParticleLimit()) {
                localeManager.sendMessage(player, "budget_exceeded",
                        "%new%", String.valueOf(neuePartikel),
                        "%current%", String.valueOf(playerShapes.currentParticleCount - altePartikel),
                        "%limit%", String.valueOf(plugin.getParticleLimit())
                );
                return;
            }

            // Alles gut -> Kreis speichern und Budget aktualisieren
            playerShapes.circles.put(name, testKreis);
            playerShapes.currentParticleCount = playerShapes.currentParticleCount - altePartikel + neuePartikel;
            plugin.savePlayerData(uuid);
            localeManager.sendMessage(player, "circle_success", "%name%", name);

        } catch (NumberFormatException e) {
            localeManager.sendMessage(player, "invalid_number");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length != 2) {
            sendeHilfe(player);
            return;
        }
        UUID uuid = player.getUniqueId();
        String name = args[1];
        RoundThing.PlayerShapes playerShapes = plugin.getPlayerShapes(uuid);
        Map<String, ParticleCircle> playerCircles = playerShapes.circles;

        if (name.equalsIgnoreCase("all")) {
            if (playerCircles.isEmpty()) {
                localeManager.sendMessage(player, "circle_list_empty");
                return;
            }
            playerShapes.currentParticleCount -= playerCircles.values().stream().mapToInt(ParticleCircle::getParticleCount).sum();
            playerCircles.clear();
            localeManager.sendMessage(player, "circle_deleted_all");
        } else {
            ParticleCircle removedCircle = playerCircles.remove(name);
            if (removedCircle == null) {
                localeManager.sendMessage(player, "circle_not_found");
                return;
            } else {
                playerShapes.currentParticleCount -= removedCircle.getParticleCount();
                localeManager.sendMessage(player, "circle_deleted", "%name%", name);
            }
        }
        plugin.savePlayerData(uuid);
    }

    private void handleList(Player player) {
        Map<String, ParticleCircle> playerCircles = plugin.getPlayerCircles(player.getUniqueId());
        if (playerCircles.isEmpty()) {
            localeManager.sendMessage(player, "circle_list_empty");
            return;
        }
        localeManager.sendMessage(player, "circle_list_header");
        for (String name : playerCircles.keySet()) {
            player.sendMessage(ChatColor.AQUA + "- " + name);
        }
    }

    private void sendeHilfe(Player player) {
        localeManager.sendMessage(player, "help_header");
        localeManager.sendMessage(player, "help_create_circle");
        localeManager.sendMessage(player, "help_create_desc");
        localeManager.sendMessage(player, "help_options_circle");
        localeManager.sendMessage(player, "help_delete");
        localeManager.sendMessage(player, "help_delete_desc");
        localeManager.sendMessage(player, "help_list");
        localeManager.sendMessage(player, "help_list_desc");
        localeManager.sendMessage(player, "help_help");
        localeManager.sendMessage(player, "help_help_desc");
        localeManager.sendMessage(player, "help_footer");
    }

    public Color parseFarbe(String farbName) {
        return colorMap.get(farbName.toLowerCase());
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}