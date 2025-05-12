package org.smaskee.blockFaker.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.structs.SkullTexture;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final Plugin plugin;
    private final File dataFolder;
    private VisibilityManager visibilityManager;

    private final File blocksFile;
    private final File skullsFile;
    private final File texturesFile;

    private FileConfiguration blocksData;
    private FileConfiguration skullsData;
    private FileConfiguration texturesData;

    private final Map<String, FakeBlock> blocks;
    private final Map<String, FakeSkull> skulls;
    private final Map<String, SkullTexture> textures;

    private final String unknownMaterial = "cobblestone";

    public DataManager(Plugin plugin, File dataFolder) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;

        // Ensure data folder exists
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Initialize files
        this.blocksFile = new File(dataFolder, "blocks.yml");
        this.skullsFile = new File(dataFolder, "skulls.yml");
        this.texturesFile = new File(dataFolder, "textures.yml");

        this.blocks = new HashMap<>();
        this.skulls = new HashMap<>();
        this.textures = new HashMap<>();

        // Initialize visibility manager after plugin is fully loaded
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.visibilityManager = ((BlockFaker) plugin).getVisibilityManager();
            // load config after the worlds have loaded
            loadData();
        });
    }

    public void loadData() {
        // Load blocks
        if (!blocksFile.exists()) {
            try {
                blocksFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        blocksData = YamlConfiguration.loadConfiguration(blocksFile);
        loadBlocks();

        // Load skulls
        if (!skullsFile.exists()) {
            try {
                skullsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        skullsData = YamlConfiguration.loadConfiguration(skullsFile);
        loadSkulls();

        // Load textures
        if (!texturesFile.exists()) {
            try {
                texturesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        texturesData = YamlConfiguration.loadConfiguration(texturesFile);
        loadTextures();

        String msg = String.format("Loaded %d blocks, %d skulls and %d textures",
                blocks.size(), skulls.size(), textures.size());
        plugin.getLogger().log(Level.INFO, "\u001B[35m" + msg + "\u001B[0m");
    }

    private void loadBlocks() {
        ConfigurationSection blocksSection = blocksData.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String name : blocksSection.getKeys(false)) {
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(name);
                if (blockSection != null) {
                    String worldName = blockSection.getString("world", "");
                    World world = Bukkit.getWorld(worldName);
                    if (!worldName.isEmpty() && world != null) {
                        Location loc = new Location(
                                world,
                                blockSection.getDouble("x"),
                                blockSection.getDouble("y"),
                                blockSection.getDouble("z")
                        );
                        String materialName = blockSection.getString("material", unknownMaterial);
                        Material material = Material.matchMaterial(materialName);
                        FakeBlock block = new FakeBlock(name, loc, material);

                        // load visibility data
                        for (String uuidString : blockSection.getStringList("visibleTo")) {
                            block.setVisibleTo(UUID.fromString(uuidString), true);
                        }

                        blocks.put(name, block);
                    }
                }
            }
        }
    }

    private void loadSkulls() {
        ConfigurationSection skullsSection = skullsData.getConfigurationSection("skulls");
        if (skullsSection != null) {
            for (String name : skullsSection.getKeys(false)) {
                ConfigurationSection skullSection = skullsSection.getConfigurationSection(name);
                if (skullSection != null) {
                    String worldName = skullSection.getString("world", "");
                    World world = Bukkit.getWorld(worldName);
                    if (!worldName.isEmpty() && world != null) {
                        Location loc = new Location(
                                world,
                                skullSection.getDouble("x"),
                                skullSection.getDouble("y"),
                                skullSection.getDouble("z")
                        );
                        String textureName = skullSection.getString("texture", "");
                        BlockFace rotation = BlockFace.valueOf(skullSection.getString("rotation", "SOUTH"));
                        boolean isWallSkull = skullSection.getBoolean("isWallSkull", false);
                        FakeSkull skull = new FakeSkull(name, loc, textureName, rotation, isWallSkull);

                        // load visibility data
                        for (String uuidString : skullSection.getStringList("visibleTo")) {
                            skull.setVisibleTo(UUID.fromString(uuidString), true);
                        }

                        skulls.put(name, skull);
                    }
                }
            }
        }
    }

    private void loadTextures() {
        ConfigurationSection texturesSection = texturesData.getConfigurationSection("textures");
        if (texturesSection != null) {
            for (String name : texturesSection.getKeys(false)) {
                ConfigurationSection textureSection = texturesSection.getConfigurationSection(name);
                if (textureSection != null) {
                    String value = textureSection.getString("value");
                    String strId = textureSection.getString("uuid");
                    if (strId != null) {
                        textures.put(name, new SkullTexture(name, value, UUID.fromString(strId)));
                    }
                }
            }
        }
    }

    public void saveData() {
        saveBlocks();
        saveSkulls();
        saveTextures();
    }

    private void saveBlocks() {
        blocksData.set("blocks", null);
        ConfigurationSection blocksSection = blocksData.createSection("blocks");
        for (FakeBlock block : blocks.values()) {
            ConfigurationSection blockSection = blocksSection.createSection(block.getName());
            Location loc = block.getLocation();
            blockSection.set("world", loc.getWorld().getName());
            blockSection.set("x", loc.getX());
            blockSection.set("y", loc.getY());
            blockSection.set("z", loc.getZ());
            blockSection.set("material", block.getMaterial().toString());
            blockSection.set("visibleTo", block.getVisibleTo().stream().map(UUID::toString).toList());
        }

        try {
            blocksData.save(blocksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSkulls() {
        skullsData.set("skulls", null);
        ConfigurationSection skullsSection = skullsData.createSection("skulls");
        for (FakeSkull skull : skulls.values()) {
            ConfigurationSection skullSection = skullsSection.createSection(skull.getName());
            Location loc = skull.getLocation();
            skullSection.set("world", loc.getWorld().getName());
            skullSection.set("x", loc.getX());
            skullSection.set("y", loc.getY());
            skullSection.set("z", loc.getZ());
            skullSection.set("texture", skull.getTextureName());
            skullSection.set("rotation", skull.getRotation().name());
            skullSection.set("isWallSkull", skull.isWallSkull());
            skullSection.set("visibleTo", skull.getVisibleTo().stream().map(UUID::toString).toList());
        }

        try {
            skullsData.save(skullsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTextures() {
        texturesData.set("textures", null);
        ConfigurationSection texturesSection = texturesData.createSection("textures");
        for (SkullTexture skullTexture : textures.values()) {
            ConfigurationSection textureSection = texturesSection.createSection(skullTexture.getName());
            textureSection.set("value", skullTexture.getValue());
            textureSection.set("uuid", skullTexture.getUuid().toString());
        }

        try {
            texturesData.save(texturesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------
    // --- Skulls ---------------------------------------------------
    public void addSkull(FakeSkull skull) {
        skulls.put(skull.getName(), skull);
        saveSkulls();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] addSkull()\u001B[0m");
    }

    public void removeSkull(String name) {
        skulls.remove(name);
        saveSkulls();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] removeSkull()\u001B[0m");
    }

    public FakeSkull getSkull(String name) {
        return skulls.get(name);
    }

    public Map<String, FakeSkull> getAllSkulls() {
        return skulls;
    }

    public FakeSkull getSkullAtLocation(Location location) {
        for (FakeSkull skull : skulls.values()) {
            Location skullLoc = skull.getLocation();
            if (skullLoc.getWorld().equals(location.getWorld()) &&
                    skullLoc.getBlockX() == location.getBlockX() &&
                    skullLoc.getBlockY() == location.getBlockY() &&
                    skullLoc.getBlockZ() == location.getBlockZ()) {
                return skull;
            }
        }
        return null;
    }

    public boolean setSkullVisibility(String name, UUID playerId, boolean visible) {
        FakeSkull skull = skulls.get(name);
        if (skull == null)
            return false;

        skull.setVisibleTo(playerId, visible);
        visibilityManager.setSkullVisibility(skull, playerId, visible);
        return true;
    }

    // --------------------------------------------------------------
    // --- Skull textures -------------------------------------------
    public void addTexture(String name, String value, UUID uuid) {
        textures.put(name, new SkullTexture(name, value, uuid));
        saveTextures();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] addTexture()\u001B[0m");
    }

    public void removeTexture(String name) {
        textures.remove(name);
        saveTextures();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] removeTexture()\u001B[0m");
    }

    public SkullTexture getTexture(String name) {
        return textures.get(name);
    }

    public Map<String, SkullTexture> getAllTextures() {
        return new HashMap<>(textures);
    }

    public String getTextureNameByValue(String texture) {
        for (Map.Entry<String, SkullTexture> entry: textures.entrySet()) {
            if (entry.getValue().getValue().equals(texture))
                return entry.getKey();
        }
        return null;
    }

    // --------------------------------------------------------------
    // --- Blocks ---------------------------------------------------
    public void addBlock(FakeBlock block) {
        blocks.put(block.getName(), block);
        saveBlocks();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] addBlock()\u001B[0m");
    }

    public void removeBlock(String name) {
        blocks.remove(name);
        saveBlocks();

        if (BlockFaker.debug)
            plugin.getLogger().log(Level.INFO, "\u001B[35m[Saved] removeBlock()\u001B[0m");
    }

    public FakeBlock getBlock(String name) {
        return blocks.get(name);
    }

    public Map<String, FakeBlock> getAllBlocks() {
        return blocks;
    }

    public FakeBlock getBlockAtPosition(Location location) {
        for (FakeBlock block : blocks.values()) {
            Location blockLoc = block.getLocation();
            if (blockLoc.getWorld().equals(location.getWorld()) &&
                    blockLoc.getBlockX() == location.getBlockX() &&
                    blockLoc.getBlockY() == location.getBlockY() &&
                    blockLoc.getBlockZ() == location.getBlockZ()) {
                return block;
            }
        }
        return null;
    }

    public boolean setBlockVisibility(String name, UUID playerId, boolean visible) {
        FakeBlock block = blocks.get(name);
        if (block == null)
            return false;

        block.setVisibleTo(playerId, visible);
        visibilityManager.setBlockVisibility(block, playerId, visible);
        return true;
    }
}

