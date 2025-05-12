package org.smaskee.blockFaker.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.SkullTexture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages available textures for fake skulls.
 */
public class TextureManager {
    private final BlockFaker plugin;
    private final Map<String, SkullTexture> textures;
    private static final String CONFIG_PATH = "textures";

    public TextureManager(BlockFaker plugin) {
        this.plugin = plugin;
        this.textures = new HashMap<>();
        loadTextures();
    }

    private void loadTextures() {
        plugin.saveDefaultConfig();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(CONFIG_PATH);
        
        if (section == null) {
            // Add default textures if none exist
            addTexture("steve", "default_steve_value");
            addTexture("alex", "default_alex_value");
            addTexture("zombie", "default_zombie_value");
            addTexture("skeleton", "default_skeleton_value");
            addTexture("creeper", "default_creeper_value");
            saveTextures();
            return;
        }

        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                textures.put(key, new SkullTexture(key, value));
            }
        }
    }

    public void saveTextures() {
        ConfigurationSection section = plugin.getConfig().createSection(CONFIG_PATH);
        for (Map.Entry<String, SkullTexture> entry : textures.entrySet()) {
            section.set(entry.getKey(), entry.getValue().getValue());
        }
        plugin.saveConfig();
    }

    public boolean addTexture(String name, String value) {
        if (textures.containsKey(name)) return false;
        textures.put(name, new SkullTexture(name, value));
        saveTextures();
        return true;
    }

    public boolean removeTexture(String name) {
        boolean removed = textures.remove(name) != null;
        if (removed) {
            saveTextures();
        }
        return removed;
    }

    public SkullTexture getTexture(String name) {
        return textures.get(name);
    }

    /**
     * Checks if a texture is valid.
     * @param texture The texture to check
     * @return true if the texture is valid, false otherwise
     */
    public boolean isValidTexture(String texture) {
        return textures.containsKey(texture);
    }

    /**
     * Gets all available texture names.
     * @return A set of available texture names
     */
    public Set<String> getAvailableTextures() {
        return Collections.unmodifiableSet(textures.keySet());
    }

    /**
     * Gets all textures.
     * @return An unmodifiable map of all textures
     */
    public Map<String, SkullTexture> getAllTextures() {
        return Collections.unmodifiableMap(textures);
    }
} 