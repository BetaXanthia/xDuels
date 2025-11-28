package tech.xanthia.duel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DuelTeam.java
 * Represents a team within a duel.
 * 
 * Features:
 * - Thread-safe member tracking
 * - Team color and display name
 * - Round win tracking
 * - Alive/dead member tracking
 * - Team statistics
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelTeam {
    
    private final String teamId;
    private final String displayName;
    private final ChatColor teamColor;
    private final Map<UUID, DuelParticipant> members;
    
    // Team statistics
    private int roundsWon;
    private int totalKills;
    private int totalDeaths;
    private int totalDamageDealt;
    private int totalDamageTaken;
    
    // Cached state
    private volatile int aliveCount;
    
    public DuelTeam(String teamId, String displayName, ChatColor teamColor) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.teamColor = teamColor;
        this.members = new ConcurrentHashMap<>();
        this.roundsWon = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalDamageDealt = 0;
        this.totalDamageTaken = 0;
        this.aliveCount = 0;
    }
    
    /**
     * Add participant to team
     * 
     * @param participant Participant to add
     */
    public void addMember(DuelParticipant participant) {
        members.put(participant.getPlayerUUID(), participant);
        participant.setTeam(this);
        if (participant.isAlive()) {
            aliveCount++;
        }
    }
    
    /**
     * Remove participant from team
     * 
     * @param playerUUID Player UUID to remove
     */
    public void removeMember(UUID playerUUID) {
        DuelParticipant removed = members.remove(playerUUID);
        if (removed != null && removed.isAlive()) {
            aliveCount--;
        }
    }
    
    /**
     * Get participant by UUID
     * 
     * @param playerUUID Player UUID
     * @return Participant or null
     */
    public DuelParticipant getMember(UUID playerUUID) {
        return members.get(playerUUID);
    }
    
    /**
     * Get all team members
     * 
     * @return Unmodifiable collection of members
     */
    public Collection<DuelParticipant> getMembers() {
        return Collections.unmodifiableCollection(members.values());
    }
    
    /**
     * Get all alive members
     * 
     * @return List of alive participants
     */
    public List<DuelParticipant> getAliveMembers() {
        return members.values().stream()
                .filter(DuelParticipant::isAlive)
                .toList();
    }
    
    /**
     * Get all online players in team
     * 
     * @return List of online players
     */
    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (DuelParticipant participant : members.values()) {
            Player player = participant.getPlayer();
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }
    
    /**
     * Check if team has specific player
     * 
     * @param playerUUID Player UUID
     * @return true if player is in team
     */
    public boolean hasMember(UUID playerUUID) {
        return members.containsKey(playerUUID);
    }
    
    /**
     * Get team size
     * 
     * @return Number of members
     */
    public int getSize() {
        return members.size();
    }
    
    /**
     * Get number of alive members (cached)
     * 
     * @return Alive count
     */
    public int getAliveCount() {
        return aliveCount;
    }
    
    /**
     * Check if entire team is eliminated
     * 
     * @return true if no alive members
     */
    public boolean isEliminated() {
        return aliveCount == 0;
    }
    
    /**
     * Mark participant as dead and update alive count
     * 
     * @param playerUUID Player UUID
     */
    public void markDead(UUID playerUUID) {
        DuelParticipant participant = members.get(playerUUID);
        if (participant != null && participant.isAlive()) {
            participant.setAlive(false);
            aliveCount--;
            totalDeaths++;
        }
    }
    
    /**
     * Revive all members (for next round)
     */
    public void reviveAll() {
        for (DuelParticipant participant : members.values()) {
            participant.setAlive(true);
            participant.resetRoundStats();
        }
        aliveCount = members.size();
    }
    
    /**
     * Increment round wins
     */
    public void addRoundWin() {
        roundsWon++;
    }
    
    /**
     * Record kill for team
     */
    public void addKill() {
        totalKills++;
    }
    
    /**
     * Record damage dealt
     * 
     * @param damage Damage amount
     */
    public void addDamageDealt(double damage) {
        totalDamageDealt += (int) damage;
    }
    
    /**
     * Record damage taken
     * 
     * @param damage Damage amount
     */
    public void addDamageTaken(double damage) {
        totalDamageTaken += (int) damage;
    }
    
    /**
     * Get formatted team name with color
     * 
     * @return Colored team name
     */
    public String getFormattedName() {
        return teamColor + displayName;
    }
    
    /**
     * Broadcast message to all online team members
     * 
     * @param message Message to send
     */
    public void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
    
    // Getters
    
    public String getTeamId() {
        return teamId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatColor getTeamColor() {
        return teamColor;
    }
    
    public int getRoundsWon() {
        return roundsWon;
    }
    
    public int getTotalKills() {
        return totalKills;
    }
    
    public int getTotalDeaths() {
        return totalDeaths;
    }
    
    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }
    
    public int getTotalDamageTaken() {
        return totalDamageTaken;
    }
    
    /**
     * Get team kill/death ratio
     * 
     * @return KDR
     */
    public double getKDR() {
        if (totalDeaths == 0) {
            return totalKills;
        }
        return (double) totalKills / totalDeaths;
    }
    
    @Override
    public String toString() {
        return "DuelTeam{" +
                "id='" + teamId + '\'' +
                ", name='" + displayName + '\'' +
                ", members=" + members.size() +
                ", alive=" + aliveCount +
                ", roundsWon=" + roundsWon +
                '}';
    }
}
