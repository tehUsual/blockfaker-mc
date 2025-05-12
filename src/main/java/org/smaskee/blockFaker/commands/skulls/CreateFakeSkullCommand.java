package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// /createfakeskull <name> <x> <y> <z> <world> <texture_name> [ground|wall] [rotation]
public class CreateFakeSkullCommand extends BaseCommand implements TabCompleter {
    public CreateFakeSkullCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsMinLength(sender, command, args, 6)) return false;
        if (!CommandUtils.validateName(sender, args[0])) return false;
        if (!CommandUtils.isFakeSkullNameAvailable(sender, args[0], dataManager)) return false;
        if (!CommandUtils.isTextureCreated(sender, args[5], dataManager)) return false;
        if (!CommandUtils.validateLocation(sender, command, args[1], args[2], args[3], args[4])) return false;

        Location loc = CommandUtils.argToLocation(args[1], args[2], args[3], args[4]);
        if (!CommandUtils.isFakePositionAvailable(sender, loc, dataManager)) return false;

        // Placement
        if (args.length > 6) {
            if (!(args[6].equalsIgnoreCase("ground") || args[6].equalsIgnoreCase("wall"))) {
                sender.sendMessage("§c" + command.getUsage());
                return false;
            }
        }

        // Rotation
        if (args.length > 7) {
            // Wall
            if (args[6].equalsIgnoreCase("wall")) {
                if (!Arrays.asList("NORTH", "EAST", "SOUTH", "WEST").contains(args[7].toUpperCase())) {
                    sender.sendMessage("§cInvalid rotation");
                    return false;
                }
            // Ground
            } else {
                if (!Arrays.asList(SkullBlock.VALID_FACES).contains(args[7].toUpperCase())) {
                    sender.sendMessage("§cInvalid rotation");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        String skullName = args[0];
        Location loc = CommandUtils.argToLocation(args[1], args[2], args[3], args[4]);
        String textureName = args[5];
        boolean onWall = (args.length > 6) && args[6].equalsIgnoreCase("wall");
        BlockFace rotation = (args.length > 7) ? BlockFace.valueOf(args[7].toUpperCase()) : BlockFace.SOUTH;

        FakeSkull skull = new FakeSkull(skullName, loc, textureName, rotation, onWall);
        dataManager.addSkull(skull);

        sender.sendMessage("§aFake skull '" + skullName + "' created at " + CommandUtils.locToStr(loc) + ".");
        return true;
    }

    // /createfakeskull <name> <x> <y> <z> <world> <texture_name> [ground|wall] [rotation]
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
            case 7: // ground|wall
                completions.addAll(Arrays.asList("ground", "wall"));
                break;
            case 8:
                if (args[6].equalsIgnoreCase("wall")) {
                    completions.addAll(Arrays.asList("NORTH", "EAST", "SOUTH", "WEST"));
                } else {
                    completions.addAll(Arrays.asList(SkullBlock.VALID_FACES));
                }
                break;
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}