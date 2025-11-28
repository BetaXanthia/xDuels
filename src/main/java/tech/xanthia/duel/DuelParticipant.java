package tech.xanthia.duel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * DuelParticipant.java
 * Represents a single player participating in a duel.
 * 
 * Tracks:
 * - Player identity
 * - Team assignment
 * - Alive/dead status
 * - Combat statistics
 * - Round-specific data
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelParticipant {
    
    private final UUID playerUUID;
    private final String playerName;
    private DuelTeam team;
    
    // Combat state
    private boolean alive;
    private boolean frozen;
    private long lastDamageTime;
    private long lastHitTime;
    
    // Statistics (cumulative across all rounds)
    private int kills;
    private int deaths;
    private int damageDealt;
    private int damageTaken;
    private int hits;
    private int combos;
    private int longestCombo;
    
    // Round-specific stats (reset each round)
    private int roundKills;
    private int roundDamageDealt;
    private int roundDamageTaken;
    private int roundHits;
    private int currentCombo;
    
    // Special mode data
    private int boxingHits; // Boxing mode hit counter
    private boolean eliminated; // Sumo/Spleef elimination flag
    
    public DuelParticipant(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.alive = true;
        this.frozen = false;
        this.lastDamageTime = 0;
        this.lastHitTime = 0;
        this.kills = 0;
        this.deaths = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.hits = 0;
        this.combos = 0;
        this.longestCombo = 0;
        this.roundKills = 0;
        this.roundDamageDealt = 0;
        this.roundDamageTaken = 0;
        this.roundHits = 0;
        this.currentCombo = 0;
        this.boxingHits = 0;
        this.eliminated = false;
    }
    
    /**
     * Get Bukkit Player object
     * 
     * @return Player or null if offline
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
    
    /**
     * Check if player is online
     * 
     * @return true if online
     */
    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }
    
    /**
     * Record hit dealt to opponent
     * 
     * @param damage Damage dealt
     */
    public void recordHit(double damage) {
        hits++;
        roundHits++;
        damageDealt += (int) damage;
        roundDamageDealt += (int) damage;
        lastHitTime = System.currentTimeMillis();
        
        // Combo tracking
        currentCombo++;
        if (currentCombo > longestCombo) {
            longestCombo = currentCombo;
        }
    }
    
    /**
     * Record damage taken
     * 
     * @param damage Damage amount
     */
    public void recordDamageTaken(double damage) {
        damageTaken += (int) damage;
        roundDamageTaken += (int) damage;
        lastDamageTime = System.currentTimeMillis();
        
        // Reset combo when hit
        if (currentCombo > 0) {
            combos++;
        }
        currentCombo = 0;
    }
    
    /**
     * Record kill
     */
    public void recordKill() {
        kills++;
        roundKills++;
        if (team != null) {
            team.addKill();
        }
    }
    
    /**
     * Record death
     */
    public void recordDeath() {
        deaths++;
        alive = false;
        if (team != null) {
            team.markDead(playerUUID);
        }
    }
    
    /**
     * Increment boxing hit counter
     */
    public void incrementBoxingHits() {
        boxingHits++;
    }
    
    /**
     * Reset round-specific statistics
     */
    public void resetRoundStats() {
        roundKills = 0;
        roundDamageDealt = 0;
        roundDamageTaken = 0;
        roundHits = 0;
        currentCombo = 0;
        boxingHits = 0;
        eliminated = false;
        alive = true;
    }
    
    /**
     * Get kill/death ratio
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
     * Get average damage per hit
     * 
     * @return Average damage
     */
    public double getAverageDamage() {
        if (hits == 0) {
            return 0.0;
        }
        return (double) damageDealt / hits;
    }
    
    /**
     * Check if player is currently in a combo
     * 
     * @return true if combo active
     */
    public boolean isInCombo() {
        return currentCombo > 0;
    }
    
    /**
     * Get time since last damage dealt (ms)
     * 
     * @return Milliseconds since last hit
     */
    public long getTimeSinceLastHit() {
        return System.currentTimeMillis() - lastHitTime;
    }
    
    /**
     * Get time since last damage taken (ms)
     * 
     * @return Milliseconds since last damage
     */
    public long getTimeSinceLastDamage() {
        return System.currentTimeMillis() - lastDamageTime;
    }
    
    // Getters and Setters
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public DuelTeam getTeam() {
        return team;
    }
    
    public void setTeam(DuelTeam team) {
        this.team = team;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean isFrozen() {
        return frozen;
    }
    
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
    
    public int getKills() {
        return kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public int getDamageTaken() {
        return damageTaken;
    }
    
    public int getHits() {
        return hits;
    }
    
    public int getCombos() {
        return combos;
    }
    
    public int getLongestCombo() {
        return longestCombo;
    }
    
    public int getRoundKills() {
        return roundKills;
    }
    
    public int getRoundDamageDealt() {
        return roundDamageDealt;
    }
    
    public int getRoundDamageTaken() {
        return roundDamageTaken;
    }
    
    public int getRoundHits() {
        return roundHits;
    }
    
    public int getCurrentCombo() {
        return currentCombo;
    }
    
    public int getBoxingHits() {
        return boxingHits;
    }
    
    public void setBoxingHits(int boxingHits) {
        this.boxingHits = boxingHits;
    }
    
    public boolean isEliminated() {
        return eliminated;
    }
    
    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
    
    @Override
    public String toString() {
        return "DuelParticipant{" +
                "name='" + playerName + '\'' +
                ", team=" + (team != null ? team.getTeamId() : "none") +
                ", alive=" + alive +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", kdr=" + String.format("%.2f", getKDR()) +
                '}';
    }
}
