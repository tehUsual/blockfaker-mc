package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.DataManager;
import org.smaskee.blockFaker.commands.CommandContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReloadCommand extends BaseCommand implements TabCompleter {
    private final DataManager dataManager;

    public ReloadCommand(BlockFaker plugin) {
        super(plugin, "reload", "blockfaker.reload",
                "Reloads the plugin configuration and data",
                "/blockfaker reload",
                Arrays.asList("r", "rl"),
                true);
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.reload");
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!CommandUtils.validateArgsLength(context.getSender(), context.getCommand(), context.getArgs(), 1)) {
            return false;
        }
        if (!context.getArg(0).equalsIgnoreCase("reload")) {
            context.getSender().sendMessage("§cUnknown subcommand. Use: /blockfaker reload");
            return false;
        }

        dataManager.loadData();

        int blockCount = dataManager.getAllBlocks().size();
        int skullCount = dataManager.getAllSkulls().size();
        int textureCount = dataManager.getAllTextures().size();

        String msg = String.format("§aLoaded %d blocks, %d skulls and %d textures",
                blockCount, skullCount, textureCount);

        context.getSender().sendMessage(msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
        }
        return completions;
    }
}