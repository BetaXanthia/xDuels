package tech.xanthia.arena;

/**
 * Enum representing all location types that can be defined in an arena.
 * Used for editor commands, GUIs, teleportation, and validation.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ArenaLocationType {
    
    /**
     * The center point of the arena (used for party teleports)
     */
    CENTER("Center", "center"),
    
    /**
     * First corner of the arena cuboid boundary
     */
    CORNER1("Corner 1", "corner1"),
    
    /**
     * Second corner of the arena cuboid boundary (opposite to CORNER1)
     */
    CORNER2("Corner 2", "corner2"),
    
    /**
     * Red team spawn location (solo duels or team duels)
     */
    RED_SPAWN("Red Spawn", "redspawn"),
    
    /**
     * Blue team spawn location (solo duels or team duels)
     */
    BLUE_SPAWN("Blue Spawn", "bluespawn"),
    
    /**
     * Party center point (for party vs party duels)
     */
    PARTY_CENTER("Party Center", "partycenter");
    
    private final String displayName;
    private final String configKey;
    
    /**
     * Constructs an ArenaLocationType.
     * 
     * @param displayName Human-readable name for GUIs and messages
     * @param configKey YAML configuration key name
     */
    ArenaLocationType(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }
    
    /**
     * Gets the display name for this location type.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the configuration key for YAML storage.
     * 
     * @return Config key
     */
    public String getConfigKey() {
        return configKey;
    }
    
    /**
     * Parses a string into an ArenaLocationType.
     * Case-insensitive and matches both display names and config keys.
     * 
     * @param input String to parse
     * @return Matching ArenaLocationType or null if not found
     */
    public static ArenaLocationType fromString(String input) {
        if (input == null) return null;
        
        String normalized = input.toLowerCase().replace(" ", "");
        
        for (ArenaLocationType type : values()) {
            if (type.name().equalsIgnoreCase(input) ||
                type.configKey.equalsIgnoreCase(input) ||
                type.displayName.replace(" ", "").equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if this location type is a spawn point.
     * 
     * @return true if this is RED_SPAWN or BLUE_SPAWN
     */
    public boolean isSpawnPoint() {
        return this == RED_SPAWN || this == BLUE_SPAWN;
    }
    
    /**
     * Checks if this location type is a corner point.
     * 
     * @return true if this is CORNER1 or CORNER2
     */
    public boolean isCorner() {
        return this == CORNER1 || this == CORNER2;
    }
    
    /**
     * Gets all location types that are required for a valid arena.
     * 
     * @return Array of required location types
     */
    public static ArenaLocationType[] getRequiredLocations() {
        return new ArenaLocationType[]{
            CENTER, CORNER1, CORNER2, RED_SPAWN, BLUE_SPAWN
        };
    }
}
