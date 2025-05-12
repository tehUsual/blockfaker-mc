package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
 * Command for creating fake skulls.
 */
public class CreateFakeSkullCommand extends BaseCommand {
    public CreateFakeSkullCommand(BlockFaker plugin) {
        super(plugin, "createfakeskull", "blockfaker.createfakeskull",
                "Creates a fake skull at the target location",
                "/createfakeskull <texture>",
                Arrays.asList("cfs", "createskull"),
                true);
    }

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgCount() < 1) {
            context.getSender().sendMessage("§cUsage: " + getUsage());
            return true;
        }

        Block targetBlock = context.getPlayer().getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            context.getSender().sendMessage("§cYou must be looking at a block!");
            return true;
        }

        String texture = context.getArg(0);
        if (!plugin.getTextureManager().isValidTexture(texture)) {
            context.getSender().sendMessage("§cInvalid texture! Use /listtextures to see available textures.");
            return true;
        }

        // Check if there's already a fake entity at this location
        if (plugin.getLocationManager().hasEntity(targetBlock.getLocation())) {
            context.getSender().sendMessage("§cThere is already a fake entity at that location!");
            return true;
        }

        // Create and register the fake skull
        String name = "skull_" + System.currentTimeMillis();
        FakeSkull skull = new FakeSkull(name, targetBlock.getLocation(), texture, BlockFace.SOUTH, false, plugin, plugin.getPacketHandler());
        plugin.getLocationManager().registerEntity(skull);
        plugin.getDataManager().addSkull(skull);
        plugin.getVisibilityManager().showEntityToAll(skull);

        context.getSender().sendMessage("§aCreated fake skull with texture '" + texture + "' at " +
                targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(plugin.getTextureManager().getAvailableTextures());
        }
        return new ArrayList<>();
    }
}