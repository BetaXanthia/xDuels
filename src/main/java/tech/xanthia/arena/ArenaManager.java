package tech.xanthia.arena;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Central manager for all arena operations.
 * 
 * Responsibilities:
 * - Loading and caching arenas and groups
 * - Arena CRUD operations (create, read, update, delete)
 * - Arena selection logic for matchmaking
 * - Arena state management (in-use tracking, cooldowns)
 * - Group management
 * - Integration with regeneration and validation systems
 * 
 * Thread Safety: This class uses ConcurrentHashMap for thread-safe operations.
 * However, individual arena modifications should be synchronized externally.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaManager {
    
    private final XDuels plugin;
    private final ArenaStorage storage;
    private final ArenaValidator validator;
    private final ArenaRegenerationManager regenerationManager;
    
    // ========================================
    // CACHES
    // ========================================
    
    private final Map<String, Arena> arenas;
    private final Map<String, ArenaGroup> groups;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a new ArenaManager.
     * 
     * @param plugin Plugin instance
     * @param regenerationManager Regeneration manager for arena resets
     */
    public ArenaManager(XDuels plugin, ArenaRegenerationManager regenerationManager) {
        this.plugin = plugin;
        this.storage = new ArenaStorage(plugin);
        this.validator = new ArenaValidator(plugin);
        this.regenerationManager = regenerationManager;
        this.arenas = new ConcurrentHashMap<>();
        this.groups = new ConcurrentHashMap<>();
    }
    
    // ========================================
    // INITIALIZATION
    // ========================================
    
    /**
     * Initializes the arena manager by loading all arenas and groups from storage.
     */
    public void initialize() {
        storage.initialize();
        loadAll();
    }
    
    /**
     * Loads all arenas and groups from storage.
     */
    private void loadAll() {
        // Load arenas
        Map<String, Arena> loadedArenas = storage.loadAllArenas();
        arenas.putAll(loadedArenas);
        
        // Load groups
        Map<String, ArenaGroup> loadedGroups = storage.loadAllGroups();
        groups.putAll(loadedGroups);
        
        // Validate loaded data
        linkGroupRelationships();
        
        plugin.getLogger().info("[ ArenaManager ] Loaded " + arenas.size() + 
            " arenas and " + groups.size() + " groups");
    }
    
    /**
     * Links arenas to their groups and ensures bidirectional consistency.
     */
    private void linkGroupRelationships() {
        // Ensure all arena group IDs exist
        for (Arena arena : arenas.values()) {
            if (arena.hasGroup() && !groups.containsKey(arena.getGroupId())) {
                plugin.getLogger().warning("[ ArenaManager ] Arena " + arena.getId() + 
                    " references non-existent group " + arena.getGroupId());
                arena.setGroupId(null);
            }
        }
        
        // Ensure all group arenas exist
        for (ArenaGroup group : groups.values()) {
            Set<String> invalidArenas = new HashSet<>();
            for (String arenaId : group.getArenaIds()) {
                if (!arenas.containsKey(arenaId)) {
                    invalidArenas.add(arenaId);
                }
            }
            invalidArenas.forEach(group::removeArena);
            
            if (!invalidArenas.isEmpty()) {
                plugin.getLogger().warning("[ ArenaManager ] Group " + group.getGroupId() + 
                    " contained " + invalidArenas.size() + " invalid arena references");
            }
        }
    }
    
    /**
     * Warms the cache by preloading all arena data.
     * Called during plugin startup.
     */
    public void warmCache() {
        int validArenas = 0;
        int invalidArenas = 0;
        int disabledArenas = 0;
        
        for (Arena arena : arenas.values()) {
            if (!arena.isEnabled()) {
                disabledArenas++;
                continue;
            }
            
            if (validator.isArenaValid(arena)) {
                validArenas++;
            } else {
                invalidArenas++;
                arena.setEnabled(false); // Auto-disable invalid arenas
            }
        }
        
        plugin.getLogger().info("[ ArenaManager ] Cache warmed: " + validArenas + 
            " valid, " + invalidArenas + " invalid (disabled), " + disabledArenas + " manually disabled");
    }
    
    // ========================================
    // ARENA RETRIEVAL
    // ========================================
    
    /**
     * Gets an arena by ID.
     * 
     * @param id Arena ID
     * @return Arena or null if not found
     */
    public Arena getArena(String id) {
        return arenas.get(id);
    }
    
    /**
     * Gets all arenas.
     * 
     * @return Immutable collection of all arenas
     */
    public Collection<Arena> getAllArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }
    
    /**
     * Gets all enabled arenas.
     * 
     * @return List of enabled arenas
     */
    public List<Arena> getEnabledArenas() {
        return arenas.values().stream()
            .filter(Arena::isEnabled)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all available (enabled, not in use, not on cooldown) arenas.
     * 
     * @return List of available arenas
     */
    public List<Arena> getAvailableArenas() {
        return arenas.values().stream()
            .filter(Arena::isAvailable)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets arenas that allow a specific kit.
     * 
     * @param kitId Kit ID
     * @return List of arenas that allow this kit
     */
    public List<Arena> getArenasForKit(String kitId) {
        return arenas.values().stream()
            .filter(arena -> arena.isAvailable() && arena.isKitAllowed(kitId))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets arenas in a specific group.
     * 
     * @param groupId Group ID
     * @return List of arenas in this group
     */
    public List<Arena> getArenasByGroup(String groupId) {
        return arenas.values().stream()
            .filter(arena -> groupId.equals(arena.getGroupId()))
            .collect(Collectors.toList());
    }
    
    // ========================================
    // ARENA SELECTION LOGIC
    // ========================================
    
    /**
     * Gets a random free arena for the specified kit.
     * 
     * @param kitId Kit ID
     * @return Random available arena or null if none available
     */
    public Arena getFreeArena(String kitId) {
        List<Arena> suitable = getArenasForKit(kitId);
        
        if (suitable.isEmpty()) {
            return null;
        }
        
        // Prefer arenas in groups
        List<Arena> groupedArenas = suitable.stream()
            .filter(Arena::hasGroup)
            .collect(Collectors.toList());
        
        if (!groupedArenas.isEmpty()) {
            return groupedArenas.get(ThreadLocalRandom.current().nextInt(groupedArenas.size()));
        }
        
        // Fall back to standalone arenas
        return suitable.get(ThreadLocalRandom.current().nextInt(suitable.size()));
    }
    
    /**
     * Gets a random free arena within a specific group.
     * 
     * @param groupId Group ID
     * @return Random available arena from group or null if none available
     */
    public Arena getFreeArenaInGroup(String groupId) {
        List<Arena> groupArenas = getArenasByGroup(groupId).stream()
            .filter(Arena::isAvailable)
            .collect(Collectors.toList());
        
        if (groupArenas.isEmpty()) {
            return null;
        }
        
        return groupArenas.get(ThreadLocalRandom.current().nextInt(groupArenas.size()));
    }
    
    /**
     * Gets a random arena for any kit (best effort).
     * 
     * @return Random available arena or null if none available
     */
    public Arena getRandomArenaForKit() {
        List<Arena> available = getAvailableArenas();
        
        if (available.isEmpty()) {
            return null;
        }
        
        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }
    
    // ========================================
    // ARENA STATE MANAGEMENT
    // ========================================
    
    /**
     * Checks if an arena is currently in use.
     * 
     * @param arenaId Arena ID
     * @return true if arena is in use
     */
    public boolean isArenaInUse(String arenaId) {
        Arena arena = getArena(arenaId);
        return arena != null && arena.isInUse();
    }
    
    /**
     * Marks an arena as in use by a duel.
     * 
     * @param arenaId Arena ID
     * @param duelId Duel UUID
     * @return true if successfully marked
     */
    public boolean markArenaInUse(String arenaId, UUID duelId) {
        Arena arena = getArena(arenaId);
        if (arena == null || arena.isInUse()) {
            return false;
        }
        
        arena.markInUse(duelId);
        return true;
    }
    
    /**
     * Marks an arena as no longer in use.
     * 
     * @param arenaId Arena ID
     */
    public void unmarkArenaInUse(String arenaId) {
        Arena arena = getArena(arenaId);
        if (arena != null) {
            arena.markNotInUse();
            
            // Apply cooldown if configured
            int cooldownSeconds = plugin.getConfig().getInt("arena.cooldown-seconds", 5);
            if (cooldownSeconds > 0) {
                arena.setCooldown(cooldownSeconds * 1000L);
            }
        }
    }
    
    /**
     * Clears all arena usage states.
     * Useful during reload or emergency situations.
     */
    public void clearAllUsageStates() {
        arenas.values().forEach(Arena::markNotInUse);
        plugin.getLogger().info("[ ArenaManager ] Cleared all arena usage states");
    }
    
    // ========================================
    // ARENA CRUD OPERATIONS
    // ========================================
    
    /**
     * Creates a new arena.
     * 
     * @param id Unique arena ID
     * @param displayName Display name
     * @return Created arena or null if ID already exists
     */
    public Arena createArena(String id, String displayName) {
        if (arenas.containsKey(id)) {
            return null;
        }
        
        Arena arena = new Arena(id);
        arena.setDisplayName(displayName);
        arenas.put(id, arena);
        
        storage.saveArenaAsync(arena);
        
        plugin.getLogger().info("[ ArenaManager ] Created arena: " + id);
        return arena;
    }
    
    /**
     * Deletes an arena.
     * 
     * @param arenaId Arena ID
     * @return true if deleted, false if not found or in use
     */
    public boolean deleteArena(String arenaId) {
        Arena arena = getArena(arenaId);
        if (arena == null) {
            return false;
        }
        
        if (arena.isInUse()) {
            plugin.getLogger().warning("[ ArenaManager ] Cannot delete arena " + arenaId + " - currently in use");
            return false;
        }
        
        // Remove from group if applicable
        if (arena.hasGroup()) {
            ArenaGroup group = getGroup(arena.getGroupId());
            if (group != null) {
                group.removeArena(arenaId);
                storage.saveGroupAsync(group);
            }
        }
        
        arenas.remove(arenaId);
        storage.deleteArenaAsync(arenaId);
        
        plugin.getLogger().info("[ ArenaManager ] Deleted arena: " + arenaId);
        return true;
    }
    
    /**
     * Saves a specific arena to storage.
     * 
     * @param arena Arena to save
     */
    public void saveArena(Arena arena) {
        if (arena != null) {
            storage.saveArenaAsync(arena);
        }
    }
    
    /**
     * Saves all arenas to storage.
     */
    public void saveAllArenas() {
        storage.saveAllArenasAsync(new HashMap<>(arenas));
    }
    
    /**
     * Clones an arena with a new ID and optional position offset.
     * 
     * @param sourceId Source arena ID
     * @param newId New arena ID
     * @param offset Position offset (or null for no offset)
     * @return Cloned arena or null if source doesn't exist or newId already exists
     */
    public Arena cloneArena(String sourceId, String newId, Vector offset) {
        Arena source = getArena(sourceId);
        if (source == null || arenas.containsKey(newId)) {
            return null;
        }
        
        Vector actualOffset = offset != null ? offset : new Vector(0, 0, 0);
        Arena clone = source.clone(newId, actualOffset);
        
        arenas.put(newId, clone);
        storage.saveArenaAsync(clone);
        
        plugin.getLogger().info("[ ArenaManager ] Cloned arena " + sourceId + " to " + newId);
        return clone;
    }
    
    // ========================================
    // GROUP CRUD OPERATIONS
    // ========================================
    
    /**
     * Creates a new arena group.
     * 
     * @param groupId Unique group ID
     * @param displayName Display name
     * @return Created group or null if ID already exists
     */
    public ArenaGroup createGroup(String groupId, String displayName) {
        if (groups.containsKey(groupId)) {
            return null;
        }
        
        ArenaGroup group = new ArenaGroup(groupId);
        group.setDisplayName(displayName);
        groups.put(groupId, group);
        
        storage.saveGroupAsync(group);
        
        plugin.getLogger().info("[ ArenaManager ] Created group: " + groupId);
        return group;
    }
    
    /**
     * Gets a group by ID.
     * 
     * @param groupId Group ID
     * @return Group or null if not found
     */
    public ArenaGroup getGroup(String groupId) {
        return groups.get(groupId);
    }
    
    /**
     * Gets all groups.
     * 
     * @return Immutable collection of all groups
     */
    public Collection<ArenaGroup> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }
    
    /**
     * Deletes an arena group.
     * Also removes group association from all arenas in the group.
     * 
     * @param groupId Group ID
     * @return true if deleted, false if not found
     */
    public boolean deleteGroup(String groupId) {
        ArenaGroup group = getGroup(groupId);
        if (group == null) {
            return false;
        }
        
        // Remove group from all arenas
        for (String arenaId : group.getArenaIds()) {
            Arena arena = getArena(arenaId);
            if (arena != null) {
                arena.setGroupId(null);
                storage.saveArenaAsync(arena);
            }
        }
        
        groups.remove(groupId);
        storage.deleteGroupAsync(groupId);
        
        plugin.getLogger().info("[ ArenaManager ] Deleted group: " + groupId);
        return true;
    }
    
    /**
     * Saves a specific group to storage.
     * 
     * @param group Group to save
     */
    public void saveGroup(ArenaGroup group) {
        if (group != null) {
            storage.saveGroupAsync(group);
        }
    }
    
    /**
     * Saves all groups to storage.
     */
    public void saveAllGroups() {
        storage.saveAllGroupsAsync(new HashMap<>(groups));
    }
    
    /**
     * Assigns an arena to a group.
     * 
     * @param arenaId Arena ID
     * @param groupId Group ID
     * @return true if successfully assigned
     */
    public boolean assignArenaToGroup(String arenaId, String groupId) {
        Arena arena = getArena(arenaId);
        ArenaGroup group = getGroup(groupId);
        
        if (arena == null || group == null) {
            return false;
        }
        
        // Remove from previous group if applicable
        if (arena.hasGroup()) {
            ArenaGroup oldGroup = getGroup(arena.getGroupId());
            if (oldGroup != null) {
                oldGroup.removeArena(arenaId);
                storage.saveGroupAsync(oldGroup);
            }
        }
        
        // Assign to new group
        arena.setGroupId(groupId);
        group.addArena(arenaId);
        
        storage.saveArenaAsync(arena);
        storage.saveGroupAsync(group);
        
        plugin.getLogger().info("[ ArenaManager ] Assigned arena " + arenaId + " to group " + groupId);
        return true;
    }
    
    /**
     * Removes an arena from its group.
     * 
     * @param arenaId Arena ID
     * @return true if removed from a group
     */
    public boolean removeArenaFromGroup(String arenaId) {
        Arena arena = getArena(arenaId);
        if (arena == null || !arena.hasGroup()) {
            return false;
        }
        
        ArenaGroup group = getGroup(arena.getGroupId());
        if (group != null) {
            group.removeArena(arenaId);
            storage.saveGroupAsync(group);
        }
        
        arena.setGroupId(null);
        storage.saveArenaAsync(arena);
        
        plugin.getLogger().info("[ ArenaManager ] Removed arena " + arenaId + " from its group");
        return true;
    }
    
    // ========================================
    // VALIDATION
    // ========================================
    
    /**
     * Validates all arenas and returns a comprehensive report.
     * 
     * @return Validation result
     */
    public ArenaValidationResult validateAllArenas() {
        return validator.validateAll(arenas.values());
    }
    
    /**
     * Validates a specific arena.
     * 
     * @param arenaId Arena ID
     * @return true if valid
     */
    public boolean isArenaValid(String arenaId) {
        Arena arena = getArena(arenaId);
        return arena != null && validator.isArenaValid(arena);
    }
    
    // ========================================
    // STALE ARENA DETECTION
    // ========================================
    
    /**
     * Detects and resets arenas that have been in use for too long.
     * Called periodically by a scheduler.
     */
    public void detectAndResetStaleArenas() {
        long staleThreshold = System.currentTimeMillis() - (30 * 60 * 1000); // 30 minutes
        
        for (Arena arena : arenas.values()) {
            if (arena.isInUse() && arena.getLastRegenTimestamp() < staleThreshold) {
                plugin.getLogger().warning("[ ArenaManager ] Detected stale arena: " + arena.getId());
                unmarkArenaInUse(arena.getId());
                regenerationManager.regenerateArena(arena);
            }
        }
    }
    
    // ========================================
    // RELOAD
    // ========================================
    
    /**
     * Reloads all arenas and groups from storage.
     * Preserves runtime state (in-use status).
     */
    public void reload() {
        // Save current in-use states
        Map<String, Boolean> inUseStates = new HashMap<>();
        Map<String, UUID> duelIds = new HashMap<>();
        for (Arena arena : arenas.values()) {
            if (arena.isInUse()) {
                inUseStates.put(arena.getId(), true);
                duelIds.put(arena.getId(), arena.getCurrentDuelId());
            }
        }
        
        // Clear and reload
        arenas.clear();
        groups.clear();
        storage.reload();
        loadAll();
        
        // Restore in-use states
        for (Map.Entry<String, Boolean> entry : inUseStates.entrySet()) {
            Arena arena = getArena(entry.getKey());
            if (arena != null) {
                UUID duelId = duelIds.get(entry.getKey());
                if (duelId != null) {
                    arena.markInUse(duelId);
                }
            }
        }
        
        plugin.getLogger().info("[ ArenaManager ] Reloaded " + arenas.size() + 
            " arenas and " + groups.size() + " groups");
    }
    
    // ========================================
    // ========================================
    // SHUTDOWN
    // ========================================
    
    /**
     * Shuts down the arena manager.
     */
    public void shutdown() {
        plugin.getLogger().info("[ArenaManager] Shutting down...");
        
        // Release all in-use arenas
        for (Arena arena : arenas.values()) {
            if (arena.isInUse()) {
                arena.setInUse(false);
                arena.setCurrentDuelId(null);
            }
        }
        
        // Save all arenas
        storage.saveAllArenas(arenas);
        storage.saveAllGroups(groups);
        
        plugin.getLogger().info("[ArenaManager] Shutdown complete");
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    public ArenaStorage getStorage() {
        return storage;
    }
    
    public ArenaValidator getValidator() {
        return validator;
    }
    
    public ArenaRegenerationManager getRegenerationManager() {
        return regenerationManager;
    }
    
    public int getArenaCount() {
        return arenas.size();
    }
    
    public int getGroupCount() {
        return groups.size();
    }
    
    public int getAvailableArenaCount() {
        return (int) arenas.values().stream().filter(Arena::isAvailable).count();
    }
    
    // ========================================
    // ARENA RESERVATION METHODS
    // ========================================
    
    /**
     * Reserves an arena for a duel with the specified kit.
     * Finds the first available arena that allows the kit.
     * 
     * @param kitName Kit name for the duel
     * @return Reserved arena or null if none available
     */
    public Arena reserveArena(String kitName) {
        for (Arena arena : arenas.values()) {
            if (arena.isAvailable() && arena.isKitAllowed(kitName)) {
                arena.setInUse(true);
                return arena;
            }
        }
        return null;
    }
    
    /**
     * Releases an arena back to the pool after a duel.
     * 
     * @param arena Arena to release
     */
    public void releaseArena(Arena arena) {
        if (arena != null) {
            arena.setInUse(false);
            arena.setCurrentDuelId(null);
            arena.setCooldownUntil(System.currentTimeMillis());
        }
    }
}
