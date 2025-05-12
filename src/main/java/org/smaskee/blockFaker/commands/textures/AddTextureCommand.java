package org.smaskee.blockFaker.commands.textures;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for adding new textures.
 */
public class AddTextureCommand extends BaseCommand {
    public AddTextureCommand(BlockFaker plugin) {
        super(plugin, "addtexture", "blockfaker.addtexture",
                "Adds a new texture for fake skulls",
                "/addtexture <name> <value>",
                Arrays.asList("at", "addtex"),
                false);
    }

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgCount() < 2) {
            context.getSender().sendMessage("§cUsage: " + getUsage());
            return true;
        }

        String name = context.getArg(0);
        String value = context.getArg(1);

        if (plugin.getTextureManager().isValidTexture(name)) {
            context.getSender().sendMessage("§cA texture with that name already exists.");
            return true;
        }
        plugin.getTextureManager().addTexture(name, value);
        context.getSender().sendMessage("§aTexture '" + name + "' added successfully.");
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