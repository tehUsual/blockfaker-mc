package org.smaskee.blockFaker.helpers;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldResolver {
    public static World getOverworld() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                return world;
            }
        }
        return null;
    }
}
