package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.DataManager;

public abstract class BaseCommand implements CommandExecutor {
    protected final BlockFaker plugin;
    protected final DataManager dataManager;

    protected BaseCommand(JavaPlugin plugin) {
        this.plugin = (BlockFaker) plugin;
        this.dataManager = this.plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (!validateCommand(sender, command, args)) {
            return true;
        }

        return execute(sender, command, args);
    }

    protected abstract boolean hasPermission(CommandSender sender);
    protected abstract boolean validateCommand(CommandSender sender, Command command, String[] args);
    protected abstract boolean execute(CommandSender sender, Command command, String[] args);
} 