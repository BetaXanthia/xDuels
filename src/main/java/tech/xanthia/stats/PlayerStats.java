package tech.xanthia.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerStats.java
 * Represents all statistics for a single player.
 * 
 * Contains:
 * - Global stats (all duels combined)
 * - Per-kit stats
 * - Combo tracking
 * - Playtime
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class PlayerStats {
    
    private final UUID playerUUID;
    
    // Global stats
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int damageDealt;
    private int damageTaken;
    private int longestCombo;
    private int totalCombos;
    private long playtime; // Milliseconds
    
    // Per-kit stats
    private final Map<String, KitStats> kitStats;
    
    // Dirty flag for async save optimization
    private volatile boolean dirty;
    
    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        this.deaths = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.longestCombo = 0;
        this.totalCombos = 0;
        this.playtime = 0;
        this.kitStats = new HashMap<>();
        this.dirty = false;
    }
    
    /**
     * Get or create kit stats
     * 
     * @param kitName Kit name
     * @return KitStats
     */
    public KitStats getKitStats(String kitName) {
        return kitStats.computeIfAbsent(kitName, k -> new KitStats(kitName));
    }
    
    /**
     * Get all kit stats
     * 
     * @return Map of kit name to stats
     */
    public Map<String, KitStats> getAllKitStats() {
        return kitStats;
    }
    
    /**
     * Record combo
     * 
     * @param comboLength Combo length
     */
    public void recordCombo(int comboLength) {
        totalCombos++;
        if (comboLength > longestCombo) {
            longestCombo = comboLength;
        }
        markDirty();
    }
    
    /**
     * Get KDR (kill/death ratio)
     * 
     * @return KDR
     */
    public double getKDR() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }
    
    /**
     * Get win rate percentage
     * 
     * @return Win rate (0-100)
     */
    public double getWinRate() {
        int totalGames = wins + losses;
        if (totalGames == 0) {
            return 0.0;
        }
        return ((double) wins / totalGames) * 100.0;
    }
    
    /**
     * Mark stats as dirty (needs save)
     */
    public void markDirty() {
        this.dirty = true;
    }
    
    /**
     * Clear dirty flag
     */
    public void clearDirty() {
        this.dirty = false;
    }
    
    /**
     * Check if stats need saving
     * 
     * @return true if dirty
     */
    public boolean isDirty() {
        return dirty;
    }
    
    // Increment methods
    
    public void addWin() {
        wins++;
        markDirty();
    }
    
    public void addLoss() {
        losses++;
        markDirty();
    }
    
    public void addKills(int amount) {
        kills += amount;
        markDirty();
    }
    
    public void addDeaths(int amount) {
        deaths += amount;
        markDirty();
    }
    
    public void addDamageDealt(int amount) {
        damageDealt += amount;
        markDirty();
    }
    
    public void addDamageTaken(int amount) {
        damageTaken += amount;
        markDirty();
    }
    
    public void addPlaytime(long milliseconds) {
        playtime += milliseconds;
        markDirty();
    }
    
    // Getters and Setters
    
    public UUID getPlayerUUID() {
        return playerUUID;
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
    
    public int getLongestCombo() {
        return longestCombo;
    }
    
    public void setLongestCombo(int longestCombo) {
        this.longestCombo = longestCombo;
    }
    
    public int getTotalCombos() {
        return totalCombos;
    }
    
    public void setTotalCombos(int totalCombos) {
        this.totalCombos = totalCombos;
    }
    
    public long getPlaytime() {
        return playtime;
    }
    
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }
}
