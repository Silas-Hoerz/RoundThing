/**
 * Provides tab-completion for the /roundthing admin command. It suggests sub-commands
 * like 'setlimit' and 'reload', as well as example values for the limit.
 *
 * @author Silas Hörz
 * @version 1.0
 */
package de.roundthing;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("setlimit", "reload");
    private static final List<String> LIMIT_SUGGESTIONS = Arrays.asList("5000", "10000", "25000");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        final String currentArg = args[args.length - 1];

        // Suggestions for the first argument (setlimit, reload)
        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, SUB_COMMANDS, completions);
        }
        // Suggestions for the amount on /roundthing setlimit <amount>
        else if (args.length == 2 && args[0].equalsIgnoreCase("setlimit")) {
            StringUtil.copyPartialMatches(currentArg, LIMIT_SUGGESTIONS, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}