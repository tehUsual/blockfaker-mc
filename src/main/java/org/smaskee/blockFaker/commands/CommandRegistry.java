package org.smaskee.blockFaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.blocks.CreateFakeBlockCommand;
import org.smaskee.blockFaker.commands.blocks.DeleteFakeBlockCommand;
import org.smaskee.blockFaker.commands.blocks.ListFakeBlocksCommand;
import org.smaskee.blockFaker.commands.skulls.CreateFakeSkullCommand;
import org.smaskee.blockFaker.commands.skulls.DeleteFakeSkullCommand;
import org.smaskee.blockFaker.commands.skulls.ListFakeSkullsCommand;
import org.smaskee.blockFaker.commands.textures.AddTextureCommand;
import org.smaskee.blockFaker.commands.textures.ListTexturesCommand;
import org.smaskee.blockFaker.commands.textures.RemoveTextureCommand;
import org.smaskee.blockFaker.commands.textures.CreateTextureFromSkullCommand;

import java.util.*;

/**
 * Class for managing command registration and execution.
 */
public class CommandRegistry implements CommandExecutor, TabCompleter {
    private final BlockFaker plugin;
    private final Map<String, BaseCommand> commands;
    private final Map<CommandCategory, List<BaseCommand>> categoryCommands;

    /**
     * Creates a new command registry.
     * @param plugin The plugin instance
     */
    public CommandRegistry(BlockFaker plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.categoryCommands = new EnumMap<>(CommandCategory.class);
        for (CommandCategory category : CommandCategory.values()) {
            categoryCommands.put(category, new ArrayList<>());
        }
    }

    /**
     * Registers all commands.
     */
    public void registerAllCommands() {
        // Register block commands
        registerCommand(new CreateFakeBlockCommand(plugin), CommandCategory.BLOCKS);
        registerCommand(new DeleteFakeBlockCommand(plugin), CommandCategory.BLOCKS);
        registerCommand(new ListFakeBlocksCommand(plugin), CommandCategory.BLOCKS);

        // Register skull commands
        registerCommand(new CreateFakeSkullCommand(plugin), CommandCategory.SKULLS);
        registerCommand(new DeleteFakeSkullCommand(plugin), CommandCategory.SKULLS);
        registerCommand(new ListFakeSkullsCommand(plugin), CommandCategory.SKULLS);

        // Register texture commands
        registerCommand(new ListTexturesCommand(plugin), CommandCategory.TEXTURES);
        registerCommand(new AddTextureCommand(plugin), CommandCategory.TEXTURES);
        registerCommand(new RemoveTextureCommand(plugin), CommandCategory.TEXTURES);
        registerCommand(new CreateTextureFromSkullCommand(plugin), CommandCategory.TEXTURES);
    }

    /**
     * Registers a command.
     * @param command The command to register
     */
    public void registerCommand(BaseCommand command) {
        commands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }
    }

    /**
     * Registers a command in a category.
     * @param command The command to register
     * @param category The category to register the command in
     */
    public void registerCommand(BaseCommand command, CommandCategory category) {
        registerCommand(command);
        categoryCommands.get(category).add(command);
    }

    /**
     * Gets a command by name.
     * @param name The command name
     * @return The command, or null if not found
     */
    public BaseCommand getCommand(String name) {
        return commands.get(name);
    }

    /**
     * Gets all commands in a category.
     * @param category The category
     * @return The commands in the category
     */
    public List<BaseCommand> getCommands(CommandCategory category) {
        return new ArrayList<>(categoryCommands.get(category));
    }

    /**
     * Gets all registered commands.
     * @return All registered commands
     */
    public Collection<BaseCommand> getAllCommands() {
        return new ArrayList<>(commands.values());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BaseCommand baseCommand = getCommand(label);
        if (baseCommand == null) {
            return false;
        }

        if (!baseCommand.hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (baseCommand.requiresPlayer() && !baseCommand.isPlayer(sender)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        CommandContext context = new CommandContext(sender, command, label, args);
        return baseCommand.execute(context);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        BaseCommand baseCommand = getCommand(alias);
        if (baseCommand == null) {
            return null;
        }

        if (!baseCommand.hasPermission(sender)) {
            return new ArrayList<>();
        }

        return baseCommand.onTabComplete(sender, command, alias, args);
    }

    /**
     * Gets the help message for a command.
     * @param command The command
     * @return The help message
     */
    public String getCommandHelp(BaseCommand command) {
        return command.getHelp();
    }

    /**
     * Gets the help message for a category.
     * @param category The category
     * @return The help message
     */
    public String getCategoryHelp(CommandCategory category) {
        StringBuilder help = new StringBuilder();
        help.append("§6=== ").append(category.getName()).append(" ===\n");
        help.append("§eDescription: §f").append(category.getDescription()).append("\n\n");
        help.append("§eCommands:\n");
        for (BaseCommand command : categoryCommands.get(category)) {
            help.append("§6- §f").append(command.getName()).append(": §e").append(command.getDescription()).append("\n");
        }
        return help.toString();
    }

    /**
     * Gets the help message for all commands.
     * @return The help message
     */
    public String getAllHelp() {
        StringBuilder help = new StringBuilder();
        help.append("§6=== BlockFaker Commands ===\n\n");
        for (CommandCategory category : CommandCategory.values()) {
            help.append(getCategoryHelp(category)).append("\n");
        }
        return help.toString();
    }
} 