package tech.xanthia.arena;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a duel arena with all its properties, locations, and state.
 * 
 * An arena is a fully contained battle zone where duels take place.
 * It includes boundary corners, spawn points, kit bindings, regeneration settings,
 * and runtime state tracking.
 * 
 * Thread Safety: Not thread-safe. Access should be synchronized by ArenaManager.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class Arena {
    
    // ========================================
    // BASIC ATTRIBUTES
    // ========================================
    
    private final String id;
    private String displayName;
    private boolean enabled;
    private String groupId; // nullable
    
    // ========================================
    // ARENA LOCATIONS
    // ========================================
    
    private final Map<ArenaLocationType, Location> locations;
    
    // ========================================
    // KIT BINDINGS
    // ========================================
    
    private final List<String> allowedKits;
    
    // ========================================
    // REGENERATION METADATA
    // ========================================
    
    private boolean regenerationEnabled;
    private String schematicName;
    private long lastRegenTimestamp;
    private boolean useBlockTracking; // false = schematic, true = block history
    
    // ========================================
    // STATE TRACKING (RUNTIME ONLY)
    // ========================================
    
    private boolean inUse;
    private UUID currentDuelId;
    private long cooldownUntil;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a new arena with the specified ID.
     * 
     * @param id Unique identifier for this arena
     */
    public Arena(String id) {
        this.id = id;
        this.displayName = id;
        this.enabled = true;
        this.groupId = null;
        this.locations = new EnumMap<>(ArenaLocationType.class);
        this.allowedKits = new ArrayList<>();
        this.regenerationEnabled = false;
        this.schematicName = id + "_schematic";
        this.lastRegenTimestamp = 0L;
        this.useBlockTracking = true;
        this.inUse = false;
        this.currentDuelId = null;
        this.cooldownUntil = 0L;
    }
    
    // ========================================
    // LOCATION METHODS
    // ========================================
    
    /**
     * Sets a location for the specified location type.
     * 
     * @param type Location type
     * @param location Location to set
     */
    public void setLocation(ArenaLocationType type, Location location) {
        if (location != null) {
            this.locations.put(type, location.clone());
        } else {
            this.locations.remove(type);
        }
    }
    
    /**
     * Gets a location for the specified location type.
     * 
     * @param type Location type
     * @return Location or null if not set
     */
    public Location getLocation(ArenaLocationType type) {
        Location loc = locations.get(type);
        return loc != null ? loc.clone() : null;
    }
    
    /**
     * Checks if a location type has been set.
     * 
     * @param type Location type
     * @return true if location is set
     */
    public boolean hasLocation(ArenaLocationType type) {
        return locations.containsKey(type);
    }
    
    /**
     * Gets all configured locations.
     * 
     * @return Immutable map of locations
     */
    public Map<ArenaLocationType, Location> getLocations() {
        Map<ArenaLocationType, Location> copy = new EnumMap<>(ArenaLocationType.class);
        locations.forEach((type, loc) -> copy.put(type, loc.clone()));
        return Collections.unmodifiableMap(copy);
    }
    
    /**
     * Checks if all required locations are set.
     * 
     * @return true if arena has all required locations
     */
    public boolean hasAllRequiredLocations() {
        for (ArenaLocationType type : ArenaLocationType.getRequiredLocations()) {
            if (!hasLocation(type)) {
                return false;
            }
        }
        return true;
    }
    
    // ========================================
    // KIT BINDING METHODS
    // ========================================
    
    /**
     * Adds an allowed kit to this arena.
     * 
     * @param kitId Kit ID to add
     */
    public void addAllowedKit(String kitId) {
        if (kitId != null && !allowedKits.contains(kitId)) {
            allowedKits.add(kitId);
        }
    }
    
    /**
     * Removes an allowed kit from this arena.
     * 
     * @param kitId Kit ID to remove
     */
    public void removeAllowedKit(String kitId) {
        allowedKits.remove(kitId);
    }
    
    /**
     * Checks if a kit is allowed in this arena.
     * 
     * @param kitId Kit ID to check
     * @return true if kit is allowed (or if no kits are specified, allowing all)
     */
    public boolean isKitAllowed(String kitId) {
        return allowedKits.isEmpty() || allowedKits.contains(kitId);
    }
    
    /**
     * Gets all allowed kits.
     * 
     * @return Immutable list of allowed kit IDs
     */
    public List<String> getAllowedKits() {
        return Collections.unmodifiableList(allowedKits);
    }
    
    /**
     * Clears all allowed kits.
     */
    public void clearAllowedKits() {
        allowedKits.clear();
    }
    
    // ========================================
    // STATE METHODS
    // ========================================
    
    /**
     * Marks this arena as in use by a duel.
     * 
     * @param duelId UUID of the duel using this arena
     */
    public void markInUse(UUID duelId) {
        this.inUse = true;
        this.currentDuelId = duelId;
    }
    
    /**
     * Marks this arena as no longer in use.
     */
    public void markNotInUse() {
        this.inUse = false;
        this.currentDuelId = null;
    }
    
    /**
     * Sets a cooldown period for this arena.
     * 
     * @param durationMillis Cooldown duration in milliseconds
     */
    public void setCooldown(long durationMillis) {
        this.cooldownUntil = System.currentTimeMillis() + durationMillis;
    }
    
    /**
     * Checks if this arena is currently on cooldown.
     * 
     * @return true if on cooldown
     */
    public boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownUntil;
    }
    
    /**
     * Checks if this arena is available for a duel.
     * 
     * @return true if enabled, not in use, and not on cooldown
     */
    public boolean isAvailable() {
        return enabled && !inUse && !isOnCooldown();
    }
    
    // ========================================
    // REGENERATION METHODS
    // ========================================
    
    /**
     * Updates the last regeneration timestamp to now.
     */
    public void updateRegenTimestamp() {
        this.lastRegenTimestamp = System.currentTimeMillis();
    }
    
    // ========================================
    // SERIALIZATION
    // ========================================
    
    /**
     * Serializes this arena to a ConfigurationSection.
     * 
     * @param section Configuration section to save to
     */
    public void serialize(ConfigurationSection section) {
        section.set("displayName", displayName);
        section.set("enabled", enabled);
        section.set("groupId", groupId);
        
        // Serialize locations
        ConfigurationSection locationsSection = section.createSection("locations");
        for (Map.Entry<ArenaLocationType, Location> entry : locations.entrySet()) {
            Location loc = entry.getValue();
            ConfigurationSection locSection = locationsSection.createSection(entry.getKey().getConfigKey());
            locSection.set("world", loc.getWorld().getName());
            locSection.set("x", loc.getX());
            locSection.set("y", loc.getY());
            locSection.set("z", loc.getZ());
            locSection.set("yaw", loc.getYaw());
            locSection.set("pitch", loc.getPitch());
        }
        
        // Serialize kits
        section.set("allowedKits", allowedKits.isEmpty() ? null : allowedKits);
        
        // Serialize regeneration settings
        section.set("regeneration.enabled", regenerationEnabled);
        section.set("regeneration.schematicName", schematicName);
        section.set("regeneration.useBlockTracking", useBlockTracking);
    }
    
    /**
     * Deserializes an arena from a ConfigurationSection.
     * 
     * @param id Arena ID
     * @param section Configuration section to load from
     * @return Deserialized Arena or null if invalid
     */
    public static Arena deserialize(String id, ConfigurationSection section) {
        if (section == null) return null;
        
        try {
            Arena arena = new Arena(id);
            
            arena.displayName = section.getString("displayName", id);
            arena.enabled = section.getBoolean("enabled", true);
            arena.groupId = section.getString("groupId");
            
            // Deserialize locations
            ConfigurationSection locationsSection = section.getConfigurationSection("locations");
            if (locationsSection != null) {
                for (ArenaLocationType type : ArenaLocationType.values()) {
                    ConfigurationSection locSection = locationsSection.getConfigurationSection(type.getConfigKey());
                    if (locSection != null) {
                        Location loc = deserializeLocation(locSection);
                        if (loc != null) {
                            arena.setLocation(type, loc);
                        }
                    }
                }
            }
            
            // Deserialize kits
            List<String> kits = section.getStringList("allowedKits");
            if (kits != null && !kits.isEmpty()) {
                arena.allowedKits.addAll(kits);
            }
            
            // Deserialize regeneration settings
            arena.regenerationEnabled = section.getBoolean("regeneration.enabled", false);
            arena.schematicName = section.getString("regeneration.schematicName", id + "_schematic");
            arena.useBlockTracking = section.getBoolean("regeneration.useBlockTracking", true);
            
            return arena;
            
        } catch (Exception e) {
            System.err.println("Failed to deserialize arena " + id + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Deserializes a location from a ConfigurationSection.
     * 
     * @param section Configuration section
     * @return Location or null if invalid
     */
    private static Location deserializeLocation(ConfigurationSection section) {
        try {
            String worldName = section.getString("world");
            if (worldName == null) return null;
            
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) return null;
            
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw", 0.0);
            float pitch = (float) section.getDouble("pitch", 0.0);
            
            return new Location(world, x, y, z, yaw, pitch);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    // ========================================
    // CLONING
    // ========================================
    
    /**
     * Creates a deep copy of this arena with a new ID.
     * Runtime state (inUse, duelId, cooldown) is not copied.
     * 
     * @param newId New arena ID
     * @param offset Location offset to apply to all coordinates
     * @return Cloned arena
     */
    public Arena clone(String newId, org.bukkit.util.Vector offset) {
        Arena clone = new Arena(newId);
        
        clone.displayName = this.displayName + " (Copy)";
        clone.enabled = this.enabled;
        clone.groupId = this.groupId;
        
        // Clone locations with offset
        for (Map.Entry<ArenaLocationType, Location> entry : this.locations.entrySet()) {
            Location originalLoc = entry.getValue();
            Location offsetLoc = originalLoc.clone().add(offset);
            clone.setLocation(entry.getKey(), offsetLoc);
        }
        
        // Clone kit bindings
        clone.allowedKits.addAll(this.allowedKits);
        
        // Clone regeneration settings
        clone.regenerationEnabled = this.regenerationEnabled;
        clone.schematicName = newId + "_schematic";
        clone.useBlockTracking = this.useBlockTracking;
        
        return clone;
    }
    
    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public boolean hasGroup() {
        return groupId != null && !groupId.isEmpty();
    }
    
    public boolean isRegenerationEnabled() {
        return regenerationEnabled;
    }
    
    public void setRegenerationEnabled(boolean regenerationEnabled) {
        this.regenerationEnabled = regenerationEnabled;
    }
    
    public String getSchematicName() {
        return schematicName;
    }
    
    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    
    public long getLastRegenTimestamp() {
        return lastRegenTimestamp;
    }
    
    public boolean isUseBlockTracking() {
        return useBlockTracking;
    }
    
    public void setUseBlockTracking(boolean useBlockTracking) {
        this.useBlockTracking = useBlockTracking;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    public UUID getCurrentDuelId() {
        return currentDuelId;
    }
    
    public long getCooldownUntil() {
        return cooldownUntil;
    }
    
    /**
     * Sets whether this arena is in use.
     * 
     * @param inUse True if in use
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
    
    /**
     * Sets the current duel ID for this arena.
     * 
     * @param duelId Duel UUID or null
     */
    public void setCurrentDuelId(UUID duelId) {
        this.currentDuelId = duelId;
    }
    
    /**
     * Sets the cooldown until timestamp.
     * 
     * @param timestamp Cooldown timestamp
     */
    public void setCooldownUntil(long timestamp) {
        this.cooldownUntil = timestamp;
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    @Override
    public String toString() {
        return "Arena{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", enabled=" + enabled +
                ", groupId='" + groupId + '\'' +
                ", inUse=" + inUse +
                ", locations=" + locations.size() +
                ", kits=" + allowedKits.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return id.equals(arena.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // ========================================
    // ADDITIONAL HELPER METHODS
    // ========================================
    
    /**
     * Gets the arena name (same as display name).
     * 
     * @return Arena name
     */
    public String getName() {
        return displayName;
    }
    
    /**
     * Gets the center location of the arena.
     * 
     * @return Center location or null
     */
    public Location getCenter() {
        // Try CENTER first, then calculate from corners
        Location center = getLocation(ArenaLocationType.CENTER);
        if (center != null) {
            return center;
        }
        
        Location corner1 = getLocation(ArenaLocationType.CORNER1);
        Location corner2 = getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return null;
        }
        
        double x = (corner1.getX() + corner2.getX()) / 2;
        double y = (corner1.getY() + corner2.getY()) / 2;
        double z = (corner1.getZ() + corner2.getZ()) / 2;
        
        return new Location(corner1.getWorld(), x, y, z);
    }
    
    /**
     * Gets the radius of the arena.
     * 
     * @return Radius in blocks
     */
    public double getRadius() {
        Location corner1 = getLocation(ArenaLocationType.CORNER1);
        Location corner2 = getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return 0;
        }
        
        double dx = Math.abs(corner2.getX() - corner1.getX());
        double dz = Math.abs(corner2.getZ() - corner1.getZ());
        
        return Math.max(dx, dz) / 2;
    }
    
    /**
     * Gets the bounding box of the arena.
     * 
     * @return Bounding box as [minX, minY, minZ, maxX, maxY, maxZ] or null
     */
    public double[] getBoundingBox() {
        Location corner1 = getLocation(ArenaLocationType.CORNER1);
        Location corner2 = getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return null;
        }
        
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        return new double[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
    
    /**
     * Gets spawn 1 location (RED_SPAWN).
     * 
     * @return Spawn 1 location or null
     */
    public Location getSpawn1() {
        return getLocation(ArenaLocationType.RED_SPAWN);
    }
    
    /**
     * Gets spawn 2 location (BLUE_SPAWN).
     * 
     * @return Spawn 2 location or null
     */
    public Location getSpawn2() {
        return getLocation(ArenaLocationType.BLUE_SPAWN);
    }
}
