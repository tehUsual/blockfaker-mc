package org.smaskee.blockFaker.commands.textures;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.commands.CommandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for removing textures.
 */
public class RemoveTextureCommand extends BaseCommand implements TabCompleter {
    public RemoveTextureCommand(BlockFaker plugin) {
        super(plugin, "removetexture", "blockfaker.removetexture",
                "Removes a texture from the available textures",
                "/removetexture <name>",
                Arrays.asList("rt", "remtex"),
                false);
    }

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgCount() < 1) {
            context.getSender().sendMessage("§cUsage: " + getUsage());
            return true;
        }

        String name = context.getArg(0);

        if (!plugin.getTextureManager().isValidTexture(name)) {
            context.getSender().sendMessage("§cNo texture with that name exists.");
            return true;
        }
        plugin.getTextureManager().removeTexture(name);
        context.getSender().sendMessage("§aTexture '" + name + "' removed successfully.");
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