package org.smaskee.blockFaker.helpers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimplePlayer {
    private static final Map<String, SimplePlayer> playerCache = new HashMap<>();
    private static final Map<UUID, SimplePlayer> uuidCache = new HashMap<>();

    private final Player onlinePlayer;
    private final OfflinePlayer offlinePlayer;
    private final String playerName;
    private final UUID playerId;
    private final boolean isValid;
    private final String errorMessage;

    private SimplePlayer(String playerName) {
        this.onlinePlayer = Bukkit.getPlayerExact(playerName);
        this.offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        // Try to get player info from online player first
        if (this.onlinePlayer != null) {
            this.playerName = this.onlinePlayer.getName();
            this.playerId = this.onlinePlayer.getUniqueId();
            this.isValid = true;
            this.errorMessage = null;
        } else {
            // Fall back to offline player
            this.playerName = this.offlinePlayer.getName();
            this.playerId = this.offlinePlayer.getUniqueId();
            this.isValid = this.offlinePlayer.hasPlayedBefore();
            this.errorMessage = this.isValid ? null : "Player has never played before";
        }

        // Update caches
        if (this.isValid) {
            playerCache.put(this.playerName.toLowerCase(), this);
            uuidCache.put(this.playerId, this);
        }
    }

    public static SimplePlayer get(String playerName) {
        if (playerName == null) return null;
        
        // Check cache first
        SimplePlayer cached = playerCache.get(playerName.toLowerCase());
        if (cached != null) return cached;
        
        return new SimplePlayer(playerName);
    }

    public static SimplePlayer get(UUID playerId) {
        if (playerId == null) return null;
        
        // Check cache first
        SimplePlayer cached = uuidCache.get(playerId);
        if (cached != null) return cached;
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return new SimplePlayer(offlinePlayer.getName());
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getName() {
        return playerName;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isOnline() {
        return onlinePlayer != null && onlinePlayer.isOnline();
    }

    public Player getPlayer() {
        return onlinePlayer;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public static void clearCache() {
        playerCache.clear();
        uuidCache.clear();
    }
}
