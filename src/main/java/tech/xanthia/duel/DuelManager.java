package tech.xanthia.duel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaManager;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitManager;
import tech.xanthia.party.PartyManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * DuelManager.java
 * Central nervous system of the duel engine.
 * 
 * Responsibilities:
 * - Create and track duel instances
 * - Manage duel lifecycle (teleport → countdown → combat → end)
 * - Enforce state machine transitions
 * - Interface with Arena, Kit, Party, Stats systems
 * - Handle duel cleanup and memory management
 * 
 * Thread-safe for concurrent event handling.
 * TPS-optimized with cached lookups and async operations.
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    
    // External managers
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final PartyManager partyManager;
    private DuelSpectatorManager spectatorManager;
    // TODO: Add StatsManager and EloManager when implemented
    
    // Duel tracking (O(1) lookups)
    private final Map<UUID, DuelInstance> activeDuels; // duelId -> duel
    private final Map<UUID, UUID> playerToDuel; // playerUUID -> duelId
    private final Map<UUID, UUID> partyToDuel; // partyUUID -> duelId
    
    // Teleportation tracking
    private final Set<UUID> teleporting;
    
    // Cleanup task
    private BukkitTask cleanupTask;
    
    // Configuration
    private final int cleanupInterval = 600; // 30 seconds
    private final int disconnectGracePeriod = 60000; // 60 seconds in ms
    
    public DuelManager(JavaPlugin plugin, ArenaManager arenaManager, KitManager kitManager, PartyManager partyManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
        this.partyManager = partyManager;
        
        this.activeDuels = new ConcurrentHashMap<>();
        this.playerToDuel = new ConcurrentHashMap<>();
        this.partyToDuel = new ConcurrentHashMap<>();
        this.teleporting = ConcurrentHashMap.newKeySet();
        
        startCleanupTask();
        logger.info("DuelManager initialized");
    }
    
    /**
     * Set the spectator manager (called after initialization)
     * 
     * @param spectatorManager Spectator manager
     */
    public void setSpectatorManager(DuelSpectatorManager spectatorManager) {
        this.spectatorManager = spectatorManager;
    }
    
    /**
     * Create a new duel instance (standard 1v1 to 5v5)
     * 
     * @param team1Players Team 1 player UUIDs
     * @param team2Players Team 2 player UUIDs
     * @param kitName Kit name
     * @param ranked Whether duel is ranked
     * @param totalRounds Number of rounds (default 1)
     * @return DuelInstance or null if creation failed
     */
    public DuelInstance createDuel(List<UUID> team1Players, List<UUID> team2Players, 
                                    String kitName, boolean ranked, int totalRounds) {
        return createDuel(team1Players, team2Players, kitName, ranked, totalRounds, "standard");
    }
    
    /**
     * Create a new duel instance with specific mode
     * 
     * @param team1Players Team 1 player UUIDs
     * @param team2Players Team 2 player UUIDs
     * @param kitName Kit name
     * @param ranked Whether duel is ranked
     * @param totalRounds Number of rounds
     * @param duelMode Duel mode ("standard", "boxing", "sumo", "spleef")
     * @return DuelInstance or null if creation failed
     */
    public DuelInstance createDuel(List<UUID> team1Players, List<UUID> team2Players,
                                    String kitName, boolean ranked, int totalRounds, String duelMode) {
        // Validation
        if (team1Players.isEmpty() || team2Players.isEmpty()) {
            logger.warning("Cannot create duel: empty team");
            return null;
        }
        
        // Validate all players online
        for (UUID playerUUID : team1Players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                logger.warning("Cannot create duel: player offline - " + playerUUID);
                return null;
            }
        }
        
        for (UUID playerUUID : team2Players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                logger.warning("Cannot create duel: player offline - " + playerUUID);
                return null;
            }
        }
        
        // Validate no players in active duels
        List<UUID> allPlayers = new ArrayList<>(team1Players);
        allPlayers.addAll(team2Players);
        
        for (UUID playerUUID : allPlayers) {
            if (playerToDuel.containsKey(playerUUID)) {
                logger.warning("Cannot create duel: player already in duel - " + playerUUID);
                return null;
            }
        }
        
        // Get kit
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            logger.warning("Cannot create duel: kit not found - " + kitName);
            return null;
        }
        
        // Reserve arena
        Arena arena = arenaManager.reserveArena(kitName);
        if (arena == null) {
            logger.warning("Cannot create duel: no available arena for kit - " + kitName);
            return null;
        }
        
        // Create duel instance
        UUID duelId = UUID.randomUUID();
        DuelInstance duel = new DuelInstance(
                duelId,
                team1Players,
                team2Players,
                arena,
                kit,
                ranked,
                totalRounds,
                duelMode
        );
        
        // Register duel
        activeDuels.put(duelId, duel);
        
        // Map players to duel
        for (UUID playerUUID : allPlayers) {
            playerToDuel.put(playerUUID, duelId);
        }
        
        logger.info("Created duel " + duelId + " | Mode: " + duelMode + " | Kit: " + kitName + 
                    " | Arena: " + arena.getName() + " | Ranked: " + ranked);
        
        // Begin duel sequence
        beginDuelSequence(duel);
        
        return duel;
    }
    
    /**
     * Create FFA duel (free-for-all)
     * 
     * @param players All players
     * @param kitName Kit name
     * @param ranked Whether ranked
     * @return DuelInstance or null
     */
    public DuelInstance createFFADuel(List<UUID> players, String kitName, boolean ranked) {
        // TODO: Implement FFA duel creation
        // FFA puts all players in individual teams
        logger.warning("FFA duels not yet implemented");
        return null;
    }
    
    /**
     * Begin duel sequence: CREATED → TELEPORTING → COUNTDOWN → COMBAT
     * 
     * @param duel Duel instance
     */
    private void beginDuelSequence(DuelInstance duel) {
        // Transition to TELEPORTING
        if (!duel.setState(DuelState.TELEPORTING)) {
            logger.severe("Failed to transition duel to TELEPORTING: " + duel.getDuelId());
            cancelDuel(duel.getDuelId(), "State transition failed");
            return;
        }
        
        // Teleport players
        teleportPlayers(duel);
    }
    
    /**
     * Teleport all players to arena spawns
     * 
     * @param duel Duel instance
     */
    private void teleportPlayers(DuelInstance duel) {
        Arena arena = duel.getArena();
        Location[] spawnsA = duel.getSpawnPointsA();
        Location[] spawnsB = duel.getSpawnPointsB();
        
        // Load chunks asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Pre-load arena chunks
            for (Location spawn : spawnsA) {
                spawn.getChunk().load();
            }
            for (Location spawn : spawnsB) {
                spawn.getChunk().load();
            }
            
            // Teleport on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                teleportTeam(duel.getTeamA(), spawnsA);
                teleportTeam(duel.getTeamB(), spawnsB);
                
                // Transition to COUNTDOWN
                if (duel.setState(DuelState.COUNTDOWN)) {
                    beginCountdown(duel);
                } else {
                    logger.severe("Failed to transition to COUNTDOWN: " + duel.getDuelId());
                    cancelDuel(duel.getDuelId(), "State transition failed");
                }
            });
        });
    }
    
    /**
     * Teleport team to spawn points
     * 
     * @param team Team to teleport
     * @param spawns Spawn locations
     */
    private void teleportTeam(DuelTeam team, Location[] spawns) {
        List<DuelParticipant> members = new ArrayList<>(team.getMembers());
        
        for (int i = 0; i < members.size(); i++) {
            DuelParticipant participant = members.get(i);
            Player player = participant.getPlayer();
            
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            Location spawn = spawns[Math.min(i, spawns.length - 1)];
            
            // Mark as teleporting
            teleporting.add(player.getUniqueId());
            
            // Prepare player
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().clear();
            player.getActivePotionEffects().clear();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20);
            
            // Teleport
            player.teleport(spawn);
            
            // Freeze player
            participant.setFrozen(true);
            
            // Remove teleporting flag after 3 seconds (fall damage grace)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                teleporting.remove(player.getUniqueId());
            }, 60L); // 3 seconds
        }
    }
    
    /**
     * Begin countdown sequence
     * 
     * @param duel Duel instance
     */
    private void beginCountdown(DuelInstance duel) {
        int countdownSeconds = duel.getSettings().getRoundCountdown();
        
        // TODO: Use DuelTimer utility class for countdown
        // For now, simple implementation
        
        duel.broadcast(ChatColor.YELLOW + "Duel starting in " + countdownSeconds + " seconds...");
        
        // Start countdown
        new Runnable() {
            int remaining = countdownSeconds;
            
            @Override
            public void run() {
                if (remaining > 0) {
                    duel.broadcast(ChatColor.GOLD + "" + remaining + "...");
                    // TODO: Play sound
                    remaining--;
                    
                    // Schedule next tick
                    Bukkit.getScheduler().runTaskLater(plugin, this, 20L);
                } else {
                    // Countdown complete
                    duel.broadcast(ChatColor.GREEN + "GO!");
                    // TODO: Play gong sound
                    
                    startCombat(duel);
                }
            }
        }.run();
    }
    
    /**
     * Start combat phase
     * 
     * @param duel Duel instance
     */
    private void startCombat(DuelInstance duel) {
        // Transition to COMBAT
        if (!duel.setState(DuelState.COMBAT)) {
            logger.severe("Failed to transition to COMBAT: " + duel.getDuelId());
            cancelDuel(duel.getDuelId(), "State transition failed");
            return;
        }
        
        // Unfreeze all players
        for (DuelParticipant participant : duel.getParticipants()) {
            participant.setFrozen(false);
        }
        
        // Apply kits
        applyKits(duel);
        
        // TODO: Initialize round controller
        // TODO: Initialize special mode controllers (boxing, sumo, spleef)
        // TODO: Initialize combat controllers (damage, knockback, health)
        
        logger.info("Duel " + duel.getDuelId() + " entered COMBAT state");
    }
    
    /**
     * Apply kit loadouts to all players
     * 
     * @param duel Duel instance
     */
    private void applyKits(DuelInstance duel) {
        Kit kit = duel.getKit();
        
        for (DuelParticipant participant : duel.getParticipants()) {
            Player player = participant.getPlayer();
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Apply kit (from Section 3)
            kitManager.applyKit(player, kit);
            
            // Apply team-colored armor
            // TODO: Dye armor based on team color (red/blue)
        }
    }
    
    /**
     * End duel and perform cleanup
     * 
     * @param duelId Duel ID
     * @param winner Winning team
     * @param reason End reason
     */
    public void endDuel(UUID duelId, DuelTeam winner, String reason) {
        DuelInstance duel = activeDuels.get(duelId);
        if (duel == null) {
            return;
        }
        
        // Transition to FINISHED
        if (!duel.setState(DuelState.FINISHED)) {
            logger.warning("Failed to transition to FINISHED: " + duelId);
        }
        
        duel.setDuelWinner(winner);
        
        // Announce winner
        String winnerName = winner != null ? winner.getFormattedName() : ChatColor.GRAY + "No one";
        duel.broadcastAll(ChatColor.GOLD + "========================================");
        duel.broadcastAll(ChatColor.GREEN + "Winner: " + winnerName);
        duel.broadcastAll(ChatColor.GRAY + "Reason: " + reason);
        duel.broadcastAll(ChatColor.GOLD + "========================================");
        
        // TODO: Update stats via StatsManager
        // TODO: Update ELO via EloManager (if ranked)
        
        // Teleport to lobby
        teleportToLobby(duel);
        
        // Cleanup
        cleanup(duel);
        
        logger.info("Duel " + duelId + " ended | Winner: " + 
                    (winner != null ? winner.getTeamId() : "none") + " | Reason: " + reason);
    }
    
    /**
     * Cancel duel (emergency abort)
     * 
     * @param duelId Duel ID
     * @param reason Cancellation reason
     */
    public void cancelDuel(UUID duelId, String reason) {
        DuelInstance duel = activeDuels.get(duelId);
        if (duel == null) {
            return;
        }
        
        duel.setState(DuelState.CANCELLED);
        duel.broadcast(ChatColor.RED + "Duel cancelled: " + reason);
        
        // Teleport to lobby
        teleportToLobby(duel);
        
        // Cleanup
        cleanup(duel);
        
        logger.warning("Duel " + duelId + " cancelled: " + reason);
    }
    
    /**
     * Teleport all players to lobby
     * 
     * @param duel Duel instance
     */
    private void teleportToLobby(DuelInstance duel) {
        Location lobby = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        // TODO: Get lobby location from config
        
        for (Player player : duel.getOnlinePlayers()) {
            player.teleport(lobby);
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.getActivePotionEffects().clear();
        }
        
        // Teleport spectators
        for (UUID spectatorUUID : duel.getSpectators()) {
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null && spectator.isOnline()) {
                spectator.teleport(lobby);
                spectator.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
    
    /**
     * Cleanup duel and free resources
     * 
     * @param duel Duel instance
     */
    private void cleanup(DuelInstance duel) {
        UUID duelId = duel.getDuelId();
        
        // Transition to CLEANUP
        duel.setState(DuelState.CLEANUP);
        
        // Remove all spectators
        if (spectatorManager != null) {
            spectatorManager.handleDuelEnd(duelId);
        }
        
        // Remove player mappings
        for (DuelParticipant participant : duel.getParticipants()) {
            playerToDuel.remove(participant.getPlayerUUID());
        }
        
        // Remove party mapping (if applicable)
        // TODO: Remove party mapping when party integration complete
        
        // Free arena
        arenaManager.releaseArena(duel.getArena());
        
        // Remove duel instance
        activeDuels.remove(duelId);
        
        logger.info("Cleaned up duel " + duelId);
    }
    
    /**
     * Get duel instance by ID
     * 
     * @param duelId Duel ID
     * @return DuelInstance or null
     */
    public DuelInstance getDuel(UUID duelId) {
        return activeDuels.get(duelId);
    }
    
    /**
     * Get player's active duel
     * 
     * @param playerUUID Player UUID
     * @return DuelInstance or null
     */
    public DuelInstance getPlayerDuel(UUID playerUUID) {
        UUID duelId = playerToDuel.get(playerUUID);
        return duelId != null ? activeDuels.get(duelId) : null;
    }
    
    /**
     * Check if player is in active duel
     * 
     * @param playerUUID Player UUID
     * @return true if in duel
     */
    public boolean isInDuel(UUID playerUUID) {
        return playerToDuel.containsKey(playerUUID);
    }
    
    /**
     * Check if player is currently teleporting
     * 
     * @param playerUUID Player UUID
     * @return true if teleporting
     */
    public boolean isTeleporting(UUID playerUUID) {
        return teleporting.contains(playerUUID);
    }
    
    /**
     * Get all active duels
     * 
     * @return Unmodifiable collection of duels
     */
    public Collection<DuelInstance> getActiveDuels() {
        return Collections.unmodifiableCollection(activeDuels.values());
    }
    
    /**
     * Get active duel count
     * 
     * @return Number of active duels
     */
    public int getActiveDuelCount() {
        return activeDuels.size();
    }
    
    /**
     * Start cleanup task
     */
    private void startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Check for disconnected players with grace period expired
            long now = System.currentTimeMillis();
            
            for (DuelInstance duel : activeDuels.values()) {
                for (DuelParticipant participant : duel.getParticipants()) {
                    if (!participant.isOnline()) {
                        // TODO: Implement disconnect grace period logic
                        // If offline longer than grace period, forfeit
                    }
                }
            }
        }, cleanupInterval, cleanupInterval);
    }
    
    /**
     * Shutdown manager
     */
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        // Cancel all active duels
        for (UUID duelId : new ArrayList<>(activeDuels.keySet())) {
            cancelDuel(duelId, "Server shutdown");
        }
        
        logger.info("DuelManager shutdown complete");
    }
}
