package tech.xanthia.duel;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import tech.xanthia.arena.Arena;
import tech.xanthia.kits.Kit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DuelInstance.java
 * Represents a complete duel instance with all state and participants.
 * 
 * This is the core duel object that contains:
 * - Duel metadata (ID, state, settings)
 * - Teams and participants
 * - Arena and spawn locations
 * - Kit information
 * - Round tracking
 * - Statistics
 * 
 * Thread-safe for concurrent access from event handlers.
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelInstance {
    
    // Core metadata
    private final UUID duelId;
    private DuelState state;
    private final boolean ranked;
    private final long createdAt;
    
    // Teams and participants
    private final DuelTeam teamA;
    private final DuelTeam teamB;
    private final Map<UUID, DuelParticipant> participants;
    
    // Arena information
    private final Arena arena;
    private final Location[] spawnPointsA;
    private final Location[] spawnPointsB;
    private final BoundingBox arenaBounds;
    
    // Kit information
    private final Kit kit;
    
    // Round tracking
    private int currentRound;
    private final int totalRounds;
    private DuelTeam roundWinner;
    private DuelTeam duelWinner;
    
    // Special mode flags
    private final String duelMode; // "standard", "boxing", "sumo", "spleef", "ffa"
    private final boolean ffaMode;
    
    // Settings (merged from kit + config + arena)
    private final DuelSettings settings;
    
    // Spectators
    private final Set<UUID> spectators;
    
    // Timing
    private long startTime;
    private long endTime;
    private long roundStartTime;
    
    // Current duel ID (for party tracking)
    private UUID currentDuelId;
    private String currentArenaId;
    
    public DuelInstance(UUID duelId, List<UUID> team1Players, List<UUID> team2Players,
                        Arena arena, Kit kit, boolean ranked, int totalRounds, String duelMode) {
        this.duelId = duelId;
        this.state = DuelState.CREATED;
        this.ranked = ranked;
        this.createdAt = System.currentTimeMillis();
        
        // Initialize teams
        this.teamA = new DuelTeam("A", "Team A", ChatColor.RED);
        this.teamB = new DuelTeam("B", "Team B", ChatColor.BLUE);
        this.participants = new ConcurrentHashMap<>();
        
        // Add participants to teams
        for (UUID playerUUID : team1Players) {
            String playerName = org.bukkit.Bukkit.getOfflinePlayer(playerUUID).getName();
            DuelParticipant participant = new DuelParticipant(playerUUID, playerName);
            teamA.addMember(participant);
            participants.put(playerUUID, participant);
        }
        
        for (UUID playerUUID : team2Players) {
            String playerName = org.bukkit.Bukkit.getOfflinePlayer(playerUUID).getName();
            DuelParticipant participant = new DuelParticipant(playerUUID, playerName);
            teamB.addMember(participant);
            participants.put(playerUUID, participant);
        }
        
        // Arena setup
        this.arena = arena;
        this.spawnPointsA = calculateSpawnPoints(arena, team1Players.size(), true);
        this.spawnPointsB = calculateSpawnPoints(arena, team2Players.size(), false);
        double[] boundsArray = arena.getBoundingBox();
        this.arenaBounds = boundsArray != null ? 
            org.bukkit.util.BoundingBox.of(
                new org.bukkit.util.Vector(boundsArray[0], boundsArray[1], boundsArray[2]),
                new org.bukkit.util.Vector(boundsArray[3], boundsArray[4], boundsArray[5])) : null;
        
        // Kit setup
        this.kit = kit;
        
        // Round setup
        this.currentRound = 0;
        this.totalRounds = totalRounds;
        this.roundWinner = null;
        this.duelWinner = null;
        
        // Mode setup
        this.duelMode = duelMode != null ? duelMode : "standard";
        this.ffaMode = "ffa".equalsIgnoreCase(this.duelMode);
        
        // Settings (merged from kit config)
        this.settings = new DuelSettings(kit);
        
        // Spectators
        this.spectators = ConcurrentHashMap.newKeySet();
        
        // Timing
        this.startTime = 0;
        this.endTime = 0;
        this.roundStartTime = 0;
        
        // IDs
        this.currentDuelId = duelId;
        this.currentArenaId = arena.getName();
    }
    
    /**
     * Calculate spawn points for team based on arena and team size
     * 
     * @param arena Arena
     * @param teamSize Number of players
     * @param isTeamA true for team A, false for team B
     * @return Array of spawn locations
     */
    private Location[] calculateSpawnPoints(Arena arena, int teamSize, boolean isTeamA) {
        Location baseSpawn = isTeamA ? arena.getSpawn1() : arena.getSpawn2();
        Location[] spawns = new Location[teamSize];
        
        if (teamSize == 1) {
            spawns[0] = baseSpawn.clone();
        } else {
            // Spread players in a line perpendicular to facing direction
            double spacing = 2.0; // blocks between players
            double totalWidth = (teamSize - 1) * spacing;
            double startOffset = -totalWidth / 2.0;
            
            for (int i = 0; i < teamSize; i++) {
                double offset = startOffset + (i * spacing);
                Location spawn = baseSpawn.clone();
                
                // Offset perpendicular to facing direction
                double yaw = Math.toRadians(spawn.getYaw() + 90);
                spawn.add(Math.cos(yaw) * offset, 0, Math.sin(yaw) * offset);
                
                spawns[i] = spawn;
            }
        }
        
        return spawns;
    }
    
    /**
     * Transition to new state with validation
     * 
     * @param newState Target state
     * @return true if transition successful
     */
    public boolean setState(DuelState newState) {
        if (!state.canTransitionTo(newState)) {
            return false;
        }
        
        DuelState oldState = this.state;
        this.state = newState;
        
        // State-specific actions
        switch (newState) {
            case COMBAT:
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
                roundStartTime = System.currentTimeMillis();
                currentRound++;
                break;
                
            case FINISHED:
                endTime = System.currentTimeMillis();
                break;
                
            case CLEANUP:
                // Cleanup handled by DuelManager
                break;
        }
        
        return true;
    }
    
    /**
     * Get participant by UUID
     * 
     * @param playerUUID Player UUID
     * @return Participant or null
     */
    public DuelParticipant getParticipant(UUID playerUUID) {
        return participants.get(playerUUID);
    }
    
    /**
     * Check if player is participating
     * 
     * @param playerUUID Player UUID
     * @return true if participant
     */
    public boolean isParticipant(UUID playerUUID) {
        return participants.containsKey(playerUUID);
    }
    
    /**
     * Get all participants
     * 
     * @return Unmodifiable collection of participants
     */
    public Collection<DuelParticipant> getParticipants() {
        return Collections.unmodifiableCollection(participants.values());
    }
    
    /**
     * Get all online players in duel
     * 
     * @return List of online players
     */
    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (DuelParticipant participant : participants.values()) {
            Player player = participant.getPlayer();
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }
    
    /**
     * Broadcast message to all participants
     * 
     * @param message Message to send
     */
    public void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Broadcast message to all participants and spectators
     * 
     * @param message Message to send
     */
    public void broadcastAll(String message) {
        broadcast(message);
        for (UUID spectatorUUID : spectators) {
            Player spectator = org.bukkit.Bukkit.getPlayer(spectatorUUID);
            if (spectator != null && spectator.isOnline()) {
                spectator.sendMessage(message);
            }
        }
    }
    
    /**
     * Add spectator
     * 
     * @param playerUUID Player UUID
     * @return true if added successfully
     */
    public boolean addSpectator(UUID playerUUID) {
        if (!state.allowsSpectators()) {
            return false;
        }
        return spectators.add(playerUUID);
    }
    
    /**
     * Remove spectator
     * 
     * @param playerUUID Player UUID
     */
    public void removeSpectator(UUID playerUUID) {
        spectators.remove(playerUUID);
    }
    
    /**
     * Check if player is spectating
     * 
     * @param playerUUID Player UUID
     * @return true if spectator
     */
    public boolean isSpectator(UUID playerUUID) {
        return spectators.contains(playerUUID);
    }
    
    /**
     * Get duel duration in milliseconds
     * 
     * @return Duration or 0 if not started
     */
    public long getDuration() {
        if (startTime == 0) {
            return 0;
        }
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
    
    /**
     * Get current round duration in milliseconds
     * 
     * @return Duration or 0 if not started
     */
    public long getRoundDuration() {
        if (roundStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - roundStartTime;
    }
    
    /**
     * Check if location is within arena bounds
     * 
     * @param location Location to check
     * @return true if within bounds
     */
    public boolean isInBounds(Location location) {
        return arenaBounds.contains(location.toVector());
    }
    
    /**
     * Get team for player
     * 
     * @param playerUUID Player UUID
     * @return Team or null
     */
    public DuelTeam getTeam(UUID playerUUID) {
        DuelParticipant participant = participants.get(playerUUID);
        return participant != null ? participant.getTeam() : null;
    }
    
    /**
     * Get opponent team for player
     * 
     * @param playerUUID Player UUID
     * @return Opponent team or null
     */
    public DuelTeam getOpponentTeam(UUID playerUUID) {
        DuelTeam playerTeam = getTeam(playerUUID);
        if (playerTeam == null) {
            return null;
        }
        return playerTeam == teamA ? teamB : teamA;
    }
    
    // Getters
    
    public UUID getDuelId() {
        return duelId;
    }
    
    public DuelState getState() {
        return state;
    }
    
    public boolean isRanked() {
        return ranked;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public DuelTeam getTeamA() {
        return teamA;
    }
    
    public DuelTeam getTeamB() {
        return teamB;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public Location[] getSpawnPointsA() {
        return spawnPointsA;
    }
    
    public Location[] getSpawnPointsB() {
        return spawnPointsB;
    }
    
    public BoundingBox getArenaBounds() {
        return arenaBounds;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public int getTotalRounds() {
        return totalRounds;
    }
    
    public DuelTeam getRoundWinner() {
        return roundWinner;
    }
    
    public void setRoundWinner(DuelTeam roundWinner) {
        this.roundWinner = roundWinner;
    }
    
    public DuelTeam getDuelWinner() {
        return duelWinner;
    }
    
    public void setDuelWinner(DuelTeam duelWinner) {
        this.duelWinner = duelWinner;
    }
    
    public String getDuelMode() {
        return duelMode;
    }
    
    public boolean isFfaMode() {
        return ffaMode;
    }
    
    public DuelSettings getSettings() {
        return settings;
    }
    
    public Set<UUID> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public UUID getCurrentDuelId() {
        return currentDuelId;
    }
    
    public String getCurrentArenaId() {
        return currentArenaId;
    }
    
    @Override
    public String toString() {
        return "DuelInstance{" +
                "id=" + duelId +
                ", state=" + state +
                ", mode=" + duelMode +
                ", ranked=" + ranked +
                ", round=" + currentRound + "/" + totalRounds +
                ", teamA=" + teamA.getSize() +
                ", teamB=" + teamB.getSize() +
                ", arena=" + arena.getName() +
                ", kit=" + kit.getName() +
                '}';
    }
}
