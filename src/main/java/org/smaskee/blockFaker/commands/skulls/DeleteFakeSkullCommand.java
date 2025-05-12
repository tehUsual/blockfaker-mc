package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for deleting fake skulls.
 */
public class DeleteFakeSkullCommand extends BaseCommand {
    public DeleteFakeSkullCommand(BlockFaker plugin) {
        super(plugin, "deletefakeskull", "blockfaker.deletefakeskull",
                "Deletes a fake skull at the target location",
                "/deletefakeskull",
                Arrays.asList("dfs", "deleteskull"),
                true);
    }

    @Override
    public boolean execute(CommandContext context) {
        Block targetBlock = context.getPlayer().getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            context.getSender().sendMessage("§cYou must be looking at a block!");
            return true;
        }

        FakeSkull fakeSkull = plugin.getLocationManager().getSkull(targetBlock.getLocation());
        if (fakeSkull == null) {
            context.getSender().sendMessage("§cThere is no fake skull at that location!");
            return true;
        }

        plugin.getVisibilityManager().hideEntityFromAll(fakeSkull);
        plugin.getLocationManager().unregisterEntity(fakeSkull);
        plugin.getDataManager().removeSkull(fakeSkull.getName());

        context.getSender().sendMessage("§aDeleted fake skull '" + fakeSkull.getName() + "' at " +
                targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
} 