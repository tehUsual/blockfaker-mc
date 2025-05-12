package org.smaskee.blockFaker.commands;

/**
 * Enum representing different command categories.
 */
public enum CommandCategory {
    BLOCKS("Blocks", "Commands for managing fake blocks"),
    SKULLS("Skulls", "Commands for managing fake skulls"),
    TEXTURES("Textures", "Commands for managing skull textures"),
    ADMIN("Admin", "Administrative commands");

    private final String name;
    private final String description;

    /**
     * Creates a new command category.
     * @param name The category name
     * @param description The category description
     */
    CommandCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the category name.
     * @return The category name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the category description.
     * @return The category description
     */
    public String getDescription() {
        return description;
    }
} 