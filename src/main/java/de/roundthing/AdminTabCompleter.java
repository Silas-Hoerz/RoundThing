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
        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1];

        // Vorschläge für den ersten Befehl (setlimit, reload)
        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, SUB_COMMANDS, completions);
        }
        // Vorschläge für die Anzahl bei /roundthing setlimit <Anzahl>
        else if (args.length == 2 && args[0].equalsIgnoreCase("setlimit")) {
            StringUtil.copyPartialMatches(currentArg, LIMIT_SUGGESTIONS, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}