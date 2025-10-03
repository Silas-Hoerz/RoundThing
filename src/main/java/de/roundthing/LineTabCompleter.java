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

public class LineTabCompleter implements TabCompleter {

    private final RoundThing plugin;
    private final CircleCommand circleCommand;
    private static final List<String> SUB_COMMANDS = Arrays.asList("create", "delete", "list", "help");

    public LineTabCompleter(RoundThing plugin, CircleCommand circleCommand) {
        this.plugin = plugin;
        this.circleCommand = circleCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        final Player player = (Player) sender;
        final List<String> completions = new ArrayList<>();
        final String currentArg = args[args.length - 1];

        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, SUB_COMMANDS, completions);
        }
        else if (args[0].equalsIgnoreCase("create")) {
            handleLineCreateSuggestions(player, args, completions);
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            List<String> lineNames = new ArrayList<>(plugin.getPlayerShapes(player.getUniqueId()).lines.keySet());
            lineNames.add("all");
            StringUtil.copyPartialMatches(currentArg, lineNames, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void handleLineCreateSuggestions(Player player, String[] args, List<String> completions) {
        final String currentArg = args[args.length - 1];
        final int argIndex = args.length - 1;

        // Befehlsstruktur: /l create <name> <x1> <y1> <z1> <x2> <y2> <z2> [color]
        Location playerLoc = player.getLocation();

        switch (argIndex) {
            // Vorschläge für Position 1
            case 2: completions.add(String.valueOf(playerLoc.getBlockX())); break;
            case 3: completions.add(String.valueOf(playerLoc.getBlockY())); break;
            case 4: completions.add(String.valueOf(playerLoc.getBlockZ())); break;

            // Vorschläge für Position 2
            case 5: completions.add(String.valueOf(playerLoc.getBlockX())); break;
            case 6: completions.add(String.valueOf(playerLoc.getBlockY())); break;
            case 7: completions.add(String.valueOf(playerLoc.getBlockZ())); break;

            // Vorschlag für Farbe
            case 8:
                StringUtil.copyPartialMatches(currentArg, circleCommand.getColorMap().keySet(), completions);
                break;
        }
    }
}