package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.smaskee.blockFaker.BlockFaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all commands in the BlockFaker plugin.
 */
public abstract class BaseCommand implements TabCompleter {
    protected final BlockFaker plugin;
    protected final String name;
    protected final String permission;
    protected final String description;
    protected final String usage;
    protected final List<String> aliases;
    protected final boolean requiresPlayer;

    /**
     * Creates a new base command.
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use the command
     * @param description The command description
     * @param usage The command usage
     * @param aliases The command aliases
     * @param requiresPlayer Whether the command requires a player to execute
     */
    protected BaseCommand(BlockFaker plugin, String name, String permission, String description, String usage, List<String> aliases, boolean requiresPlayer) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.requiresPlayer = requiresPlayer;
    }

    /**
     * Executes the command.
     * @param context The command context
     * @return true if the command was executed successfully
     */
    public abstract boolean execute(CommandContext context);

    /**
     * Gets the command name.
     * @return The command name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the permission required to use the command.
     * @return The permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the command description.
     * @return The command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the command usage.
     * @return The command usage
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Gets the command aliases.
     * @return The command aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Checks if the command requires a player to execute.
     * @return true if the command requires a player
     */
    public boolean requiresPlayer() {
        return requiresPlayer;
    }

    /**
     * Checks if the sender has permission to use the command.
     * @param sender The command sender
     * @return true if the sender has permission
     */
    public boolean hasPermission(CommandSender sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    /**
     * Checks if the sender is a player.
     * @param sender The command sender
     * @return true if the sender is a player
     */
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    /**
     * Gets the command help message.
     * @return The help message
     */
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("§6=== ").append(name).append(" ===\n");
        help.append("§eDescription: §f").append(description).append("\n");
        help.append("§eUsage: §f").append(usage).append("\n");
        if (permission != null && !permission.isEmpty()) {
            help.append("§ePermission: §f").append(permission).append("\n");
        }
        return help.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
} 