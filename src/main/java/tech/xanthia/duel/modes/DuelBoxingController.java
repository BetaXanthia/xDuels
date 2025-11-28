package tech.xanthia.duel.modes;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;
import tech.xanthia.duel.DuelTeam;

import java.util.logging.Logger;

/**
 * DuelBoxingController.java
 * Controls Boxing mode duels.
 * 
 * Boxing Mode Rules:
 * - No damage (all damage cancelled)
 * - Hit counter tracks left-click hits
 * - First to hit threshold wins
 * - Milestones announced (10, 25, 50, 75, 90 hits)
 * - Custom hit sounds
 * - Actionbar displays hit count
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelBoxingController {
    
    private final DuelInstance duel;
    private final Logger logger;
    
    private final int hitThreshold;
    
    // Milestone announcements
    private static final int[] MILESTONES = {10, 25, 50, 75, 90};
    
    public DuelBoxingController(DuelInstance duel, Logger logger) {
        this.duel = duel;
        this.logger = logger;
        this.hitThreshold = duel.getSettings().getBoxingHitThreshold();
        
        logger.info("Boxing controller initialized for duel " + duel.getDuelId() + 
                    " | Hit threshold: " + hitThreshold);
    }
    
    /**
     * Handle boxing hit
     * 
     * @param event Damage event
     * @return true if round won
     */
    public boolean handleBoxingHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return false;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return false;
        }
        
        // Cancel damage
        event.setDamage(0);
        event.setCancelled(true);
        
        // Get participants
        DuelParticipant attackerParticipant = duel.getParticipant(attacker.getUniqueId());
        DuelParticipant victimParticipant = duel.getParticipant(victim.getUniqueId());
        
        if (attackerParticipant == null || victimParticipant == null) {
            return false;
        }
        
        // Increment hit counter
        attackerParticipant.incrementBoxingHits();
        int hits = attackerParticipant.getBoxingHits();
        
        // Play hit sound
        attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
        victim.playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.8f);
        
        // Update actionbar
        updateActionbars();
        
        // Check milestone
        checkMilestone(attackerParticipant, hits);
        
        // Check win condition
        if (hits >= hitThreshold) {
            announceWinner(attackerParticipant);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update hit count actionbars for all players
     */
    public void updateActionbars() {
        for (DuelParticipant participant : duel.getParticipants()) {
            Player player = participant.getPlayer();
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Get opponent hit count
            DuelTeam opponentTeam = duel.getOpponentTeam(participant.getPlayerUUID());
            int opponentHits = getTeamMaxHits(opponentTeam);
            int playerHits = participant.getBoxingHits();
            
            // Format: "Your Hits: 45 | Opponent: 32"
            String actionbar = ChatColor.GREEN + "Your Hits: " + playerHits + 
                             ChatColor.GRAY + " | " + 
                             ChatColor.RED + "Opponent: " + opponentHits;
            
            player.sendActionBar(actionbar);
        }
    }
    
    /**
     * Get highest hit count in team
     * 
     * @param team Team
     * @return Max hits
     */
    private int getTeamMaxHits(DuelTeam team) {
        int maxHits = 0;
        for (DuelParticipant participant : team.getMembers()) {
            if (participant.getBoxingHits() > maxHits) {
                maxHits = participant.getBoxingHits();
            }
        }
        return maxHits;
    }
    
    /**
     * Check and announce milestone
     * 
     * @param participant Participant
     * @param hits Current hit count
     */
    private void checkMilestone(DuelParticipant participant, int hits) {
        for (int milestone : MILESTONES) {
            if (hits == milestone) {
                String message = ChatColor.YELLOW + participant.getPlayerName() + 
                               ChatColor.GOLD + " reached " + milestone + " hits!";
                duel.broadcast(message);
                
                // Play sound
                Player player = participant.getPlayer();
                if (player != null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                }
                
                break;
            }
        }
    }
    
    /**
     * Announce boxing winner
     * 
     * @param winner Winning participant
     */
    private void announceWinner(DuelParticipant winner) {
        duel.broadcast(ChatColor.GOLD + "========================================");
        duel.broadcast(ChatColor.GREEN + winner.getPlayerName() + " wins!");
        duel.broadcast(ChatColor.YELLOW + "Reached " + hitThreshold + " hits!");
        duel.broadcast(ChatColor.GOLD + "========================================");
        
        // Play victory sound
        for (Player player : duel.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 2.0f);
        }
    }
    
    /**
     * Get current hit counts for display
     * 
     * @return Formatted hit count string
     */
    public String getHitSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(ChatColor.GOLD).append("Boxing Hit Count:\n");
        
        for (DuelParticipant participant : duel.getParticipants()) {
            summary.append(ChatColor.WHITE).append(participant.getPlayerName())
                   .append(": ").append(ChatColor.YELLOW).append(participant.getBoxingHits())
                   .append(ChatColor.GRAY).append("/").append(hitThreshold)
                   .append("\n");
        }
        
        return summary.toString();
    }
}
