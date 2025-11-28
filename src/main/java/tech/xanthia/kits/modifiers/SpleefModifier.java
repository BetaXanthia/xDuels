package tech.xanthia.kits.modifiers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Spleef mode modifier.
 * 
 * Player loses if:
 * - Touch water OR lava
 * - Fall through void region (below threshold)
 * 
 * Damage and regeneration disabled.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class SpleefModifier {
    
    private static final double DEFAULT_VOID_THRESHOLD = 50.0;
    
    /**
     * Checks if a player has lost in spleef mode.
     * 
     * @param player Player to check
     * @param voidThreshold Y-level threshold for void
     * @return True if player lost
     */
    public static boolean checkLossCondition(Player player, double voidThreshold) {
        Location loc = player.getLocation();
        
        // Check Y-level
        if (loc.getY() < voidThreshold) {
            return true;
        }
        
        // Check if standing in water or lava
        Material blockType = loc.getBlock().getType();
        if (blockType == Material.WATER || blockType == Material.LAVA) {
            return true;
        }
        
        // Check block below
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.WATER || below == Material.LAVA) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a player has lost in spleef mode using default void threshold.
     * 
     * @param player Player to check
     * @return True if player lost
     */
    public static boolean checkLossCondition(Player player) {
        return checkLossCondition(player, DEFAULT_VOID_THRESHOLD);
    }
    
    /**
     * Gets the default void threshold.
     * 
     * @return Default Y-level threshold
     */
    public static double getDefaultVoidThreshold() {
        return DEFAULT_VOID_THRESHOLD;
    }
    
    /**
     * Validates a block break in spleef mode.
     * 
     * Typically only allows breaking specific blocks (e.g., snow, ice).
     * 
     * @param blockType Block type being broken
     * @return True if block can be broken
     */
    public static boolean canBreakBlock(Material blockType) {
        // Common spleef blocks
        return blockType == Material.SNOW_BLOCK ||
               blockType == Material.SNOW ||
               blockType == Material.ICE ||
               blockType == Material.PACKED_ICE ||
               blockType == Material.BLUE_ICE ||
               blockType == Material.POWDER_SNOW ||
               blockType == Material.WHITE_WOOL ||
               blockType == Material.WHITE_CONCRETE;
    }
    
    /**
     * Checks if a block should regenerate after being broken.
     * 
     * In some spleef variants, blocks regenerate after a delay.
     * 
     * @param blockType Block type
     * @return True if block should regenerate
     */
    public static boolean shouldRegenerate(Material blockType) {
        // Typically snow and ice regenerate
        return blockType == Material.SNOW_BLOCK ||
               blockType == Material.SNOW ||
               blockType == Material.ICE;
    }
}
