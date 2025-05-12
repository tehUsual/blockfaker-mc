package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
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

/**
 * Command for deleting fake blocks.
 */
public class DeleteFakeBlockCommand extends BaseCommand {
    public DeleteFakeBlockCommand(BlockFaker plugin) {
        super(plugin, "deletefakeblock", "blockfaker.deletefakeblock",
                "Deletes a fake block at the target location",
                "/deletefakeblock",
                Arrays.asList("dfb", "deleteblock"),
                true);
    }

    @Override
    public boolean execute(CommandContext context) {
        Block targetBlock = context.getPlayer().getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            context.getSender().sendMessage("§cYou must be looking at a block!");
            return true;
        }

        FakeBlock fakeBlock = plugin.getLocationManager().getBlock(targetBlock.getLocation());
        if (fakeBlock == null) {
            context.getSender().sendMessage("§cThere is no fake block at that location!");
            return true;
        }

        plugin.getVisibilityManager().hideEntityFromAll(fakeBlock);
        plugin.getLocationManager().unregisterEntity(fakeBlock);
        plugin.getDataManager().removeBlock(fakeBlock.getName());

        context.getSender().sendMessage("§aDeleted fake block '" + fakeBlock.getName() + "' at " +
                targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
} 