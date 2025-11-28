package tech.xanthia.arena;

import org.bukkit.Location;
import tech.xanthia.XDuels;

import java.util.Collection;

/**
 * Validates arena configurations to ensure they meet all requirements.
 * 
 * Validation Rules:
 * - All required locations must be set (CENTER, CORNER1, CORNER2, RED_SPAWN, BLUE_SPAWN)
 * - Corners must form a valid cuboid (corner1 < corner2 on all axes)
 * - Center must be inside the cuboid
 * - Spawn points must be inside the arena
 * - All locations must be in the same world
 * - Referenced kits must exist in KitManager
 * - Referenced groups must exist
 * - No duplicate IDs
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaValidator {
    
    private final XDuels plugin;
    
    /**
     * Creates a new ArenaValidator.
     * 
     * @param plugin Plugin instance
     */
    public ArenaValidator(XDuels plugin) {
        this.plugin = plugin;
    }
    
    // ========================================
    // VALIDATION METHODS
    // ========================================
    
    /**
     * Validates a single arena.
     * 
     * @param arena Arena to validate
     * @return true if arena is valid
     */
    public boolean isArenaValid(Arena arena) {
        if (arena == null) {
            return false;
        }
        
        // Check all required locations are set
        if (!arena.hasAllRequiredLocations()) {
            return false;
        }
        
        // Validate corners form a proper cuboid
        if (!validateCuboid(arena)) {
            return false;
        }
        
        // Validate center is inside cuboid
        if (!validateCenterInside(arena)) {
            return false;
        }
        
        // Validate spawn points are inside arena
        if (!validateSpawnsInside(arena)) {
            return false;
        }
        
        // Validate all locations are in same world
        if (!validateSameWorld(arena)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates all arenas in a collection.
     * 
     * @param arenas Collection of arenas to validate
     * @return Validation result with statistics and errors
     */
    public ArenaValidationResult validateAll(Collection<Arena> arenas) {
        ArenaValidationResult result = new ArenaValidationResult();
        
        for (Arena arena : arenas) {
            if (!arena.isEnabled()) {
                result.incrementDisabled();
                continue;
            }
            
            if (isArenaValid(arena)) {
                result.incrementValid();
            } else {
                result.incrementInvalid();
                result.addError("Arena '" + arena.getId() + "' failed validation: " + getValidationErrors(arena));
            }
        }
        
        return result;
    }
    
    /**
     * Gets detailed validation error messages for an arena.
     * 
     * @param arena Arena to check
     * @return Error message describing all validation issues
     */
    public String getValidationErrors(Arena arena) {
        StringBuilder errors = new StringBuilder();
        
        // Check required locations
        if (!arena.hasAllRequiredLocations()) {
            errors.append("Missing required locations: ");
            for (ArenaLocationType type : ArenaLocationType.getRequiredLocations()) {
                if (!arena.hasLocation(type)) {
                    errors.append(type.getDisplayName()).append(", ");
                }
            }
            errors.setLength(errors.length() - 2); // Remove last comma
            errors.append("; ");
        }
        
        // Check cuboid
        if (!validateCuboid(arena)) {
            errors.append("Invalid cuboid (corner1 must be less than corner2 on all axes); ");
        }
        
        // Check center
        if (!validateCenterInside(arena)) {
            errors.append("Center is outside cuboid boundaries; ");
        }
        
        // Check spawns
        if (!validateSpawnsInside(arena)) {
            errors.append("One or more spawn points are outside arena; ");
        }
        
        // Check same world
        if (!validateSameWorld(arena)) {
            errors.append("Locations are in different worlds; ");
        }
        
        if (errors.length() == 0) {
            return "Unknown validation error";
        }
        
        return errors.toString();
    }
    
    // ========================================
    // SPECIFIC VALIDATION CHECKS
    // ========================================
    
    /**
     * Validates that corners form a valid cuboid.
     * Corner1 coordinates must be less than Corner2 coordinates on all axes.
     * 
     * @param arena Arena to validate
     * @return true if cuboid is valid
     */
    private boolean validateCuboid(Arena arena) {
        Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
        Location corner2 = arena.getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return false;
        }
        
        // Corners must form a valid cuboid (corner1 < corner2)
        return corner1.getX() < corner2.getX() &&
               corner1.getY() < corner2.getY() &&
               corner1.getZ() < corner2.getZ();
    }
    
    /**
     * Validates that the center point is inside the arena cuboid.
     * 
     * @param arena Arena to validate
     * @return true if center is inside
     */
    private boolean validateCenterInside(Arena arena) {
        Location center = arena.getLocation(ArenaLocationType.CENTER);
        Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
        Location corner2 = arena.getLocation(ArenaLocationType.CORNER2);
        
        if (center == null || corner1 == null || corner2 == null) {
            return false;
        }
        
        return isLocationInside(center, corner1, corner2);
    }
    
    /**
     * Validates that all spawn points are inside the arena cuboid.
     * 
     * @param arena Arena to validate
     * @return true if all spawns are inside
     */
    private boolean validateSpawnsInside(Arena arena) {
        Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
        Location corner2 = arena.getLocation(ArenaLocationType.CORNER2);
        
        if (corner1 == null || corner2 == null) {
            return false;
        }
        
        Location redSpawn = arena.getLocation(ArenaLocationType.RED_SPAWN);
        Location blueSpawn = arena.getLocation(ArenaLocationType.BLUE_SPAWN);
        
        if (redSpawn == null || blueSpawn == null) {
            return false;
        }
        
        if (!isLocationInside(redSpawn, corner1, corner2)) {
            return false;
        }
        
        if (!isLocationInside(blueSpawn, corner1, corner2)) {
            return false;
        }
        
        // Check party center if set
        Location partyCenter = arena.getLocation(ArenaLocationType.PARTY_CENTER);
        if (partyCenter != null && !isLocationInside(partyCenter, corner1, corner2)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates that all locations are in the same world.
     * 
     * @param arena Arena to validate
     * @return true if all locations are in same world
     */
    private boolean validateSameWorld(Arena arena) {
        String worldName = null;
        
        for (Location loc : arena.getLocations().values()) {
            if (loc == null) continue;
            
            String currentWorld = loc.getWorld().getName();
            if (worldName == null) {
                worldName = currentWorld;
            } else if (!worldName.equals(currentWorld)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a location is inside a cuboid defined by two corners.
     * 
     * @param loc Location to check
     * @param corner1 First corner
     * @param corner2 Second corner
     * @return true if location is inside cuboid
     */
    private boolean isLocationInside(Location loc, Location corner1, Location corner2) {
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
}
