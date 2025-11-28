package tech.xanthia.queue.utils;

/**
 * ELO/MMR calculation system for ranked matches.
 * 
 * Uses standard ELO algorithm with K-factor adjustments.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueEloCalculator {
    
    private static final int K_FACTOR_SOLO = 32;
    private static final int K_FACTOR_TEAM = 24;
    
    /**
     * Calculates ELO change for a player/team.
     * 
     * @param winnerMMR Winner's MMR
     * @param loserMMR Loser's MMR
     * @param isTeam True if team match
     * @return ELO change (positive for winner, negative for loser)
     */
    public static int calculateEloChange(int winnerMMR, int loserMMR, boolean isTeam) {
        int kFactor = isTeam ? K_FACTOR_TEAM : K_FACTOR_SOLO;
        
        // Calculate expected scores
        double expectedWinner = getExpectedScore(winnerMMR, loserMMR);
        
        // Winner gets 1, loser gets 0
        double actualWinner = 1.0;
        
        // Calculate change
        int change = (int) Math.round(kFactor * (actualWinner - expectedWinner));
        
        return Math.max(1, change); // Minimum 1 point change
    }
    
    /**
     * Gets expected win probability.
     * 
     * Formula: E = 1 / (1 + 10^((opponentMMR - playerMMR) / 400))
     * 
     * @param playerMMR Player's MMR
     * @param opponentMMR Opponent's MMR
     * @return Expected score (0.0 to 1.0)
     */
    public static double getExpectedScore(int playerMMR, int opponentMMR) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentMMR - playerMMR) / 400.0));
    }
    
    /**
     * Calculates team average MMR.
     * 
     * @param mmrs Array of team member MMRs
     * @return Average MMR
     */
    public static int calculateTeamMMR(int[] mmrs) {
        if (mmrs.length == 0) {
            return 1000; // Default
        }
        
        int total = 0;
        for (int mmr : mmrs) {
            total += mmr;
        }
        
        return total / mmrs.length;
    }
    
    /**
     * Gets MMR tier name.
     * 
     * @param mmr MMR value
     * @return Tier name
     */
    public static String getTierName(int mmr) {
        if (mmr >= 2400) return "§6§lGrandmaster";
        if (mmr >= 2200) return "§c§lMaster";
        if (mmr >= 2000) return "§5§lDiamond";
        if (mmr >= 1800) return "§b§lPlatinum";
        if (mmr >= 1600) return "§e§lGold";
        if (mmr >= 1400) return "§7§lSilver";
        if (mmr >= 1200) return "§f§lBronze";
        return "§8§lUnranked";
    }
}
