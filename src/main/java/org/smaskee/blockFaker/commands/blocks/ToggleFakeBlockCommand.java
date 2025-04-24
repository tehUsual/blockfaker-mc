package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.helpers.SimplePlayer;
import org.smaskee.blockFaker.managers.BlockSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /togglefakeblock <player> <name> <show|hide>
public class ToggleFakeBlockCommand extends BaseCommand implements TabCompleter {
    private final BlockSender blockSender;

    public ToggleFakeBlockCommand(JavaPlugin plugin) {
        super(plugin);
        blockSender = this.plugin.getBlockSender();
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.toggle");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 3)) return false;
        if (!CommandUtils.validateName(sender, args[1])) return false;
        if (!(args[2].equalsIgnoreCase("hide") || args[2].equalsIgnoreCase("show"))) {
            sender.sendMessage("§c" + command.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        boolean visible = args[2].equalsIgnoreCase("show");
        String playerName = args[0];
        String blockName = args[1];

        SimplePlayer player = SimplePlayer.get(playerName);
        if (player == null || !player.isValid()) {
            sender.sendMessage("§c" + (player != null ? player.getErrorMessage() : "Invalid player name"));
            return true;
        }

        if (!dataManager.setBlockVisibility(blockName, player.getPlayerId(), visible)) {
            sender.sendMessage("§cA block with the name '" + blockName + "' does not exist!");
            return true;
        }

        if (player.isOnline()) {
            blockSender.sendBlock(player.getPlayer(), dataManager.getBlock(blockName), visible);
        }

        return true;
    }

    // usage: /togglefakeblock <player> <name> <show|hide>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1: // Player name
                // Add all online players
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList());
                break;
            case 2: // Block name
                completions.addAll(dataManager.getAllBlocks().keySet());
                break;
            case 3: // Show/Hide
                completions.addAll(Arrays.asList("show", "hide"));
                break;
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
} 