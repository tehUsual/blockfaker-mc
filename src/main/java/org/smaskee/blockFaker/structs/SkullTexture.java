package org.smaskee.blockFaker.structs;

import java.util.UUID;

public class SkullTexture {
    private final String name;
    private final String value;
    private final UUID uuid;

    public SkullTexture(String name, String value, UUID uuid) {
        this.name = name;
        this.value = value;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public UUID getUuid() {
        return uuid;
    }
}