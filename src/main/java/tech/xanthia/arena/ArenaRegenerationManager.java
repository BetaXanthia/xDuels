package tech.xanthia.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages arena regeneration using two different modes:
 * 
 * Mode 1 - Schematic Restore:
 * - Captures full arena state as schematic
 * - Restores arena to original state after each duel
 * - Higher memory usage but guaranteed perfect restoration
 * 
 * Mode 2 - Block Tracking:
 * - Tracks only blocks modified during duels
 * - Restores only changed blocks
 * - Lower memory usage and faster for minimal changes
 * 
 * Additionally handles:
 * - Entity cleanup (items, projectiles, TNT)
 * - Debris removal
 * - Async rollback queue
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaRegenerationManager {
    
    private final XDuels plugin;
    private final ArenaSchematicHandler schematicHandler;
    
    // Block tracking for non-schematic regeneration
    private final Map<UUID, BlockTracker> activeBlockTrackers;
    
    // Regeneration queue
    private final Queue<RegenerationTask> regenerationQueue;
    
    /**
     * Creates a new regeneration manager.
     * 
     * @param plugin Plugin instance
     */
    public ArenaRegenerationManager(XDuels plugin) {
        this.plugin = plugin;
        this.schematicHandler = new ArenaSchematicHandler(plugin);
        this.activeBlockTrackers = new ConcurrentHashMap<>();
        this.regenerationQueue = new LinkedList<>();
    }
    
    // ========================================
    // REGENERATION - MAIN METHODS
    // ========================================
    
    /**
     * Regenerates an arena based on its configured mode.
     * This method determines whether to use schematic or block tracking.
     * 
     * @param arena Arena to regenerate
     */
    public void regenerateArena(Arena arena) {
        if (!arena.isRegenerationEnabled()) {
            plugin.getLogger().info("[ ArenaRegeneration ] Regeneration disabled for " + arena.getId());
            return;
        }
        
        if (arena.isUseBlockTracking()) {
            regenerateUsingBlockTracking(arena);
        } else {
            regenerateUsingSchematic(arena);
        }
        
        // Clean up entities regardless of mode
        cleanupEntities(arena);
        
        arena.updateRegenTimestamp();
    }
    
    /**
     * Regenerates an arena using its schematic.
     * Must be called on the main thread.
     * 
     * @param arena Arena to regenerate
     */
    private void regenerateUsingSchematic(Arena arena) {
        long startTime = System.currentTimeMillis();
        
        boolean success = schematicHandler.restoreSchematic(arena);
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (success) {
            plugin.getLogger().info("[ ArenaRegeneration ] Restored schematic for " + 
                arena.getId() + " in " + duration + "ms");
        } else {
            plugin.getLogger().severe("[ ArenaRegeneration ] Failed to restore schematic for " + 
                arena.getId());
        }
    }
    
    /**
     * Regenerates an arena using block tracking.
     * Only restores blocks that were modified during the duel.
     * 
     * @param arena Arena to regenerate
     */
    private void regenerateUsingBlockTracking(Arena arena) {
        UUID duelId = arena.getCurrentDuelId();
        if (duelId == null) {
            plugin.getLogger().warning("[ ArenaRegeneration ] No duel ID for block tracking: " + arena.getId());
            return;
        }
        
        BlockTracker tracker = activeBlockTrackers.remove(duelId);
        if (tracker == null) {
            plugin.getLogger().warning("[ ArenaRegeneration ] No block tracker found for duel: " + duelId);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        int restoredBlocks = tracker.restoreBlocks();
        long duration = System.currentTimeMillis() - startTime;
        
        plugin.getLogger().info("[ ArenaRegeneration ] Restored " + restoredBlocks + 
            " blocks for " + arena.getId() + " in " + duration + "ms");
    }
    
    // ========================================
    // ENTITY CLEANUP
    // ========================================
    
    /**
     * Cleans up all entities within an arena.
     * Removes items, projectiles, TNT, and other debris.
     * 
     * @param arena Arena to clean
     */
    private void cleanupEntities(Arena arena) {
        Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
        Location corner2 = arena.getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return;
        }
        
        int removed = 0;
        
        // Get all entities in the arena region
        for (Entity entity : corner1.getWorld().getEntities()) {
            if (entity instanceof Player) {
                continue; // Never remove players
            }
            
            Location entityLoc = entity.getLocation();
            if (isLocationInArena(entityLoc, corner1, corner2)) {
                // Remove items, arrows, fireballs, TNT, etc.
                if (entity instanceof Item || 
                    entity.getType().name().contains("ARROW") ||
                    entity.getType().name().contains("FIREBALL") ||
                    entity.getType().name().contains("TNT") ||
                    entity.getType().name().contains("PROJECTILE")) {
                    entity.remove();
                    removed++;
                }
            }
        }
        
        if (removed > 0) {
            plugin.getLogger().info("[ ArenaRegeneration ] Removed " + removed + 
                " entities from " + arena.getId());
        }
    }
    
    /**
     * Checks if a location is within an arena's boundaries.
     * 
     * @param loc Location to check
     * @param corner1 First corner
     * @param corner2 Second corner
     * @return true if location is inside arena
     */
    private boolean isLocationInArena(Location loc, Location corner1, Location corner2) {
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
    
    // ========================================
    // BLOCK TRACKING
    // ========================================
    
    /**
     * Starts tracking block changes for a duel.
     * 
     * @param duelId Duel UUID
     * @param arena Arena where duel is taking place
     */
    public void startBlockTracking(UUID duelId, Arena arena) {
        if (arena.isUseBlockTracking()) {
            BlockTracker tracker = new BlockTracker(arena);
            activeBlockTrackers.put(duelId, tracker);
            
            plugin.getLogger().info("[ ArenaRegeneration ] Started block tracking for duel: " + duelId);
        }
    }
    
    /**
     * Records a block change during a duel.
     * 
     * @param duelId Duel UUID
     * @param block Block that was changed
     * @param originalMaterial Original material before change
     * @param originalBlockData Original block data before change
     */
    public void trackBlockChange(UUID duelId, Block block, Material originalMaterial, String originalBlockData) {
        BlockTracker tracker = activeBlockTrackers.get(duelId);
        if (tracker != null) {
            tracker.recordChange(block.getLocation(), originalMaterial, originalBlockData);
        }
    }
    
    /**
     * Stops tracking block changes for a duel.
     * 
     * @param duelId Duel UUID
     */
    public void stopBlockTracking(UUID duelId) {
        activeBlockTrackers.remove(duelId);
    }
    
    // ========================================
    // SCHEMATIC MANAGEMENT
    // ========================================
    
    /**
     * Captures a schematic for an arena.
     * Must be called on the main thread.
     * 
     * @param arena Arena to capture
     * @return true if capture was successful
     */
    public boolean captureSchematic(Arena arena) {
        return schematicHandler.captureSchematic(arena);
    }
    
    /**
     * Checks if a schematic exists for an arena.
     * 
     * @param arena Arena to check
     * @return true if schematic exists
     */
    public boolean hasSchematic(Arena arena) {
        return schematicHandler.schematicExists(arena);
    }
    
    /**
     * Deletes a schematic for an arena.
     * 
     * @param arena Arena whose schematic to delete
     * @return true if deleted successfully
     */
    public boolean deleteSchematic(Arena arena) {
        return schematicHandler.deleteSchematic(arena);
    }
    
    // ========================================
    // REGENERATION QUEUE
    // ========================================
    
    /**
     * Queues an arena for regeneration.
     * Useful for batch processing or delayed regeneration.
     * 
     * @param arena Arena to queue
     */
    public void queueRegeneration(Arena arena) {
        regenerationQueue.offer(new RegenerationTask(arena, System.currentTimeMillis()));
    }
    
    /**
     * Processes the regeneration queue.
     * Should be called periodically by a scheduler.
     */
    public void processQueue() {
        if (regenerationQueue.isEmpty()) {
            return;
        }
        
        // Process up to 5 arenas per tick to avoid lag
        int processed = 0;
        while (!regenerationQueue.isEmpty() && processed < 5) {
            RegenerationTask task = regenerationQueue.poll();
            if (task != null) {
                Bukkit.getScheduler().runTask(plugin, () -> regenerateArena(task.arena));
                processed++;
            }
        }
    }
    
    // ========================================
    // INNER CLASSES
    // ========================================
    
    /**
     * Tracks block changes within an arena during a duel.
     */
    private static class BlockTracker {
        private final Arena arena;
        private final Map<Location, BlockSnapshot> changedBlocks;
        
        public BlockTracker(Arena arena) {
            this.arena = arena;
            this.changedBlocks = new HashMap<>();
        }
        
        /**
         * Records a block change.
         * 
         * @param location Location of changed block
         * @param originalMaterial Original material
         * @param originalBlockData Original block data
         */
        public void recordChange(Location location, Material originalMaterial, String originalBlockData) {
            // Only record the first change to preserve original state
            changedBlocks.putIfAbsent(location, new BlockSnapshot(originalMaterial, originalBlockData));
        }
        
        /**
         * Restores all tracked blocks to their original state.
         * 
         * @return Number of blocks restored
         */
        public int restoreBlocks() {
            for (Map.Entry<Location, BlockSnapshot> entry : changedBlocks.entrySet()) {
                Location loc = entry.getKey();
                BlockSnapshot snapshot = entry.getValue();
                
                Block block = loc.getBlock();
                block.setType(snapshot.material, false);
                
                if (snapshot.blockData != null && !snapshot.blockData.isEmpty()) {
                    try {
                        org.bukkit.block.data.BlockData blockData = 
                            Bukkit.createBlockData(snapshot.blockData);
                        block.setBlockData(blockData, false);
                    } catch (Exception ignored) {
                        // Fallback to just material if block data fails
                    }
                }
            }
            
            int count = changedBlocks.size();
            changedBlocks.clear();
            return count;
        }
    }
    
    /**
     * Snapshot of a block's state.
     */
    private static class BlockSnapshot {
        final Material material;
        final String blockData;
        
        public BlockSnapshot(Material material, String blockData) {
            this.material = material;
            this.blockData = blockData;
        }
    }
    
    /**
     * Regeneration task for the queue.
     */
    private static class RegenerationTask {
        final Arena arena;
        final long queuedTime;
        
        public RegenerationTask(Arena arena, long queuedTime) {
            this.arena = arena;
            this.queuedTime = queuedTime;
        }
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    public ArenaSchematicHandler getSchematicHandler() {
        return schematicHandler;
    }
    
    public int getQueuedRegenerations() {
        return regenerationQueue.size();
    }
    
    public int getActiveTrackers() {
        return activeBlockTrackers.size();
    }
}
