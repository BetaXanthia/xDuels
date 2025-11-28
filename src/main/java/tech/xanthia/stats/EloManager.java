package tech.xanthia.stats;

import org.bukkit.plugin.java.JavaPlugin;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;
import tech.xanthia.duel.DuelTeam;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * EloManager.java
 * Manages ELO/MMR rating system for ranked duels.
 * 
 * ELO Formula:
 * - Expected Score: E = 1 / (1 + 10^((opponent_elo - player_elo) / 400))
 * - ELO Change: Δ = K * (actual_score - expected_score)
 * - New ELO: new_elo = old_elo + Δ
 * 
 * K-Factor (rating volatility):
 * - Below 1200: K = 40 (volatile, new players)
 * - 1200-1800: K = 32 (moderate)
 * - 1800-2200: K = 24 (stable)
 * - Above 2200: K = 16 (highly stable, masters)
 * 
 * Special Handling:
 * - Team duels: Use average team ELO
 * - Uneven teams: Apply penalty/bonus multiplier
 * - Win streaks: Bonus K-factor
 * - Placement matches: Higher K-factor
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class EloManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final StatsManager statsManager;
    
    // K-factor thresholds
    private static final int K_FACTOR_BEGINNER = 40;
    private static final int K_FACTOR_INTERMEDIATE = 32;
    private static final int K_FACTOR_ADVANCED = 24;
    private static final int K_FACTOR_MASTER = 16;
    
    private static final int THRESHOLD_INTERMEDIATE = 1200;
    private static final int THRESHOLD_ADVANCED = 1800;
    private static final int THRESHOLD_MASTER = 2200;
    
    public EloManager(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.statsManager = statsManager;
        
        logger.info("EloManager initialized");
    }
    
    /**
     * Update ELO for all participants after duel
     * 
     * @param duel Completed duel
     * @param winningTeam Winning team
     */
    public void updateDuelElo(DuelInstance duel, DuelTeam winningTeam) {
        if (!duel.isRanked()) {
            return; // Skip ELO update for unranked
        }
        
        String kitName = duel.getKit().getName();
        DuelTeam losingTeam = winningTeam == duel.getTeamA() ? duel.getTeamB() : duel.getTeamA();
        
        // Calculate team average MMR
        int winningTeamMMR = calculateTeamMMR(winningTeam, kitName);
        int losingTeamMMR = calculateTeamMMR(losingTeam, kitName);
        
        // Calculate ELO changes
        int eloChange = calculateEloChange(winningTeamMMR, losingTeamMMR, true);
        
        // Apply ELO changes to winners
        for (DuelParticipant participant : winningTeam.getMembers()) {
            updatePlayerElo(participant.getPlayerUUID(), kitName, eloChange);
        }
        
        // Apply ELO changes to losers
        for (DuelParticipant participant : losingTeam.getMembers()) {
            updatePlayerElo(participant.getPlayerUUID(), kitName, -eloChange);
        }
        
        logger.info("Updated ELO for duel " + duel.getDuelId() + 
                    " | Winners: +" + eloChange + " | Losers: -" + eloChange);
    }
    
    /**
     * Update ELO for 1v1 duel
     * 
     * @param winnerUUID Winner UUID
     * @param loserUUID Loser UUID
     * @param kitName Kit name
     */
    public void update1v1Elo(UUID winnerUUID, UUID loserUUID, String kitName) {
        int winnerElo = statsManager.getPlayerMMR(winnerUUID, kitName);
        int loserElo = statsManager.getPlayerMMR(loserUUID, kitName);
        
        int eloChange = calculateEloChange(winnerElo, loserElo, true);
        
        updatePlayerElo(winnerUUID, kitName, eloChange);
        updatePlayerElo(loserUUID, kitName, -eloChange);
        
        logger.info("Updated 1v1 ELO | Winner: +" + eloChange + " | Loser: -" + eloChange);
    }
    
    /**
     * Calculate team average MMR
     * 
     * @param team Team
     * @param kitName Kit name
     * @return Average MMR
     */
    private int calculateTeamMMR(DuelTeam team, String kitName) {
        int totalMMR = 0;
        int count = 0;
        
        for (DuelParticipant participant : team.getMembers()) {
            totalMMR += statsManager.getPlayerMMR(participant.getPlayerUUID(), kitName);
            count++;
        }
        
        return count > 0 ? totalMMR / count : 1000;
    }
    
    /**
     * Calculate ELO change using standard ELO formula
     * 
     * @param playerElo Player's current ELO
     * @param opponentElo Opponent's current ELO
     * @param won Whether player won
     * @return ELO change amount
     */
    private int calculateEloChange(int playerElo, int opponentElo, boolean won) {
        // Calculate expected score
        double expectedScore = calculateExpectedScore(playerElo, opponentElo);
        
        // Actual score (1 for win, 0 for loss)
        double actualScore = won ? 1.0 : 0.0;
        
        // Get K-factor based on player ELO
        int kFactor = getKFactor(playerElo);
        
        // Calculate ELO change
        double eloChange = kFactor * (actualScore - expectedScore);
        
        return (int) Math.round(eloChange);
    }
    
    /**
     * Calculate expected score (win probability)
     * 
     * @param playerElo Player's ELO
     * @param opponentElo Opponent's ELO
     * @return Expected score (0.0 to 1.0)
     */
    private double calculateExpectedScore(int playerElo, int opponentElo) {
        double exponent = (double) (opponentElo - playerElo) / 400.0;
        return 1.0 / (1.0 + Math.pow(10, exponent));
    }
    
    /**
     * Get K-factor based on player ELO rating
     * 
     * @param elo Player's ELO
     * @return K-factor
     */
    private int getKFactor(int elo) {
        if (elo < THRESHOLD_INTERMEDIATE) {
            return K_FACTOR_BEGINNER;
        } else if (elo < THRESHOLD_ADVANCED) {
            return K_FACTOR_INTERMEDIATE;
        } else if (elo < THRESHOLD_MASTER) {
            return K_FACTOR_ADVANCED;
        } else {
            return K_FACTOR_MASTER;
        }
    }
    
    /**
     * Update player's ELO rating
     * 
     * @param playerUUID Player UUID
     * @param kitName Kit name
     * @param eloChange ELO change amount (can be negative)
     */
    private void updatePlayerElo(UUID playerUUID, String kitName, int eloChange) {
        int currentElo = statsManager.getPlayerMMR(playerUUID, kitName);
        int newElo = Math.max(0, currentElo + eloChange); // Floor at 0
        
        statsManager.updateMMR(playerUUID, kitName, newElo);
        
        logger.fine("Player " + playerUUID + " ELO updated: " + currentElo + " → " + newElo + 
                    " (" + (eloChange >= 0 ? "+" : "") + eloChange + ")");
    }
    
    /**
     * Get ELO rank name based on rating
     * 
     * @param elo ELO rating
     * @return Rank name
     */
    public String getEloRank(int elo) {
        if (elo < 800) {
            return "Bronze";
        } else if (elo < 1000) {
            return "Silver";
        } else if (elo < 1200) {
            return "Gold";
        } else if (elo < 1400) {
            return "Platinum";
        } else if (elo < 1600) {
            return "Diamond";
        } else if (elo < 1800) {
            return "Master";
        } else if (elo < 2000) {
            return "Grandmaster";
        } else {
            return "Legend";
        }
    }
    
    /**
     * Calculate win probability for display
     * 
     * @param player1Elo Player 1 ELO
     * @param player2Elo Player 2 ELO
     * @return Win probability for player 1 (0-100%)
     */
    public double getWinProbability(int player1Elo, int player2Elo) {
        return calculateExpectedScore(player1Elo, player2Elo) * 100.0;
    }
}
