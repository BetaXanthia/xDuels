package tech.xanthia.duel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.xanthia.arena.ArenaRegenerationManager;

import java.util.logging.Logger;

/**
 * DuelRoundController.java
 * Manages round lifecycle and win condition checking.
 * 
 * Round Flow:
 * 1. START ROUND → Teleport players to spawns
 * 2. RESET STATES → Clear inventories, reset health
 * 3. APPLY KITS → Apply kit loadouts
 * 4. COUNTDOWN → 3-2-1-GO sequence
 * 5. COMBAT → Enable combat, check win conditions
 * 6. ROUND END → Determine winner, update stats
 * 7. NEXT ROUND or END DUEL
 * 
 * Win Conditions:
 * - All opponents eliminated
 * - Opponent disconnected
 * - Opponent forfeit
 * - Timeout
 * - Left arena bounds
 * - Mode-specific (boxing hits, sumo fall, spleef fall)
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelRoundController {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final DuelManager duelManager;
    private final ArenaRegenerationManager regenManager;
    
    private final DuelInstance duel;
    
    // Round timing
    private long roundStartTime;
    private int roundTimeoutSeconds;
    
    public DuelRoundController(JavaPlugin plugin, DuelManager duelManager, 
                                ArenaRegenerationManager regenManager, DuelInstance duel) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.duelManager = duelManager;
        this.regenManager = regenManager;
        this.duel = duel;
        this.roundTimeoutSeconds = duel.getSettings().getRoundDuration();
    }
    
    /**
     * Start a new round
     */
    public void startRound() {
        int roundNumber = duel.getCurrentRound();
        
        logger.info("Starting round " + roundNumber + " for duel " + duel.getDuelId());
        
        // Announce round
        if (duel.getTotalRounds() > 1) {
            duel.broadcast(ChatColor.GOLD + "========================================");
            duel.broadcast(ChatColor.YELLOW + "Round " + roundNumber + " of " + duel.getTotalRounds());
            duel.broadcast(ChatColor.GOLD + "========================================");
        }
        
        // Freeze all players
        freezeAllPlayers();
        
        // Clear inventories
        clearAllInventories();
        
        // Reset round stats
        resetRoundStats();
        
        // Teleport to spawns
        teleportToSpawns();
        
        // Apply kits
        applyKitsToAll();
        
        // Start countdown (handled by DuelManager)
        // After countdown, combat begins
        
        roundStartTime = System.currentTimeMillis();
        
        // Start round timeout checker if configured
        if (roundTimeoutSeconds > 0) {
            startTimeoutChecker();
        }
    }
    
    /**
     * Check win conditions every tick
     * 
     * @return Winning team or null if no winner yet
     */
    public DuelTeam checkWinConditions() {
        // Check team elimination
        if (duel.getTeamA().isEliminated()) {
            return duel.getTeamB();
        }
        if (duel.getTeamB().isEliminated()) {
            return duel.getTeamA();
        }
        
        // Check disconnections
        DuelTeam disconnectedTeam = checkDisconnections();
        if (disconnectedTeam != null) {
            return getOpponentTeam(disconnectedTeam);
        }
        
        // Check arena bounds
        for (DuelParticipant participant : duel.getParticipants()) {
            if (!participant.isAlive()) continue;
            
            Player player = participant.getPlayer();
            if (player != null && !duel.isInBounds(player.getLocation())) {
                eliminatePlayer(participant, "left arena bounds");
                
                // Recheck elimination
                if (participant.getTeam().isEliminated()) {
                    return getOpponentTeam(participant.getTeam());
                }
            }
        }
        
        // Check timeout
        if (roundTimeoutSeconds > 0) {
            long elapsed = (System.currentTimeMillis() - roundStartTime) / 1000;
            if (elapsed >= roundTimeoutSeconds) {
                return determineWinnerByHealth();
            }
        }
        
        // No winner yet
        return null;
    }
    
    /**
     * End current round
     * 
     * @param winner Winning team
     * @param reason Win reason
     */
    public void endRound(DuelTeam winner, String reason) {
        if (winner == null) {
            logger.warning("Round ended with no winner for duel " + duel.getDuelId());
            return;
        }
        
        // Transition to ROUND_END
        if (!duel.setState(DuelState.ROUND_END)) {
            logger.severe("Failed to transition to ROUND_END for duel " + duel.getDuelId());
            return;
        }
        
        duel.setRoundWinner(winner);
        winner.addRoundWin();
        
        // Announce round winner
        duel.broadcast(ChatColor.GOLD + "========================================");
        duel.broadcast(ChatColor.GREEN + "Round Winner: " + winner.getFormattedName());
        duel.broadcast(ChatColor.GRAY + "Reason: " + reason);
        duel.broadcast(ChatColor.GOLD + "========================================");
        
        // Display round stats
        displayRoundStats();
        
        // Regenerate arena
        regenManager.regenerateArena(duel.getArena());
        
        // Check if more rounds needed
        if (shouldContinue()) {
            // Prepare next round
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                prepareNextRound();
            }, 60L); // 3 second delay
        } else {
            // Duel complete
            DuelTeam duelWinner = determineDuelWinner();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                duelManager.endDuel(duel.getDuelId(), duelWinner, "Best of " + duel.getTotalRounds() + " complete");
            }, 60L);
        }
    }
    
    /**
     * Prepare for next round
     */
    private void prepareNextRound() {
        // Revive all players
        duel.getTeamA().reviveAll();
        duel.getTeamB().reviveAll();
        
        // Transition back to COUNTDOWN
        if (duel.setState(DuelState.COUNTDOWN)) {
            // DuelManager will handle countdown and start combat
            startRound();
        }
    }
    
    /**
     * Determine if duel should continue to next round
     * 
     * @return true if more rounds needed
     */
    private boolean shouldContinue() {
        int roundsToWin = (duel.getTotalRounds() / 2) + 1;
        return duel.getTeamA().getRoundsWon() < roundsToWin && 
               duel.getTeamB().getRoundsWon() < roundsToWin;
    }
    
    /**
     * Determine overall duel winner
     * 
     * @return Winning team
     */
    private DuelTeam determineDuelWinner() {
        int teamAWins = duel.getTeamA().getRoundsWon();
        int teamBWins = duel.getTeamB().getRoundsWon();
        
        if (teamAWins > teamBWins) {
            return duel.getTeamA();
        } else if (teamBWins > teamAWins) {
            return duel.getTeamB();
        } else {
            // Tie (shouldn't happen with odd rounds, but handle it)
            return determineWinnerByHealth();
        }
    }
    
    /**
     * Determine winner by remaining health (timeout/tie scenario)
     * 
     * @return Team with more total health
     */
    private DuelTeam determineWinnerByHealth() {
        double teamAHealth = calculateTeamHealth(duel.getTeamA());
        double teamBHealth = calculateTeamHealth(duel.getTeamB());
        
        if (teamAHealth > teamBHealth) {
            return duel.getTeamA();
        } else if (teamBHealth > teamAHealth) {
            return duel.getTeamB();
        } else {
            // Perfect tie, choose team A
            return duel.getTeamA();
        }
    }
    
    /**
     * Calculate total team health
     * 
     * @param team Team
     * @return Total health
     */
    private double calculateTeamHealth(DuelTeam team) {
        double totalHealth = 0;
        for (DuelParticipant participant : team.getMembers()) {
            if (participant.isAlive()) {
                Player player = participant.getPlayer();
                if (player != null) {
                    totalHealth += player.getHealth();
                }
            }
        }
        return totalHealth;
    }
    
    /**
     * Check for team disconnections
     * 
     * @return Team with all players offline, or null
     */
    private DuelTeam checkDisconnections() {
        boolean teamAAllOffline = duel.getTeamA().getOnlinePlayers().isEmpty();
        boolean teamBAllOffline = duel.getTeamB().getOnlinePlayers().isEmpty();
        
        if (teamAAllOffline && !teamBAllOffline) {
            return duel.getTeamA();
        } else if (teamBAllOffline && !teamAAllOffline) {
            return duel.getTeamB();
        }
        
        return null;
    }
    
    /**
     * Eliminate player from round
     * 
     * @param participant Participant to eliminate
     * @param reason Elimination reason
     */
    public void eliminatePlayer(DuelParticipant participant, String reason) {
        if (!participant.isAlive()) {
            return;
        }
        
        participant.recordDeath();
        participant.setEliminated(true);
        
        Player player = participant.getPlayer();
        if (player != null) {
            player.sendMessage(ChatColor.RED + "You have been eliminated: " + reason);
        }
        
        duel.broadcast(ChatColor.GRAY + participant.getPlayerName() + " was eliminated: " + reason);
        
        logger.info("Player " + participant.getPlayerName() + " eliminated from duel " + 
                    duel.getDuelId() + ": " + reason);
    }
    
    /**
     * Get opponent team
     * 
     * @param team Team
     * @return Opponent team
     */
    private DuelTeam getOpponentTeam(DuelTeam team) {
        return team == duel.getTeamA() ? duel.getTeamB() : duel.getTeamA();
    }
    
    /**
     * Display round statistics
     */
    private void displayRoundStats() {
        // TODO: Format and display round stats
        // - Damage dealt
        // - Hits landed
        // - Combos
        // - MVP
    }
    
    /**
     * Start round timeout checker
     */
    private void startTimeoutChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (duel.getState() != DuelState.COMBAT) {
                return;
            }
            
            DuelTeam winner = checkWinConditions();
            if (winner != null) {
                endRound(winner, "opponent eliminated");
            }
        }, 20L, 20L); // Check every second
    }
    
    // Helper methods
    
    private void freezeAllPlayers() {
        for (DuelParticipant participant : duel.getParticipants()) {
            participant.setFrozen(true);
        }
    }
    
    private void clearAllInventories() {
        for (Player player : duel.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getActivePotionEffects().clear();
        }
    }
    
    private void resetRoundStats() {
        for (DuelParticipant participant : duel.getParticipants()) {
            participant.resetRoundStats();
        }
    }
    
    private void teleportToSpawns() {
        // Teleportation handled by DuelManager
    }
    
    private void applyKitsToAll() {
        // Kit application handled by DuelManager
    }
}
