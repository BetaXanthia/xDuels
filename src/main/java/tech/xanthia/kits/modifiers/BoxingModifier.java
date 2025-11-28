package tech.xanthia.kits.modifiers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tech.xanthia.kits.KitRuntimeProfile;

/**
 * Boxing mode modifier.
 * 
 * First player to 100 hits wins.
 * No damage dealt, only hit counting.
 * Only left-click attack hits count.
 * Counters reset per round.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class BoxingModifier {
    
    public static final int HITS_TO_WIN = 100;
    
    /**
     * Handles a hit in boxing mode.
     * 
     * @param event Damage event
     * @param profile Kit runtime profile
     * @param attacker Attacking player
     * @param victim Victim player
     * @return Hit count for attacker, or -1 if win condition not met
     */
    public static int handleHit(EntityDamageByEntityEvent event, KitRuntimeProfile profile, 
                                 Player attacker, Player victim) {
        // Cancel damage
        event.setDamage(0);
        event.setCancelled(true);
        
        // Increment hit counter
        int hits = profile.incrementBoxingHits(attacker.getUniqueId().toString());
        
        // Check win condition
        if (hits >= HITS_TO_WIN) {
            return hits; // Signal win
        }
        
        return -1; // No win yet
    }
    
    /**
     * Resets hit counters for a new round.
     * 
     * @param profile Kit runtime profile
     */
    public static void resetRound(KitRuntimeProfile profile) {
        profile.resetAllBoxingHits();
    }
    
    /**
     * Gets hit count for a player.
     * 
     * @param profile Kit runtime profile
     * @param playerUUID Player UUID string
     * @return Hit count
     */
    public static int getHits(KitRuntimeProfile profile, String playerUUID) {
        return profile.getBoxingHits(playerUUID);
    }
    
    /**
     * Formats hit count for display.
     * 
     * @param hits Current hits
     * @return Formatted string
     */
    public static String formatHits(int hits) {
        return "§6" + hits + "§7/§e" + HITS_TO_WIN + " §7hits";
    }
    
    /**
     * Gets progress percentage.
     * 
     * @param hits Current hits
     * @return Progress percentage (0-100)
     */
    public static double getProgress(int hits) {
        return Math.min(100.0, (hits / (double) HITS_TO_WIN) * 100.0);
    }
}
