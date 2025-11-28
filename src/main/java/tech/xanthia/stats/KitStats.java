package tech.xanthia.stats;

/**
 * KitStats.java
 * Represents statistics for a specific kit.
 * 
 * Tracks per-kit performance including:
 * - Wins/losses
 * - Kills/deaths
 * - Damage statistics
 * - MMR/ELO rating
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class KitStats {
    
    private final String kitName;
    
    // Per-kit stats
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int damageDealt;
    private int damageTaken;
    
    // MMR/ELO rating for this kit
    private int mmr;
    
    public KitStats(String kitName) {
        this.kitName = kitName;
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        this.deaths = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.mmr = 1000; // Default starting MMR
    }
    
    /**
     * Get KDR for this kit
     * 
     * @return Kill/death ratio
     */
    public double getKDR() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }
    
    /**
     * Get win rate for this kit
     * 
     * @return Win rate percentage (0-100)
     */
    public double getWinRate() {
        int totalGames = wins + losses;
        if (totalGames == 0) {
            return 0.0;
        }
        return ((double) wins / totalGames) * 100.0;
    }
    
    /**
     * Get total games played with this kit
     * 
     * @return Total games
     */
    public int getTotalGames() {
        return wins + losses;
    }
    
    // Increment methods
    
    public void addWin() {
        wins++;
    }
    
    public void addLoss() {
        losses++;
    }
    
    public void addKills(int amount) {
        kills += amount;
    }
    
    public void addDeaths(int amount) {
        deaths += amount;
    }
    
    public void addDamageDealt(int amount) {
        damageDealt += amount;
    }
    
    public void addDamageTaken(int amount) {
        damageTaken += amount;
    }
    
    // Getters and Setters
    
    public String getKitName() {
        return kitName;
    }
    
    public int getWins() {
        return wins;
    }
    
    public void setWins(int wins) {
        this.wins = wins;
    }
    
    public int getLosses() {
        return losses;
    }
    
    public void setLosses(int losses) {
        this.losses = losses;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }
    
    public int getDamageTaken() {
        return damageTaken;
    }
    
    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }
    
    public int getMMR() {
        return mmr;
    }
    
    public void setMMR(int mmr) {
        this.mmr = mmr;
    }
}
