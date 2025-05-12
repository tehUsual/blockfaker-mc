package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class representing the context of a command execution.
 */
public class CommandContext {
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] args;
    private final Player player;

    /**
     * Creates a new command context.
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     */
    public CommandContext(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
        this.player = sender instanceof Player ? (Player) sender : null;
    }

    /**
     * Gets the command sender.
     * @return The command sender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the command.
     * @return The command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Gets the command label.
     * @return The command label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the command arguments.
     * @return The command arguments
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Gets the player who executed the command, if any.
     * @return The player, or null if the sender is not a player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if the sender is a player.
     * @return true if the sender is a player
     */
    public boolean isPlayer() {
        return player != null;
    }

    /**
     * Gets the number of arguments.
     * @return The number of arguments
     */
    public int getArgCount() {
        return args.length;
    }

    /**
     * Gets an argument at the specified index.
     * @param index The argument index
     * @return The argument, or null if the index is out of bounds
     */
    public String getArg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }

    /**
     * Gets all arguments from the specified index to the end.
     * @param startIndex The start index
     * @return The arguments
     */
    public String[] getArgsFrom(int startIndex) {
        if (startIndex >= args.length) {
            return new String[0];
        }
        String[] result = new String[args.length - startIndex];
        System.arraycopy(args, startIndex, result, 0, result.length);
        return result;
    }

    /**
     * Gets all arguments from the specified start index to the specified end index.
     * @param startIndex The start index
     * @param endIndex The end index
     * @return The arguments
     */
    public String[] getArgsFromTo(int startIndex, int endIndex) {
        if (startIndex >= args.length || endIndex < startIndex) {
            return new String[0];
        }
        endIndex = Math.min(endIndex, args.length - 1);
        String[] result = new String[endIndex - startIndex + 1];
        System.arraycopy(args, startIndex, result, 0, result.length);
        return result;
    }
} 