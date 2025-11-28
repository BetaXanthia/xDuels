package tech.xanthia.kits.modifiers;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tech.xanthia.kits.KitRuntimeProfile;

/**
 * KB Sync (Knockback Synchronization) modifier.
 * 
 * When a player hits another:
 * - Both players receive synchronized knockback vectors
 * - Ensures fair CPS-independent combat
 * - Used in competitive NoDebuff/Debuff kits
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KBSyncModifier {
    
    private static final double DEFAULT_KB_MULTIPLIER = 0.4;
    private static final double DEFAULT_Y_BOOST = 0.4;
    
    /**
     * Applies synchronized knockback to both players.
     * 
     * @param attacker Attacking player
     * @param victim Victim player
     * @param profile Kit runtime profile
     */
    public static void applySyncedKnockback(Player attacker, Player victim, KitRuntimeProfile profile) {
        if (!profile.isKbSyncActive()) {
            return;
        }
        
        // Calculate knockback direction (victim away from attacker)
        Vector direction = victim.getLocation().toVector()
            .subtract(attacker.getLocation().toVector())
            .normalize();
        
        // Apply horizontal knockback
        Vector victimKB = direction.clone()
            .multiply(DEFAULT_KB_MULTIPLIER)
            .setY(DEFAULT_Y_BOOST);
        
        // Apply reverse knockback to attacker (reduced)
        Vector attackerKB = direction.clone()
            .multiply(-DEFAULT_KB_MULTIPLIER * 0.6)
            .setY(DEFAULT_Y_BOOST * 0.8);
        
        // Set velocities
        victim.setVelocity(victimKB);
        attacker.setVelocity(attackerKB);
    }
    
    /**
     * Applies synchronized knockback with custom multipliers.
     * 
     * @param attacker Attacking player
     * @param victim Victim player
     * @param profile Kit runtime profile
     * @param kbMultiplier Horizontal knockback multiplier
     * @param yBoost Vertical boost
     */
    public static void applySyncedKnockback(Player attacker, Player victim, KitRuntimeProfile profile,
                                            double kbMultiplier, double yBoost) {
        if (!profile.isKbSyncActive()) {
            return;
        }
        
        Vector direction = victim.getLocation().toVector()
            .subtract(attacker.getLocation().toVector())
            .normalize();
        
        Vector victimKB = direction.clone()
            .multiply(kbMultiplier)
            .setY(yBoost);
        
        Vector attackerKB = direction.clone()
            .multiply(-kbMultiplier * 0.6)
            .setY(yBoost * 0.8);
        
        victim.setVelocity(victimKB);
        attacker.setVelocity(attackerKB);
    }
    
    /**
     * Gets the default knockback multiplier.
     * 
     * @return Default KB multiplier
     */
    public static double getDefaultKBMultiplier() {
        return DEFAULT_KB_MULTIPLIER;
    }
    
    /**
     * Gets the default Y boost.
     * 
     * @return Default Y boost
     */
    public static double getDefaultYBoost() {
        return DEFAULT_Y_BOOST;
    }
}
