package tech.xanthia.arena;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a group of arenas that share common properties.
 * 
 * Arena groups allow multiple arenas to share:
 * - Kit restrictions
 * - Display settings
 * - Regeneration mode
 * - Round rules
 * 
 * However, each arena in a group maintains its own locations and individual state.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaGroup {
    
    // ========================================
    // BASIC ATTRIBUTES
    // ========================================
    
    private final String groupId;
    private String displayName;
    private final Set<String> arenaIds;
    
    // ========================================
    // SHARED PROPERTIES
    // ========================================
    
    private final List<String> sharedKits;
    private Material displayItem;
    private boolean regenerationEnabled;
    private boolean useBlockTracking;
    private int roundCount;
    private boolean tournamentMode;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a new arena group.
     * 
     * @param groupId Unique identifier for this group
     */
    public ArenaGroup(String groupId) {
        this.groupId = groupId;
        this.displayName = groupId;
        this.arenaIds = new LinkedHashSet<>();
        this.sharedKits = new ArrayList<>();
        this.displayItem = Material.DIAMOND_SWORD;
        this.regenerationEnabled = true;
        this.useBlockTracking = true;
        this.roundCount = 1;
        this.tournamentMode = false;
    }
    
    // ========================================
    // ARENA MEMBERSHIP METHODS
    // ========================================
    
    /**
     * Adds an arena to this group.
     * 
     * @param arenaId Arena ID to add
     * @return true if arena was added (wasn't already present)
     */
    public boolean addArena(String arenaId) {
        if (arenaId == null || arenaId.isEmpty()) {
            return false;
        }
        return arenaIds.add(arenaId);
    }
    
    /**
     * Removes an arena from this group.
     * 
     * @param arenaId Arena ID to remove
     * @return true if arena was removed
     */
    public boolean removeArena(String arenaId) {
        return arenaIds.remove(arenaId);
    }
    
    /**
     * Checks if an arena is part of this group.
     * 
     * @param arenaId Arena ID to check
     * @return true if arena is in this group
     */
    public boolean containsArena(String arenaId) {
        return arenaIds.contains(arenaId);
    }
    
    /**
     * Gets all arena IDs in this group.
     * 
     * @return Immutable set of arena IDs
     */
    public Set<String> getArenaIds() {
        return Collections.unmodifiableSet(arenaIds);
    }
    
    /**
     * Gets the number of arenas in this group.
     * 
     * @return Arena count
     */
    public int getArenaCount() {
        return arenaIds.size();
    }
    
    /**
     * Checks if this group has any arenas.
     * 
     * @return true if group is empty
     */
    public boolean isEmpty() {
        return arenaIds.isEmpty();
    }
    
    // ========================================
    // SHARED KIT METHODS
    // ========================================
    
    /**
     * Adds a kit to the shared kit list.
     * 
     * @param kitId Kit ID to add
     */
    public void addSharedKit(String kitId) {
        if (kitId != null && !sharedKits.contains(kitId)) {
            sharedKits.add(kitId);
        }
    }
    
    /**
     * Removes a kit from the shared kit list.
     * 
     * @param kitId Kit ID to remove
     */
    public void removeSharedKit(String kitId) {
        sharedKits.remove(kitId);
    }
    
    /**
     * Checks if a kit is allowed in this group.
     * 
     * @param kitId Kit ID to check
     * @return true if kit is allowed (or if no kits specified, allowing all)
     */
    public boolean isKitAllowed(String kitId) {
        return sharedKits.isEmpty() || sharedKits.contains(kitId);
    }
    
    /**
     * Gets all shared kits for this group.
     * 
     * @return Immutable list of kit IDs
     */
    public List<String> getSharedKits() {
        return Collections.unmodifiableList(sharedKits);
    }
    
    /**
     * Clears all shared kits.
     */
    public void clearSharedKits() {
        sharedKits.clear();
    }
    
    // ========================================
    // SERIALIZATION
    // ========================================
    
    /**
     * Serializes this group to a ConfigurationSection.
     * 
     * @param section Configuration section to save to
     */
    public void serialize(ConfigurationSection section) {
        section.set("displayName", displayName);
        section.set("arenas", new ArrayList<>(arenaIds));
        section.set("sharedKits", sharedKits.isEmpty() ? null : sharedKits);
        section.set("displayItem", displayItem.name());
        section.set("regeneration.enabled", regenerationEnabled);
        section.set("regeneration.useBlockTracking", useBlockTracking);
        section.set("settings.roundCount", roundCount);
        section.set("settings.tournamentMode", tournamentMode);
    }
    
    /**
     * Deserializes an arena group from a ConfigurationSection.
     * 
     * @param groupId Group ID
     * @param section Configuration section to load from
     * @return Deserialized ArenaGroup or null if invalid
     */
    public static ArenaGroup deserialize(String groupId, ConfigurationSection section) {
        if (section == null) return null;
        
        try {
            ArenaGroup group = new ArenaGroup(groupId);
            
            group.displayName = section.getString("displayName", groupId);
            
            // Deserialize arena IDs
            List<String> arenas = section.getStringList("arenas");
            if (arenas != null) {
                group.arenaIds.addAll(arenas);
            }
            
            // Deserialize shared kits
            List<String> kits = section.getStringList("sharedKits");
            if (kits != null && !kits.isEmpty()) {
                group.sharedKits.addAll(kits);
            }
            
            // Deserialize display item
            String itemName = section.getString("displayItem", "DIAMOND_SWORD");
            try {
                group.displayItem = Material.valueOf(itemName);
            } catch (IllegalArgumentException e) {
                group.displayItem = Material.DIAMOND_SWORD;
            }
            
            // Deserialize regeneration settings
            group.regenerationEnabled = section.getBoolean("regeneration.enabled", true);
            group.useBlockTracking = section.getBoolean("regeneration.useBlockTracking", true);
            
            // Deserialize settings
            group.roundCount = section.getInt("settings.roundCount", 1);
            group.tournamentMode = section.getBoolean("settings.tournamentMode", false);
            
            return group;
            
        } catch (Exception e) {
            System.err.println("Failed to deserialize arena group " + groupId + ": " + e.getMessage());
            return null;
        }
    }
    
    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public String getGroupId() {
        return groupId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Material getDisplayItem() {
        return displayItem;
    }
    
    public void setDisplayItem(Material displayItem) {
        this.displayItem = displayItem;
    }
    
    public boolean isRegenerationEnabled() {
        return regenerationEnabled;
    }
    
    public void setRegenerationEnabled(boolean regenerationEnabled) {
        this.regenerationEnabled = regenerationEnabled;
    }
    
    public boolean isUseBlockTracking() {
        return useBlockTracking;
    }
    
    public void setUseBlockTracking(boolean useBlockTracking) {
        this.useBlockTracking = useBlockTracking;
    }
    
    public int getRoundCount() {
        return roundCount;
    }
    
    public void setRoundCount(int roundCount) {
        this.roundCount = Math.max(1, roundCount);
    }
    
    public boolean isTournamentMode() {
        return tournamentMode;
    }
    
    public void setTournamentMode(boolean tournamentMode) {
        this.tournamentMode = tournamentMode;
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    @Override
    public String toString() {
        return "ArenaGroup{" +
                "groupId='" + groupId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", arenaCount=" + arenaIds.size() +
                ", sharedKits=" + sharedKits.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArenaGroup that = (ArenaGroup) o;
        return groupId.equals(that.groupId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}
