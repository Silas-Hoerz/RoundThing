package de.roundthing;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SphereTabCompleter implements TabCompleter {

    private final RoundThing plugin;
    private final CircleCommand circleCommand; // Wir nutzen die Farb-Map von hier
    private static final List<String> SUB_COMMANDS = Arrays.asList("create", "delete", "list", "help");
    private static final List<String> DIAMETER_SUGGESTIONS = Arrays.asList("10", "20", "50");
    private static final List<String> THICKNESS_SUGGESTIONS = Arrays.asList("1", "2", "3", "5");

    public SphereTabCompleter(RoundThing plugin, CircleCommand circleCommand) {
        this.plugin = plugin;
        this.circleCommand = circleCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1];

        // 1. Vorschläge für den Hauptbefehl (create, delete, etc.)
        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, SUB_COMMANDS, completions);
        }
        // 2. Detaillierte Vorschläge für den /s create Befehl
        else if (args[0].equalsIgnoreCase("create")) {
            handleSphereCreateSuggestions(player, args, completions);
        }
        // 3. Vorschläge für den /s delete Befehl
        else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            List<String> sphereNames = new ArrayList<>(plugin.getPlayerSpheres(player.getUniqueId()).keySet());
            sphereNames.add("all");
            StringUtil.copyPartialMatches(currentArg, sphereNames, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void handleSphereCreateSuggestions(Player player, String[] args, List<String> completions) {
        String currentArg = args[args.length - 1];
        int argIndex = args.length - 1; // 0-basierter Index des aktuellen Arguments

        // Die Logik ist identisch zum Kreis, nur ohne Rotation am Ende
        switch (argIndex) {
            case 2: // Spieler tippt den Durchmesser
                StringUtil.copyPartialMatches(currentArg, DIAMETER_SUGGESTIONS, completions);
                break;
            case 3: // Spieler tippt die Dicke
                StringUtil.copyPartialMatches(currentArg, THICKNESS_SUGGESTIONS, completions);
                break;
            case 4: // Spieler tippt Koordinate X
                completions.add(String.valueOf(player.getLocation().getBlockX()));
                break;
            case 5: // Spieler tippt Koordinate Y
                completions.add(String.valueOf(player.getLocation().getBlockY()));
                break;
            case 6: // Spieler tippt Koordinate Z
                completions.add(String.valueOf(player.getLocation().getBlockZ()));
                break;
            case 7: // Spieler tippt die Farbe
                StringUtil.copyPartialMatches(currentArg, circleCommand.getColorMap().keySet(), completions);
                break;
            // Keine weiteren cases, da Kugeln keine Rotation haben
        }
    }
}