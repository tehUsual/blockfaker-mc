package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.helpers.SimplePlayer;
import org.smaskee.blockFaker.managers.SkullSender;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /showfakeskulls <player> <names...>
public class ShowFakeSkullsCommand extends BaseCommand implements TabCompleter {
    private final SkullSender skullSender;

    public ShowFakeSkullsCommand(JavaPlugin plugin) {
        super(plugin);
        skullSender = this.plugin.getSkullSender();
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.toggle");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c" + command.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        String playerName = args[0];
        SimplePlayer player = SimplePlayer.get(playerName);
        if (player == null || !player.isValid()) {
            sender.sendMessage("§c" + (player != null ? player.getErrorMessage() : "Invalid player name"));
            return true;
        }

        List<FakeSkull> fakeSkulls = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            FakeSkull fakeSkull = dataManager.getSkull(args[i]);
            if (fakeSkull != null) {
                fakeSkulls.add(fakeSkull);
                dataManager.setSkullVisibility(args[i], player.getPlayerId(), true);
            }
        }

        if (player.isOnline()) {
            skullSender.sendMultipleSkulls(player.getPlayer(), fakeSkulls.toArray(new FakeSkull[0]), true);
        }

        return true;
    }

    // usage: /showfakeskulls <player> <names...>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // First argument: Player name
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        } else if (args.length >= 2) {
            // Second and subsequent arguments: Skull names
            List<String> usedSkullName = Arrays.asList(Arrays.copyOfRange(args, 1, args.length - 1));
            completions.addAll(dataManager.getAllSkulls().keySet().stream()
                    .filter(blockName -> !usedSkullName.contains(blockName))
                    .toList());
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}