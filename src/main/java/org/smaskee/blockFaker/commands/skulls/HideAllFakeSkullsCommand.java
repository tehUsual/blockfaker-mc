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
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// usage: /hideallfakeskulls <player>
public class HideAllFakeSkullsCommand extends BaseCommand implements TabCompleter {
    private final SkullSender skullSender;

    public HideAllFakeSkullsCommand(JavaPlugin plugin) {
        super(plugin);
        skullSender = this.plugin.getSkullSender();
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.toggle");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 1)) return false;
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
        Map<String, FakeSkull> allSkulls = dataManager.getAllSkulls();

        for (FakeSkull skull : allSkulls.values()) {
            fakeSkulls.add(skull);
            dataManager.setSkullVisibility(skull.getName(), player.getPlayerId(), false);
        }

        if (player.isOnline()) {
            skullSender.sendMultipleSkulls(player.getPlayer(), fakeSkulls.toArray(new FakeSkull[0]), false);
        }

        return true;
    }

    // usage: /hideallfakeskulls <player>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) { // Player name
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}