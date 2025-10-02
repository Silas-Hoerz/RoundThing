/**
 * Provides context-aware tab-completion for the /circle command. It suggests sub-commands,
 * existing circle names for deletion, colors, coordinates, and other parameters.
 *
 * @author Silas Hörz
 * @version 1.0
 */
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

        final Player player = (Player) sender;
        final List<String> completions = new ArrayList<>();
        final String currentArg = args[args.length - 1];

        // Suggestions for the main sub-command (create, delete, etc.)
        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, SUB_COMMANDS, completions);
        }
        // Suggestions for the /c create command
        else if (args[0].equalsIgnoreCase("create")) {
            handleCreateSuggestions(player, args, completions);
        }
        // Suggestions for the /c delete command
        else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            List<String> circleNames = new ArrayList<>(plugin.getPlayerCircles(player.getUniqueId()).keySet());
            circleNames.add("all");
            StringUtil.copyPartialMatches(currentArg, circleNames, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void handleCreateSuggestions(Player player, String[] args, List<String> completions) {
        final String currentArg = args[args.length - 1];
        final int argIndex = args.length - 1; // 0-based index of the current argument

        // Command structure: /c create <name> <diameter> [thickness] [x y z] [color] [rotX] [rotZ]
        switch (argIndex) {
            case 2: // Player is typing the diameter
                StringUtil.copyPartialMatches(currentArg, DIAMETER_SUGGESTIONS, completions);
                break;
            case 3: // Player is typing the thickness
                StringUtil.copyPartialMatches(currentArg, THICKNESS_SUGGESTIONS, completions);
                break;
            case 4: // Player is typing the X coordinate
                completions.add(String.valueOf(player.getLocation().getBlockX()));
                break;
            case 5: // Player is typing the Y coordinate
                completions.add(String.valueOf(player.getLocation().getBlockY()));
                break;
            case 6: // Player is typing the Z coordinate
                completions.add(String.valueOf(player.getLocation().getBlockZ()));
                break;
            case 7: // Player is typing the color
                StringUtil.copyPartialMatches(currentArg, circleCommand.getColorMap().keySet(), completions);
                break;
            case 8: // Player is typing the X rotation
                StringUtil.copyPartialMatches(currentArg, ROTATION_SUGGESTIONS, completions);
                break;
            case 9: // Player is typing the Z rotation
                StringUtil.copyPartialMatches(currentArg, ROTATION_SUGGESTIONS, completions);
                break;
        }
    }
}