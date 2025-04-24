package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.helpers.SimplePlayer;
import org.smaskee.blockFaker.managers.BlockSender;
import org.smaskee.blockFaker.structs.FakeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// usage: /hideallfakeblocks <player>
public class HideAllFakeBlocksCommand extends BaseCommand implements TabCompleter {
    private final BlockSender blockSender;

    public HideAllFakeBlocksCommand(JavaPlugin plugin) {
        super(plugin);
        blockSender = this.plugin.getBlockSender();
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

        List<FakeBlock> fakeBlocks = new ArrayList<>();
        Map<String, FakeBlock> allBlocks = dataManager.getAllBlocks();
        
        for (FakeBlock block : allBlocks.values()) {
            fakeBlocks.add(block);
            dataManager.setBlockVisibility(block.getName(), player.getPlayerId(), false);
        }

        if (player.isOnline()) {
            blockSender.sendMultipleBlocks(player.getPlayer(), fakeBlocks.toArray(new FakeBlock[0]), false);
        }

        return true;
    }

    // usage: /hideallfakeblocks <player>
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