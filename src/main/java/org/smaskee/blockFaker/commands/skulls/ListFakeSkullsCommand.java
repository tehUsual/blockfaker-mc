package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandCategory;
import org.smaskee.blockFaker.commands.CommandContext;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Command for listing all registered fake skulls.
 */
public class ListFakeSkullsCommand extends BaseCommand {
    public ListFakeSkullsCommand(BlockFaker plugin) {
        super(plugin, "listfakeskulls", "blockfaker.listfakeskulls",
                "Lists all registered fake skulls",
                "/listfakeskulls",
                Arrays.asList("lfs", "listskulls"),
                false);
    }

    @Override
    public boolean execute(CommandContext context) {
        Map<String, FakeSkull> skulls = plugin.getDataManager().getAllSkulls();
        if (skulls.isEmpty()) {
            context.getSender().sendMessage("§cThere are no registered fake skulls.");
            return true;
        }

        context.getSender().sendMessage("§6=== Registered Fake Skulls ===");
        for (FakeSkull skull : skulls.values()) {
            context.getSender().sendMessage(String.format("§e%s §7at §f%d, %d, %d §7in §f%s §7with texture §f%s",
                    skull.getName(),
                    skull.getLocation().getBlockX(),
                    skull.getLocation().getBlockY(),
                    skull.getLocation().getBlockZ(),
                    skull.getLocation().getWorld().getName(),
                    skull.getTextureName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}