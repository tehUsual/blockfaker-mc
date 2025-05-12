package org.smaskee.blockFaker.structs;

/**
 * Represents a texture for fake skulls.
 */
public class SkullTexture {
    private final String name;
    private final String value;

    public SkullTexture(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Texture name cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Texture value cannot be null or empty");
        }
        this.name = name.trim();
        this.value = value.trim();
    }

    /**
     * Gets the name of this texture.
     * @return The texture name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of this texture.
     * @return The texture value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("SkullTexture{name='%s', value='%s'}", name, value);
    }
}