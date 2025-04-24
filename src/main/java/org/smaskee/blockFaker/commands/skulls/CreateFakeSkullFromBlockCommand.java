package org.smaskee.blockFaker.commands.skulls;

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
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.structs.SkullBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// usage: /createfakeskullfromblock <name> <x> <y> <z> <world> <texture_name>
public class CreateFakeSkullFromBlockCommand extends BaseCommand implements TabCompleter {
    public CreateFakeSkullFromBlockCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 6)) return false;
        if (!CommandUtils.validateName(sender, args[0])) return false;
        if (!CommandUtils.isFakeSkullNameAvailable(sender, args[0], dataManager)) return false;
        if (!CommandUtils.isTextureNameAvailable(sender, args[5], dataManager)) return false;
        if (!CommandUtils.validateLocation(sender,command, args[1], args[2], args[3])) return false;
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        String skullName = args[0];
        Location location = CommandUtils.argToLocation(args[1], args[2], args[3], args[4]);
        String textureName = args[5];

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

        // Create fake skull
        FakeSkull skull = new FakeSkull(skullName, location, textureName, skullBlock.getRotation(), skullBlock.isWallSkull());
        dataManager.addSkull(skull);

        sender.sendMessage("§aFake skull '" + skullName + "' created at " + CommandUtils.locToStr(location) + ".");
        return true;
    }

    // usage: /createfakeskullfromblock <name> <x> <y> <z> <world> <texture_name>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1: // Skull name
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
            case 5: // World
                completions.add("world");
                completions.add(player.getWorld().getName());
                break;
            case 6: // Texture name
                completions.addAll(dataManager.getAllTextures().keySet());
                break;
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}