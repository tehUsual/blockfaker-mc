package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.helpers.SimplePlayer;
import org.smaskee.blockFaker.managers.BlockSender;
import org.smaskee.blockFaker.structs.FakeBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /hidefakeblocks <player> <names...>
public class HideFakeBlocksCommand extends BaseCommand implements TabCompleter {
    private final BlockSender blockSender;

    public HideFakeBlocksCommand(JavaPlugin plugin) {
        super(plugin);
        blockSender = this.plugin.getBlockSender();
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

        List<FakeBlock> fakeBlocks = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            FakeBlock fakeBlock = dataManager.getBlock(args[i]);
            if (fakeBlock != null) {
                fakeBlocks.add(fakeBlock);
                dataManager.setBlockVisibility(args[i], player.getPlayerId(), false);
            }
        }

        if (player.isOnline()) {
            blockSender.sendMultipleBlocks(player.getPlayer(), fakeBlocks.toArray(new FakeBlock[0]), false);
        }

        return true;
    }

    // usage: /hidefakeblocks <player> <names...>
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
            // Second and subsequent arguments: Block names
            List<String> usedBlockNames = Arrays.asList(Arrays.copyOfRange(args, 1, args.length - 1));
            completions.addAll(dataManager.getAllBlocks().keySet().stream()
                    .filter(blockName -> !usedBlockNames.contains(blockName))
                    .toList());
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
} 