package tech.xanthia.duel.modes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;

import java.util.logging.Logger;

/**
 * DuelSumoController.java
 * Controls Sumo mode duels.
 * 
 * Sumo Mode Rules:
 * - No damage (all damage cancelled)
 * - No fall damage
 * - No projectiles (rods, arrows)
 * - No building/breaking
 * - Increased knockback
 * - Elimination on:
 *   - Falling below Y threshold
 *   - Touching water/lava
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelSumoController {
    
    private final DuelInstance duel;
    private final Logger logger;
    
    private final double failY;
    
    public DuelSumoController(DuelInstance duel, Logger logger) {
        this.duel = duel;
        this.logger = logger;
        this.failY = duel.getSettings().getSumoFailY();
        
        logger.info("Sumo controller initialized for duel " + duel.getDuelId() + 
                    " | Fail Y: " + failY);
    }
    
    /**
     * Handle player movement in sumo
     * 
     * @param event Move event
     * @return true if player eliminated
     */
    public boolean handleMovement(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DuelParticipant participant = duel.getParticipant(player.getUniqueId());
        
        if (participant == null || !participant.isAlive()) {
            return false;
        }
        
        Location location = player.getLocation();
        
        // Check Y-level
        if (location.getY() < failY) {
            eliminatePlayer(participant, "fell off the platform");
            return true;
        }
        
        // Check water/lava contact
        Material blockType = location.getBlock().getType();
        if (blockType == Material.WATER || blockType == Material.LAVA) {
            eliminatePlayer(participant, "touched " + (blockType == Material.WATER ? "water" : "lava"));
            return true;
        }
        
        // Check block below for water/lava
        Material belowType = location.clone().subtract(0, 1, 0).getBlock().getType();
        if (belowType == Material.WATER || belowType == Material.LAVA) {
            eliminatePlayer(participant, "touched " + (belowType == Material.WATER ? "water" : "lava"));
            return true;
        }
        
        return false;
    }
    
    /**
     * Eliminate player from sumo round
     * 
     * @param participant Participant to eliminate
     * @param reason Elimination reason
     */
    private void eliminatePlayer(DuelParticipant participant, String reason) {
        if (participant.isEliminated()) {
            return;
        }
        
        participant.recordDeath();
        participant.setEliminated(true);
        
        Player player = participant.getPlayer();
        
        // Announce elimination
        String message = ChatColor.RED + participant.getPlayerName() + 
                        ChatColor.GRAY + " was eliminated: " + reason;
        duel.broadcast(message);
        
        // Play elimination sound
        if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
            
            // Teleport to spectator location (above arena)
            Location spectatorLoc = duel.getArena().getCenter().clone().add(0, 10, 0);
            player.teleport(spectatorLoc);
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        
        logger.info("Player " + participant.getPlayerName() + " eliminated from sumo duel " + 
                    duel.getDuelId() + ": " + reason);
    }
    
    /**
     * Check if player is in safe zone (above fail Y)
     * 
     * @param player Player
     * @return true if safe
     */
    public boolean isInSafeZone(Player player) {
        return player.getLocation().getY() >= failY;
    }
    
    /**
     * Apply enhanced knockback for sumo mode
     * 
     * @param player Player to apply knockback to
     * @param multiplier Knockback multiplier
     */
    public void applyEnhancedKnockback(Player player, double multiplier) {
        // Knockback is already enhanced via kit settings
        // This method can be used for additional knockback modifications
    }
    
    /**
     * Get sumo mode summary
     * 
     * @return Formatted summary string
     */
    public String getSumoSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(ChatColor.GOLD).append("Sumo Status:\n");
        
        for (DuelParticipant participant : duel.getParticipants()) {
            Player player = participant.getPlayer();
            String status = participant.isAlive() ? 
                          ChatColor.GREEN + "Alive" : 
                          ChatColor.RED + "Eliminated";
            
            double currentY = player != null ? player.getLocation().getY() : 0;
            
            summary.append(ChatColor.WHITE).append(participant.getPlayerName())
                   .append(": ").append(status);
            
            if (participant.isAlive() && player != null) {
                summary.append(ChatColor.GRAY).append(" (Y: ")
                       .append(String.format("%.1f", currentY)).append(")");
            }
            
            summary.append("\n");
        }
        
        return summary.toString();
    }
}
