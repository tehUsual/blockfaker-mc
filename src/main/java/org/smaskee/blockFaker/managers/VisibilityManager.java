package org.smaskee.blockFaker.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.structs.FakeEntity;
import org.smaskee.blockFaker.managers.EntityType;
import org.smaskee.blockFaker.managers.LocationManager;
import org.smaskee.blockFaker.managers.BlockPacketHandler;
// TODO: import SkullPacketHandler when implemented

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VisibilityManager implements Listener {
    private final Plugin plugin;
    private DataManager dataManager;
    private BlockSender blockSender;
    private SkullSender skullSender;
    private final LocationManager locationManager;
    private final BlockPacketHandler blockPacketHandler;
    // private final SkullPacketHandler skullPacketHandler; // TODO: implement this

    // Maps player UUID to set of locations where they can see entities
    private final Map<UUID, Set<Location>> playerVisibleLocations;
    // Maps location to set of player UUIDs who can see entities there
    private final Map<Location, Set<UUID>> locationVisiblePlayers;

    // Maps player UUID to sets of blocks they can see
    private final Map<UUID, Set<FakeBlock>> visibleBlocks;
    private final Map<UUID, Set<FakeSkull>> visibleSkulls;
    // Maps location to sets of blocks/skulls at that location
    private final Map<Location, Map<UUID, FakeBlock>> blockViewers;
    private final Map<Location, Map<UUID, FakeSkull>> skullViewers;

    private final Set<FakeBlock> blockUpdateBlocks;
    private final Set<FakeSkull> blockUpdateSkulls;

    // New maps for tracking nearby status
    private final Map<UUID, Map<FakeBlock, Boolean>> blockNearbyStatus;
    private final Map<UUID, Map<FakeSkull, Boolean>> skullNearbyStatus;
    private static final double NEARBY_MAX_TRESH = Math.min(Bukkit.getViewDistance() * 16 - 1, 80); // block threshold
    private static final double NEARBY_MIN_TRESH = Math.max(0, NEARBY_MAX_TRESH - 17);

    // Track last update time and pending updates for each player
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
    private final Set<UUID> pendingUpdates = new HashSet<>();
    private static final long UPDATE_COOLDOWN = 1000; // 1 second cooldown

    public VisibilityManager(BlockFaker plugin, LocationManager locationManager,
                           BlockPacketHandler blockPacketHandler/*, SkullPacketHandler skullPacketHandler*/) {
        this.plugin = plugin;
        this.locationManager = locationManager;
        this.blockPacketHandler = blockPacketHandler;
        // this.skullPacketHandler = skullPacketHandler; // TODO: implement this
        this.playerVisibleLocations = new ConcurrentHashMap<>();
        this.locationVisiblePlayers = new ConcurrentHashMap<>();

        // Initialize maps with thread-safe implementations
        this.visibleBlocks = new ConcurrentHashMap<>();
        this.visibleSkulls = new ConcurrentHashMap<>();
        this.blockViewers = new ConcurrentHashMap<>();
        this.skullViewers = new ConcurrentHashMap<>();
        this.blockNearbyStatus = new ConcurrentHashMap<>();
        this.skullNearbyStatus = new ConcurrentHashMap<>();

        // In queue for block updates
        this.blockUpdateBlocks = new HashSet<>();
        this.blockUpdateSkulls = new HashSet<>();

        // Register this class as an event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.dataManager = plugin.getDataManager();
            this.blockSender = plugin.getBlockSender();
            this.skullSender = plugin.getSkullSender();
            // load config after the worlds have loaded

            // Initialize visibility for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                initializeMovementUpdater(player);
                initializePlayerVisibility(player);
                sendAllVisibleToPlayer(player);
            }
        }, 3L);
    }

    // -----------------------------------------------------------------------
    // --- PLAYER MOVEMENT UPDATE --------------------------------------------
    private boolean isNearby(Location playerLoc, Location entityLoc) {
        if (!playerLoc.getWorld().equals(entityLoc.getWorld())) {
            return false;
        }
        //double distance = playerLoc.distanceSquared(entityLoc);
        //return distance <= NEARBY_MAX_TRESH * NEARBY_MAX_TRESH && distance >= NEARBY_MIN_TRESH * NEARBY_MIN_TRESH;
        return playerLoc.distanceSquared(entityLoc) <= NEARBY_MAX_TRESH * NEARBY_MAX_TRESH;
    }

    private void updateNearbyStatus(Player player) {
        updateNearbyStatus(player, true);
    }

    private void updateNearbyStatus(Player player, boolean sendUpdate) {
        UUID playerId = player.getUniqueId();
        Location playerLoc = player.getLocation();

        // Update block nearby status
        Map<FakeBlock, Boolean> playerBlockStatus = blockNearbyStatus.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        for (Map.Entry<FakeBlock, Boolean> entry : playerBlockStatus.entrySet()) {
            FakeBlock block = entry.getKey();
            boolean wasNearby = entry.getValue();
            boolean isNowNearby = isNearby(playerLoc, block.getLocation());

            if (wasNearby != isNowNearby) {
                playerBlockStatus.put(block, isNowNearby);
                if (isNowNearby && sendUpdate) {
                    blockUpdateBlocks.add(block);
                    blockSender.sendBlock(player, block, true);

                    if (BlockFaker.debug)
                        plugin.getLogger().info("[Move]: sending block " + block.getName() + " [" + playerBlockStatus.size() + "]");
                }
            }
        }

        // Update skull nearby status
        Map<FakeSkull, Boolean> playerSkullStatus = skullNearbyStatus.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        for (Map.Entry<FakeSkull, Boolean> entry : playerSkullStatus.entrySet()) {
            FakeSkull skull = entry.getKey();
            boolean wasNearby = entry.getValue();
            boolean isNowNearby = isNearby(playerLoc, skull.getLocation());

            if (wasNearby != isNowNearby) {
                playerSkullStatus.put(skull, isNowNearby);
                if (isNowNearby && sendUpdate) {
                    blockUpdateSkulls.add(skull);
                    // TODO: send skull packet when SkullPacketHandler is implemented

                    if (BlockFaker.debug)
                        plugin.getLogger().info("[Move]: sending block " + skull.getName() + " [" + playerSkullStatus.size() + "]");
                }
            }
        }
    }

    private void scheduleDelayedUpdate(Player player) {
        UUID playerId = player.getUniqueId();

        // If there's already a pending update, don't schedule another one
        if (pendingUpdates.contains(playerId)) {
            return;
        }

        pendingUpdates.add(playerId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateNearbyStatus(player);
            lastUpdateTime.put(playerId, System.currentTimeMillis());
            pendingUpdates.remove(playerId);

//            if (BlockFaker.debug)
//                plugin.getLogger().info("[Move]: scheduled updated");
        }, 20L);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only proceed if the player actually moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Check if the player moved to a different chunk
        boolean chunkChanged = event.getFrom().getChunk().getX() != event.getTo().getChunk().getX() ||
                event.getFrom().getChunk().getZ() != event.getTo().getChunk().getZ();

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // If chunk changed
        if (chunkChanged) {
            long currentTime = System.currentTimeMillis();
            if (!pendingUpdates.contains(playerId) &&
                    (!lastUpdateTime.containsKey(playerId) || currentTime - lastUpdateTime.get(playerId) >= UPDATE_COOLDOWN))
            {
                // If it's been more than UPDATE_COOLDOWN, update immediately
                updateNearbyStatus(player);
                lastUpdateTime.put(playerId, currentTime);

            // Not pending update
            } else if (!pendingUpdates.contains(playerId)) {
                scheduleDelayedUpdate(player);

//                if (BlockFaker.debug)
//                    plugin.getLogger().info("[Move]: scheduled");
            }
        }
    }



    // -----------------------------------------------------------------------
    // --- OTHER -------------------------------------------------------------

    public void initializeMovementUpdater(Player player) {
        lastUpdateTime.put(player.getUniqueId(), System.currentTimeMillis());

//        if (BlockFaker.debug)
//            plugin.getLogger().info("[Move]: updating");
    }

    /**
     * Initializes visibility for a player based on DataManager's data
     * @param player The player to initialize visibility for
     */
    public void initializePlayerVisibility(Player player) {
        UUID playerId = player.getUniqueId();

        // Initialize empty sets for the player
        visibleBlocks.put(playerId, new HashSet<>());
        visibleSkulls.put(playerId, new HashSet<>());

        // Load blocks the player should see
        for (FakeBlock block : dataManager.getAllBlocks().values()) {
            if (block.isVisibleTo(playerId)) {
                setBlockVisibility(block, playerId, true);
            }
        }

        // Load skulls the player should see
        for (FakeSkull skull : dataManager.getAllSkulls().values()) {
            if (skull.isVisibleTo(playerId)) {
                setSkullVisibility(skull, playerId, true);
            }
        }
    }

    public boolean shouldBlockUpdate(FakeBlock block) {
        if (blockUpdateBlocks.contains(block)) {
            blockUpdateBlocks.remove(block);
            return true;
        }
        return false;
    }

    public boolean shouldSkullUpdate(FakeSkull skull) {
        if (blockUpdateSkulls.contains(skull)) {
            blockUpdateSkulls.remove(skull);
            return true;
        }
        return false;
    }

    /**
     * Sets the visibility of a block for a player
     * @param block The block to set visibility for
     * @param playerId The UUID of the player
     * @param visible Whether the block should be visible
     */
    public void setBlockVisibility(FakeBlock block, UUID playerId, boolean visible) {
        Set<FakeBlock> playerBlocks = visibleBlocks.computeIfAbsent(playerId, k -> new HashSet<>());
        Map<UUID, FakeBlock> locationBlocks = blockViewers.computeIfAbsent(block.getLocation(), k -> new ConcurrentHashMap<>());
        Map<FakeBlock, Boolean> playerBlockStatus = blockNearbyStatus.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        blockUpdateBlocks.add(block);

        if (visible) {
            playerBlocks.add(block);
            locationBlocks.put(playerId, block);

            Player player = Bukkit.getPlayer(playerId);
            if (player != null)
                playerBlockStatus.put(block, isNearby(player.getLocation(), block.getLocation()));
        } else {
            playerBlocks.remove(block);
            locationBlocks.remove(playerId, block);
            playerBlockStatus.remove(block);
        }
    }

    /**
     * Sets the visibility of a skull for a player
     * @param skull The skull to set visibility for
     * @param playerId The UUID of the player
     * @param visible Whether the skull should be visible
     */
    public void setSkullVisibility(FakeSkull skull, UUID playerId, boolean visible) {
        Set<FakeSkull> playerSkulls = visibleSkulls.computeIfAbsent(playerId, k -> new HashSet<>());
        Map<UUID, FakeSkull> locationSkulls = skullViewers.computeIfAbsent(skull.getLocation(), k -> new ConcurrentHashMap<>());
        Map<FakeSkull, Boolean> playerSkullStatus = skullNearbyStatus.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        blockUpdateSkulls.add(skull);

        if (visible) {
            playerSkulls.add(skull);
            locationSkulls.put(playerId, skull);

            Player player = Bukkit.getPlayer(playerId);
            if (player != null)
                playerSkullStatus.put(skull, isNearby(player.getLocation(), skull.getLocation()));
        } else {
            playerSkulls.remove(skull);
            locationSkulls.remove(playerId, skull);
            playerSkullStatus.remove(skull);
        }
    }

    /**
     * Sends all visible blocks and skulls to a player
     * @param player The player to send blocks and skulls to
     */
    public void sendAllVisibleToPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        Set<FakeBlock> visibleBlocksSet = visibleBlocks.get(playerId);
        Set<FakeSkull> visibleSkullsSet = visibleSkulls.get(playerId);

        if (visibleBlocksSet != null || visibleSkullsSet != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (visibleBlocksSet != null) {
                    for (FakeBlock block : visibleBlocksSet) {
                        // Only send blocks that are in the same world as the player
                        if (block.getLocation().getWorld().equals(player.getWorld())) {
                            blockUpdateBlocks.add(block);
                            blockSender.sendBlock(player, block, true);
                        }
                    }
                }

                if (visibleSkullsSet != null) {
                    for (FakeSkull skull : visibleSkullsSet) {
                        // Only send skulls that are in the same world as the player
                        if (skull.getLocation().getWorld().equals(player.getWorld())) {
                            blockUpdateSkulls.add(skull);
                            // TODO: send skull packet when SkullPacketHandler is implemented
                        }
                    }
                }
            }, 20L);
        }
    }

    public void updateFake(Player player, Location location) {
        updateFakeBlock(player, location);
        updateFakeSkull(player, location);
    }

    public void updateFakeBlock(Player player, Location location) {
        FakeBlock fakeBlock = getBlockAtPosForPlayer(player, location);
        if (fakeBlock != null) {
            blockSender.sendBlock(player, fakeBlock, true);

            if (BlockFaker.debug)
                plugin.getLogger().info("[Proto][Upd] Updating block");
        }
    }

    public void updateFakeSkull(Player player, Location location) {
        FakeSkull fakeSkull = getSkullAtPosForPlayer(player, location);
        if (fakeSkull != null) {
            // TODO: send skull packet when SkullPacketHandler is implemented

            if (BlockFaker.debug)
                plugin.getLogger().info("[Proto][Upd] Updating skull");
        }
    }

    /**
     * Checks if a player should see a specific block
     * @param player The player to check
     * @param block The block to check
     * @return true if the player should see the block
     */
    public boolean shouldSeeBlock(Player player, FakeBlock block) {
        Set<FakeBlock> playerBlocks = visibleBlocks.get(player.getUniqueId());
        return playerBlocks != null && playerBlocks.contains(block);
    }

    /**
     * Checks if a player should see a specific skull
     * @param player The player to check
     * @param skull The skull to check
     * @return true if the player should see the skull
     */
    public boolean shouldSeeSkull(Player player, FakeSkull skull) {
        Set<FakeSkull> playerSkulls = visibleSkulls.get(player.getUniqueId());
        return playerSkulls != null && playerSkulls.contains(skull);
    }

    public boolean isFakeBlockForPlayer(Player player, Location location) {
        return blockViewers.getOrDefault(location, Collections.emptyMap())
                .get(player.getUniqueId()) != null;
    }

    public boolean isFakeSkullForPlayer(Player player, Location location) {
        return skullViewers.getOrDefault(location, Collections.emptyMap())
                .get(player.getUniqueId()) != null;
    }

    public FakeBlock getBlockAtPosForPlayer(Player player, Location location) {
        Map<UUID, FakeBlock> fakeBlocks = blockViewers.get(location);
        if (fakeBlocks != null)
            return fakeBlocks.get(player.getUniqueId());
        return null;
    }

    public FakeSkull getSkullAtPosForPlayer(Player player, Location location) {
        Map<UUID, FakeSkull> fakeSkulls = skullViewers.get(location);
        if (fakeSkulls != null)
            return fakeSkulls.get(player.getUniqueId());
        return null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize movement updater
        initializeMovementUpdater(event.getPlayer());
        updateNearbyStatus(event.getPlayer(),false);

        // Initialize visibility for the player
        initializePlayerVisibility(event.getPlayer());
        // Send all visible blocks and skulls
        sendAllVisibleToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player data
        UUID playerId = event.getPlayer().getUniqueId();
        visibleBlocks.remove(playerId);
        visibleSkulls.remove(playerId);
        blockNearbyStatus.remove(playerId);
        skullNearbyStatus.remove(playerId);
        pendingUpdates.remove(playerId);
        lastUpdateTime.remove(playerId);


        for (Map<UUID, FakeBlock> locationBlocks : blockViewers.values()) {
            locationBlocks.remove(playerId);
        }
        for (Map<UUID, FakeSkull> locationSkulls : skullViewers.values()) {
            locationSkulls.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Send all visible blocks and skulls
        sendAllVisibleToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Update nearby list
        updateNearbyStatus(event.getPlayer(),false);

        // Send all visible blocks and skulls
        sendAllVisibleToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
//        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
//            return;
//        }
//
//        plugin.getLogger().info("[Bukkit] interact");
//
//        String strBlockPos = "";
//        Material matType = Material.COBBLESTONE;
//        if (event.getClickedBlock() != null) {
//            Location loc = event.getClickedBlock().getLocation();
//            strBlockPos = String.format("(%d, %d, %d)",
//                    loc.getBlockX(),
//                    loc.getBlockY(),
//                    loc.getBlockZ());
//            matType = event.getClickedBlock().getType();
//        }
//        boolean wasMain = (event.getHand() != EquipmentSlot.HAND);
//        //plugin.getLogger().info("[Bukkit] Main(" + wasMain + ") at " + strBlockPos + " [" + matType.toString() + "]");
//
//
//        Player player = event.getPlayer();
//        Block clickedBlock = event.getClickedBlock();
//        if (clickedBlock != null) {
//            boolean updated = updateFakeOnEvent(player, clickedBlock.getLocation(), 1);
//            if (updated) {
//                event.setCancelled(true);
//            }
//        }
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        if (BlockFaker.debug)
            plugin.getLogger().info("[BlockBreak]");

        Player player = event.getPlayer();
        Block clickedBlock = event.getBlock();

        boolean updated = updateFakeOnEvent(player, clickedBlock.getLocation(), 1);
        if (updated) {
            event.setCancelled(true);
        }
    }

    public boolean updateFakeOnEvent(Player player, Location location, long ticks) {
        boolean hasFakeBlockOrSkull = false;

        // Check for blocks at this location
        FakeBlock fakeBlock = getBlockAtPosForPlayer(player, location);
        if (fakeBlock != null) {
            if (shouldSeeBlock(player, fakeBlock)) {
                hasFakeBlockOrSkull = true;
            }
        }

        // Check for skulls at this location
        FakeSkull fakeSkull = getSkullAtPosForPlayer(player, location);
        if (fakeSkull != null) {
            if (shouldSeeSkull(player, fakeSkull)) {
                hasFakeBlockOrSkull = true;
            }
        }


        // Cancel the event if a fake block or skull was clicked
        if (hasFakeBlockOrSkull) {
            if (BlockFaker.debug) {
                if (fakeBlock != null)
                    plugin.getLogger().info("[Bukkit] sending fake block");
                else
                    plugin.getLogger().info("[Bukkit] sending fake skull");
            }

            // Schedule a delayed task to resend the block/skull after 2 ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (fakeBlock != null) {
                    blockUpdateBlocks.add(fakeBlock);
                    blockSender.sendBlock(player, fakeBlock, true);

                    if (BlockFaker.debug)
                        plugin.getLogger().info("[Bukkit] sent fake skull");
                }

                if (fakeSkull != null) {
                    blockUpdateSkulls.add(fakeSkull);
                    // TODO: send skull packet when SkullPacketHandler is implemented

                    if (BlockFaker.debug)
                        plugin.getLogger().info("[Bukkit] sent fake skull");
                }

            }, ticks);

            return true;
        }
        return false;
    }

    /**
     * Shows an entity to a specific player.
     * If another entity type is visible at the same location, it will be hidden.
     * @param entity The entity to show
     * @param player The player to show the entity to
     * @return true if the entity was shown successfully
     */
    public boolean showEntity(FakeEntity entity, Player player) {
        Location loc = entity.getLocation();
        UUID playerId = player.getUniqueId();
        
        // Determine which type to show
        EntityType typeToShow = entity instanceof FakeBlock ? EntityType.BLOCK : EntityType.SKULL;
        
        // Check if the other type is currently visible
        EntityType currentType = locationManager.getVisibleType(loc, playerId);
        if (currentType != null && currentType != typeToShow) {
            // Hide the currently visible entity
            hideEntityAtLocation(loc, player, currentType);
        }
        
        // Update visibility tracking
        playerVisibleLocations.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                .add(loc);
        locationVisiblePlayers.computeIfAbsent(loc, k -> ConcurrentHashMap.newKeySet())
                .add(playerId);
        
        // Set the new visible type
        locationManager.setVisibleType(loc, playerId, typeToShow);
        
        // Send the appropriate packet
        if (typeToShow == EntityType.BLOCK) {
            blockPacketHandler.sendPacket(player, entity, true);
        } else {
            // TODO: send skull packet when SkullPacketHandler is implemented
        }
        
        return true;
    }

    /**
     * Hides an entity from a specific player.
     * @param entity The entity to hide
     * @param player The player to hide the entity from
     */
    public void hideEntity(FakeEntity entity, Player player) {
        Location loc = entity.getLocation();
        UUID playerId = player.getUniqueId();
        
        // Determine which type to hide
        EntityType typeToHide = entity instanceof FakeBlock ? EntityType.BLOCK : EntityType.SKULL;
        
        hideEntityAtLocation(loc, player, typeToHide);
    }

    /**
     * Hides all entities of a specific type at a location from a player.
     * @param location The location to hide entities at
     * @param player The player to hide entities from
     * @param type The type of entity to hide
     */
    private void hideEntityAtLocation(Location location, Player player, EntityType type) {
        UUID playerId = player.getUniqueId();
        
        // Update visibility tracking
        Set<Location> playerLocations = playerVisibleLocations.get(playerId);
        if (playerLocations != null) {
            playerLocations.remove(location);
            if (playerLocations.isEmpty()) {
                playerVisibleLocations.remove(playerId);
            }
        }
        
        Set<UUID> locationPlayers = locationVisiblePlayers.get(location);
        if (locationPlayers != null) {
            locationPlayers.remove(playerId);
            if (locationPlayers.isEmpty()) {
                locationVisiblePlayers.remove(location);
            }
        }
        
        // Clear the visible type
        locationManager.clearVisibleType(location, playerId);
        
        // Send the appropriate hide packet
        if (type == EntityType.BLOCK) {
            FakeBlock block = locationManager.getBlock(location);
            if (block != null) {
                blockPacketHandler.sendPacket(player, block, false);
            }
        } else {
            FakeSkull skull = locationManager.getSkull(location);
            if (skull != null) {
                // TODO: send skull packet when SkullPacketHandler is implemented
            }
        }
    }

    /**
     * Shows an entity to all online players.
     * @param entity The entity to show
     */
    public void showEntityToAll(FakeEntity entity) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            showEntity(entity, player);
        }
    }

    /**
     * Hides an entity from all online players.
     * @param entity The entity to hide
     */
    public void hideEntityFromAll(FakeEntity entity) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            hideEntity(entity, player);
        }
    }

    /**
     * Checks if a player can see an entity.
     * @param entity The entity to check
     * @param player The player to check
     * @return true if the player can see the entity
     */
    public boolean canSeeEntity(FakeEntity entity, Player player) {
        Location loc = entity.getLocation();
        UUID playerId = player.getUniqueId();
        
        Set<Location> playerLocations = playerVisibleLocations.get(playerId);
        if (playerLocations == null || !playerLocations.contains(loc)) {
            return false;
        }
        
        EntityType visibleType = locationManager.getVisibleType(loc, playerId);
        if (visibleType == null) {
            return false;
        }
        
        return (entity instanceof FakeBlock && visibleType == EntityType.BLOCK) ||
               (entity instanceof FakeSkull && visibleType == EntityType.SKULL);
    }

    /**
     * Gets all locations where a player can see entities.
     * @param player The player to check
     * @return Set of locations where the player can see entities
     */
    public Set<Location> getVisibleLocations(Player player) {
        return new HashSet<>(playerVisibleLocations.getOrDefault(player.getUniqueId(), Collections.emptySet()));
    }

    /**
     * Gets all players who can see entities at a location.
     * @param location The location to check
     * @return Set of player UUIDs who can see entities at the location
     */
    public Set<UUID> getVisiblePlayers(Location location) {
        return new HashSet<>(locationVisiblePlayers.getOrDefault(location, Collections.emptySet()));
    }

    /**
     * Cleans up all visibility data for a specific player.
     * @param playerId The UUID of the player
     */
    public void cleanupPlayer(UUID playerId) {
        Set<Location> locations = playerVisibleLocations.remove(playerId);
        if (locations != null) {
            for (Location loc : locations) {
                Set<UUID> players = locationVisiblePlayers.get(loc);
                if (players != null) {
                    players.remove(playerId);
                    if (players.isEmpty()) {
                        locationVisiblePlayers.remove(loc);
                    }
                }
            }
        }
        locationManager.cleanupPlayer(playerId);
    }
}
