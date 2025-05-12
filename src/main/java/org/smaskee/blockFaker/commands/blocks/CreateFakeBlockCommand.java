package org.smaskee.blockFaker.commands.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.managers.DataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for creating fake blocks.
 */
public class CreateFakeBlockCommand extends BaseCommand implements TabCompleter {
    private final DataManager dataManager;

    public CreateFakeBlockCommand(BlockFaker plugin) {
        super(plugin, "createfakeblock", "blockfaker.createfakeblock",
                "Creates a fake block at the target location",
                "/createfakeblock <name> <material>",
                Arrays.asList("cfb", "createblock"),
                true);
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!validateCommand(context.getSender(), context.getCommand(), context.getArgs())) {
            return false;
        }

        Player player = context.getPlayer();
        String name = context.getArg(0);
        Material material = Material.matchMaterial(context.getArg(1));
        Block targetBlock = player.getTargetBlock(null, 5);
        Location loc = targetBlock.getLocation();

        FakeBlock fakeBlock = new FakeBlock(name, loc, material, (BlockFaker) plugin, ((BlockFaker) plugin).getBlockPacketHandler());
        plugin.getLocationManager().registerEntity(fakeBlock);
        plugin.getVisibilityManager().showEntityToAll(fakeBlock);
        dataManager.addBlock(fakeBlock);

        context.getSender().sendMessage("§aCreated fake block '" + name + "' at " +
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        return true;
    }

    private boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 2)) return false;
        if (!CommandUtils.validateName(sender, args[0])) return false;
        if (!CommandUtils.isFakeBlockNameAvailable(sender, args[0], dataManager)) return false;
        if (!CommandUtils.validateMaterial(sender, args[1])) return false;

        Block targetBlock = ((Player) sender).getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            sender.sendMessage("§cYou must be looking at a block!");
            return false;
        }

        Location loc = targetBlock.getLocation();
        if (!CommandUtils.isFakePositionAvailable(sender, loc, dataManager)) return false;
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>();
        } else if (args.length == 2) {
            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 