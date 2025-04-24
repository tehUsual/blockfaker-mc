package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.DataManager;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends BaseCommand implements TabCompleter {
    private final DataManager dataManager;

    public ReloadCommand(JavaPlugin plugin) {
        super(plugin);
        this.dataManager = ((BlockFaker) plugin).getDataManager();
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.reload");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        if (!CommandUtils.validateArgsLength(sender, command, args, 1)) return false;
        if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§cUnknown subcommand. Use: /blockfaker reload");
            return false;
        }
        return true;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        dataManager.loadData();

        int blockCount = dataManager.getAllBlocks().size();
        int skullCount = dataManager.getAllSkulls().size();
        int textureCount = dataManager.getAllTextures().size();

        String msg = String.format("§aLoaded %d blocks, %d skulls and %d textures",
                blockCount, skullCount, textureCount);

        sender.sendMessage(msg);
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