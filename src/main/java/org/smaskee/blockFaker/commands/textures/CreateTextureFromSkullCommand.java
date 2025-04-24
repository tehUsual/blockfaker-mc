package org.smaskee.blockFaker.commands.textures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.helpers.WorldResolver;
import org.smaskee.blockFaker.structs.SkullBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// usage: /createskulltexturefrom <name> <x> <y> <z> [world]
public class CreateTextureFromSkullCommand extends BaseCommand implements TabCompleter {
    public CreateTextureFromSkullCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsMinLength(sender, command, args, 4)) return false;
        if (!CommandUtils.validateName(sender, args[0])) return false;
        if (!CommandUtils.isTextureNameAvailable(sender, args[0], dataManager)) return false;
        if (!CommandUtils.validateLocation(sender,command, args[1], args[2], args[3])) return false;
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        // Get location of block
        Location location = CommandUtils.argToLocation(args[1], args[2], args[3],
                WorldResolver.getOverworld().getName());

        // Verify skull block
        Block block = location.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            sender.sendMessage("§cThere is no player skull at the given location.");
            return true;
        }

        // Load skull data
        SkullBlock skullBlock = SkullBlock.loadSkullFromBlock(location, plugin);
        if (skullBlock == null) {
            sender.sendMessage("§cCould not extract texture from skull.");
            return true;
        }

        // Verify texture
        String texture = skullBlock.getTextureValue();
        if (texture == null || texture.isEmpty()) {
            sender.sendMessage("§cCould not extract texture from skull.");
            return true;
        }

        // Check if texture already exists
        String prevTextureName = dataManager.getTextureNameByValue(texture);
        if (prevTextureName != null) {
            sender.sendMessage("Texture already created as '§a" + prevTextureName + "'§r.");
            return true;
        }

        // Store texture
        String textureName = args[0];
        dataManager.addTexture(textureName, texture, skullBlock.getSkullId());

        sender.sendMessage("§aTexture '" + textureName + "' created successfully.");
        return true;
    }

    // usage: /createskulltexturefrom <name> <x> <y> <z> [world]
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1: // Texture name
                completions.add("<name>");
                break;
            case 2: // X coordinate
                completions.add(String.valueOf(player.getTargetBlock(null, 5).getX()));
                break;
            case 3: // Y coordinate
                completions.add(String.valueOf(player.getTargetBlock(null, 5).getY()));
                break;
            case 4: // Z coordinate
                completions.add(String.valueOf(player.getTargetBlock(null, 5).getZ()));
                break;
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}
