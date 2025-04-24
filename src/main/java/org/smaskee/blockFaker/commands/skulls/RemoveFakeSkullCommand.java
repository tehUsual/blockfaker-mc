package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// usage: /removefakeskull <name>
public class RemoveFakeSkullCommand extends BaseCommand implements TabCompleter {
    public RemoveFakeSkullCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 1)) return false;
        return CommandUtils.validateName(sender, args[0]);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        if (dataManager.getSkull(args[0]) != null) {
            dataManager.removeSkull(args[0]);
            sender.sendMessage("§aFake skull removed.");
        } else {
            sender.sendMessage("§cFake skull does not exist.");
        }
        return true;
    }

    // usage: /removefakeskull <name>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (args.length != 1) {
            return new ArrayList<>();
        }

        // Suggest all existing block names
        List<String> completions = new ArrayList<>(dataManager.getAllSkulls().keySet());

        // Filter based on what the user has typed so far
        String currentArg = args[0].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}