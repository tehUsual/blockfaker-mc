package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /createfakeblock <name> <x> <y> <z> <world> <material>
public class CreateFakeBlockCommand extends BaseCommand implements TabCompleter {
    public CreateFakeBlockCommand(JavaPlugin plugin) {
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
        if (!CommandUtils.isFakeBlockNameAvailable(sender, args[0], dataManager)) return false;
        if (!CommandUtils.validateMaterial(sender, args[5])) return false;
        if (!CommandUtils.validateLocation(sender, command, args[1], args[2], args[3], args[4])) return false;

        Location loc = CommandUtils.argToLocation(args[1], args[2], args[3], args[4]);
        if (!CommandUtils.isFakePositionAvailable(sender, loc, dataManager)) return false;
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        String blockName = args[0];
        Location loc = CommandUtils.argToLocation(args[1], args[2], args[3], args[4]);
        Material material = Material.matchMaterial(args[5]);

        FakeBlock block = new FakeBlock(blockName, loc, material);
        dataManager.addBlock(block);

        sender.sendMessage("§aFake block '" + blockName + "' created at " + CommandUtils.locToStr(loc) + ".");
        return true;
    }

    // usage: /createfakeblock <name> <x> <y> <z> <world> <material>
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1: // Block name
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
            case 6: // Material
                completions.addAll(Arrays.stream(Material.values())
                        .filter(material -> material.isBlock() && !material.isAir())
                        .map(material -> material.name().toLowerCase())
                        .toList());
                break;
        }

        // Filter based on what the user has typed so far
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
} 