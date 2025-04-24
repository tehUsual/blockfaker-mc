package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.helpers.SimplePlayer;
import org.smaskee.blockFaker.managers.SkullSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /togglefakeskull <player> <name> <show|hide>
public class ToggleFakeSkullCommand extends BaseCommand implements TabCompleter {
    private final SkullSender skullSender;

    public ToggleFakeSkullCommand(JavaPlugin plugin) {
        super(plugin);
        skullSender = this.plugin.getSkullSender();
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
        String skullName = args[1];

        SimplePlayer player = SimplePlayer.get(playerName);
        if (player == null || !player.isValid()) {
            sender.sendMessage("§c" + (player != null ? player.getErrorMessage() : "Invalid player name"));
            return true;
        }

        if (!dataManager.setSkullVisibility(skullName, player.getPlayerId(), visible)) {
            sender.sendMessage("§cA block with the name '" + skullName + "' does not exist!");
            return true;
        }

        if (player.isOnline()) {
            skullSender.sendSkull(player.getPlayer(), dataManager.getSkull(skullName), visible);
        }

        return true;
    }

    // usage: /togglefakeskull <player> <name> <show|hide>
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
            case 2: // Skull name
                completions.addAll(dataManager.getAllSkulls().keySet());
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