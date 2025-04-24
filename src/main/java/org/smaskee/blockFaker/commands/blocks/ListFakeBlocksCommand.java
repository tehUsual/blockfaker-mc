package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;

import java.util.Map;

public class ListFakeBlocksCommand extends BaseCommand {
    public ListFakeBlocksCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.read");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        return true; // No arguments needed for this command
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        int count = 0;
        Map<String, FakeBlock> blocks = dataManager.getAllBlocks();
        for (String name : blocks.keySet()) {
            if (count > 30) break;

            FakeBlock block = blocks.get(name);
            sender.sendMessage("FakeBlock: §9" + CommandUtils.locToStr(block.getLocation()) + "§r, §a" + name);
            count++;
        }

        if (count < blocks.size())
            sender.sendMessage("FakeBlock: §a .." + count + "more fake blocks");
        return true;
    }
} 