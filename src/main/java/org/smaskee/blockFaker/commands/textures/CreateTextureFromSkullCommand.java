package org.smaskee.blockFaker.commands.textures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.structs.SkullBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for creating a texture from an existing skull block.
 */
public class CreateTextureFromSkullCommand extends BaseCommand {
    public CreateTextureFromSkullCommand(BlockFaker plugin) {
        super(plugin, "createtexturefromskull", "blockfaker.createtexturefromskull",
                "Creates a texture from an existing skull block",
                "/createtexturefromskull <name>",
                Arrays.asList("ctfs", "createtex"),
                true);
    }

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgCount() < 1) {
            context.getSender().sendMessage("§cUsage: " + getUsage());
            return true;
        }

        String name = context.getArg(0);
        Player player = (Player) context.getSender();
        Block targetBlock = player.getTargetBlock(null, 5);

        // Verify skull block
        if (targetBlock.getType() != Material.PLAYER_HEAD && targetBlock.getType() != Material.PLAYER_WALL_HEAD) {
            player.sendMessage("§cYou must be looking at a player skull.");
            return true;
        }

        // Load skull data
        SkullBlock skullBlock = SkullBlock.loadSkullFromBlock(targetBlock.getLocation(), plugin);
        if (skullBlock == null) {
            player.sendMessage("§cCould not extract texture from skull.");
            return true;
        }

        // Verify texture
        String texture = skullBlock.getTextureValue();
        if (texture == null || texture.isEmpty()) {
            player.sendMessage("§cCould not extract texture from skull.");
            return true;
        }

        // Check if texture already exists
        if (plugin.getTextureManager().isValidTexture(name)) {
            player.sendMessage("§cA texture with that name already exists.");
            return true;
        }

        // Store texture
        plugin.getTextureManager().addTexture(name, texture);
        player.sendMessage("§aTexture '" + name + "' created successfully.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getTextureManager().getAvailableTextures().stream()
                .filter(name -> name.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
