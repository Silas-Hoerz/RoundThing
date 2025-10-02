package de.roundthing;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CircleTabCompleter implements TabCompleter {

    private final RoundThing plugin;
    private final CircleCommand circleCommand;
    private static final List<String> SUB_COMMANDS = Arrays.asList("create", "delete", "list", "help");
    private static final List<String> DIAMETER_SUGGESTIONS = Arrays.asList("10", "20", "50");
    private static final List<String> THICKNESS_SUGGESTIONS = Arrays.asList("1", "2", "3", "5");
    private static final List<String> ROTATION_SUGGESTIONS = Arrays.asList("-90", "-45", "0", "45", "90");

    public CircleTabCompleter(RoundThing plugin, CircleCommand circleCommand) {
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
        // 2. Vorschläge für den /c create Befehl
        else if (args[0].equalsIgnoreCase("create")) {
            handleCreateSuggestions(player, args, completions);
        }
        // 3. Vorschläge für den /c delete Befehl
        else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            List<String> circleNames = new ArrayList<>(plugin.getPlayerCircles(player.getUniqueId()).keySet());
            circleNames.add("all");
            StringUtil.copyPartialMatches(currentArg, circleNames, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void handleCreateSuggestions(Player player, String[] args, List<String> completions) {
        String currentArg = args[args.length - 1];
        int argIndex = args.length - 1; // 0-basierter Index des aktuellen Arguments

        // args[0] ist "create", args[1] ist der Name, args[2] ist der Durchmesser
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
            case 8: // Spieler tippt Rotation X
                StringUtil.copyPartialMatches(currentArg, ROTATION_SUGGESTIONS, completions);
                break;
            case 9: // Spieler tippt Rotation Z
                StringUtil.copyPartialMatches(currentArg, ROTATION_SUGGESTIONS, completions);
                break;
        }
    }
}