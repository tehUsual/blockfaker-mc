package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.managers.DataManager;
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.structs.SkullBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// usage: /createfakeskullfromblock <name> <x> <y> <z> <world> <texture_name>
public class CreateFakeSkullFromBlockCommand extends BaseCommand implements TabCompleter {
    private final DataManager dataManager;

    public CreateFakeSkullFromBlockCommand(BlockFaker plugin) {
        super(plugin, "createfakeskullfromblock", "blockfaker.createfakeskullfromblock",
                "Creates a fake skull from an existing skull block",
                "/createfakeskullfromblock <name> <x> <y> <z> <world> <texture_name>",
                Arrays.asList("cfsb", "createskullfromblock"),
                true);
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.create");
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!CommandUtils.validateArgsLength(context.getSender(), context.getCommand(), context.getArgs(), 6)) {
            return false;
        }
        if (!CommandUtils.validateName(context.getSender(), context.getArg(0))) {
            return false;
        }
        if (!CommandUtils.isFakeSkullNameAvailable(context.getSender(), context.getArg(0), dataManager)) {
            return false;
        }
        if (!CommandUtils.isTextureCreated(context.getSender(), context.getArg(5), dataManager)) {
            return false;
        }
        if (!CommandUtils.validateLocation(context.getSender(), context.getCommand(), 
                context.getArg(1), context.getArg(2), context.getArg(3))) {
            return false;
        }

        String skullName = context.getArg(0);
        Location location = CommandUtils.argToLocation(context.getArg(1), context.getArg(2), 
                context.getArg(3), context.getArg(4));
        String textureName = context.getArg(5);

        // Verify skull block
        Block block = location.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            context.getSender().sendMessage("§cThere is no player skull at the given location.");
            return true;
        }

        // Load skull data
        SkullBlock skullBlock = SkullBlock.loadSkullFromBlock(location, plugin);
        if (skullBlock == null) {
            context.getSender().sendMessage("§cCould not extract texture from skull.");
            return true;
        }

        // Create fake skull
        FakeSkull skull = new FakeSkull(skullName, location, textureName, skullBlock.getRotation(), 
                skullBlock.isWallSkull(), plugin, plugin.getPacketHandler());
        dataManager.addSkull(skull);
        plugin.getLocationManager().registerEntity(skull);
        plugin.getVisibilityManager().showEntityToAll(skull);

        context.getSender().sendMessage("§aFake skull '" + skullName + "' created at " + 
                CommandUtils.locToStr(location) + ".");
        return true;
    }

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