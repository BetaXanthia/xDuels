package tech.xanthia.kits.modifiers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tech.xanthia.kits.KitRuntimeProfile;

/**
 * Sumo mode modifier.
 * 
 * Player loses if:
 * - Touch water OR lava
 * - Y-level drops below configured threshold
 * 
 * No damage dealt, high knockback allowed.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class SumoModifier {
    
    private static final double DEFAULT_VOID_THRESHOLD = 60.0;
    
    /**
     * Checks if a player has lost in sumo mode.
     * 
     * @param player Player to check
     * @param voidThreshold Y-level threshold (use arena-specific or default)
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
        
        // Check block below (for edge cases)
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.WATER || below == Material.LAVA) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a player has lost in sumo mode using default void threshold.
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
     * Applies sumo knockback multiplier to a player.
     * 
     * Can be used to increase knockback in sumo mode.
     * 
     * @param player Player to apply knockback to
     * @param multiplier Knockback multiplier (e.g., 1.5 for 50% more)
     */
    public static void applyKnockbackMultiplier(Player player, double multiplier) {
        // This would typically be handled by the duel system
        // by modifying the knockback resistance attribute or
        // applying velocity changes on hit events
    }
}
