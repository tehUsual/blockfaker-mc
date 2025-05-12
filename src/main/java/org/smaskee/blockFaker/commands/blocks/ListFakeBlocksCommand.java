package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.structs.FakeBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Command for listing all registered fake blocks.
 */
public class ListFakeBlocksCommand extends BaseCommand {
    public ListFakeBlocksCommand(BlockFaker plugin) {
        super(plugin, "listfakeblocks", "blockfaker.listfakeblocks",
                "Lists all registered fake blocks",
                "/listfakeblocks",
                Arrays.asList("lfb", "listblocks"),
                false);
    }

    @Override
    public boolean execute(CommandContext context) {
        Map<String, FakeBlock> blocks = plugin.getDataManager().getAllBlocks();
        if (blocks.isEmpty()) {
            context.getSender().sendMessage("§cThere are no registered fake blocks.");
            return true;
        }

        context.getSender().sendMessage("§6=== Registered Fake Blocks ===");
        for (FakeBlock block : blocks.values()) {
            context.getSender().sendMessage(String.format("§e%s §7at §f%d, %d, %d §7in §f%s",
                    block.getName(),
                    block.getLocation().getBlockX(),
                    block.getLocation().getBlockY(),
                    block.getLocation().getBlockZ(),
                    block.getLocation().getWorld().getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
} 