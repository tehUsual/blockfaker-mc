package org.smaskee.blockFaker.commands.textures;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.structs.SkullTexture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Command for listing all available textures.
 */
public class ListTexturesCommand extends BaseCommand {
    public ListTexturesCommand(BlockFaker plugin) {
        super(plugin, "listtextures", "blockfaker.listtextures",
                "Lists all available textures",
                "/listtextures",
                Arrays.asList("lt", "listtex"),
                false);
    }

    @Override
    public boolean execute(CommandContext context) {
        Map<String, SkullTexture> textures = plugin.getTextureManager().getAllTextures();
        
        if (textures.isEmpty()) {
            context.getSender().sendMessage("§cNo textures available.");
            return true;
        }

        context.getSender().sendMessage("§6=== Available Textures ===");
        for (Map.Entry<String, SkullTexture> entry : textures.entrySet()) {
            SkullTexture texture = entry.getValue();
            context.getSender().sendMessage(String.format("§e%s §7- §f%s", 
                texture.getName(), texture.getValue()));
        }
        context.getSender().sendMessage("§6======================");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}