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

public class SphereCommand implements CommandExecutor {

    private final RoundThing plugin;
    private final CircleCommand circleCommand;
    private final LocaleManager localeManager;

    public SphereCommand(RoundThing plugin, CircleCommand circleCommand, LocaleManager localeManager) {
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
            int currentIndex = 3;

            if (args.length > currentIndex && isNumeric(args[currentIndex])) {
                dicke = Integer.parseInt(args[currentIndex++]);
            }
            if (args.length > currentIndex + 2 && isNumeric(args[currentIndex]) && isNumeric(args[currentIndex + 1]) && isNumeric(args[currentIndex + 2])) {
                mittelpunkt = new Location(player.getWorld(), Double.parseDouble(args[currentIndex]), Double.parseDouble(args[currentIndex + 1]), Double.parseDouble(args[currentIndex + 2]));
                currentIndex += 3;
            }
            if (args.length > currentIndex && !isNumeric(args[currentIndex])) {
                farbe = circleCommand.parseFarbe(args[currentIndex++]);
                if (farbe == null) {
                    localeManager.sendMessage(player, "invalid_color");
                    String farbenListe = String.join(", ", circleCommand.getColorMap().keySet());
                    localeManager.sendMessage(player, "available_colors", "%colors%", farbenListe);
                    return;
                }
            }

            // --- PARTIKEL BUDGET PRÜFUNG ---
            ParticleSphere testKugel = new ParticleSphere(mittelpunkt, durchmesser, dicke, farbe);
            int neuePartikel = testKugel.getParticleCount();
            RoundThing.PlayerShapes playerShapes = plugin.getPlayerShapes(uuid);

            int altePartikel = 0;
            if (playerShapes.spheres.containsKey(name)) {
                altePartikel = playerShapes.spheres.get(name).getParticleCount();
            }

            if (playerShapes.currentParticleCount - altePartikel + neuePartikel > plugin.getParticleLimit()) {
                localeManager.sendMessage(player, "budget_exceeded",
                        "%new%", String.valueOf(neuePartikel),
                        "%current%", String.valueOf(playerShapes.currentParticleCount - altePartikel),
                        "%limit%", String.valueOf(plugin.getParticleLimit())
                );
                return;
            }

            // Alles gut -> Kugel speichern und Budget aktualisieren
            playerShapes.spheres.put(name, testKugel);
            playerShapes.currentParticleCount = playerShapes.currentParticleCount - altePartikel + neuePartikel;
            plugin.savePlayerData(uuid);
            localeManager.sendMessage(player, "sphere_success", "%name%", name);

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
        Map<String, ParticleSphere> playerSpheres = playerShapes.spheres;

        if (name.equalsIgnoreCase("all")) {
            if (playerSpheres.isEmpty()) {
                localeManager.sendMessage(player, "sphere_list_empty");
                return;
            }
            playerShapes.currentParticleCount -= playerSpheres.values().stream().mapToInt(ParticleSphere::getParticleCount).sum();
            playerSpheres.clear();
            localeManager.sendMessage(player, "sphere_deleted_all");
        } else {
            ParticleSphere removedSphere = playerSpheres.remove(name);
            if (removedSphere == null) {
                localeManager.sendMessage(player, "sphere_not_found");
                return;
            } else {
                playerShapes.currentParticleCount -= removedSphere.getParticleCount();
                localeManager.sendMessage(player, "sphere_deleted", "%name%", name);
            }
        }
        plugin.savePlayerData(uuid);
    }

    private void handleList(Player player) {
        Map<String, ParticleSphere> playerSpheres = plugin.getPlayerSpheres(player.getUniqueId());
        if (playerSpheres.isEmpty()) {
            localeManager.sendMessage(player, "sphere_list_empty");
            return;
        }

        localeManager.sendMessage(player, "sphere_list_header");
        for (String name : playerSpheres.keySet()) {
            player.sendMessage(ChatColor.AQUA + "- " + name);
        }
    }

    private void sendeHilfe(Player player) {
        localeManager.sendMessage(player, "help_header");
        localeManager.sendMessage(player, "help_create_sphere");
        localeManager.sendMessage(player, "help_create_desc");
        localeManager.sendMessage(player, "help_options_sphere");
        localeManager.sendMessage(player, "help_delete");
        localeManager.sendMessage(player, "help_delete_desc");
        localeManager.sendMessage(player, "help_list");
        localeManager.sendMessage(player, "help_list_desc");
        localeManager.sendMessage(player, "help_help");
        localeManager.sendMessage(player, "help_help_desc");
        localeManager.sendMessage(player, "help_footer");
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}