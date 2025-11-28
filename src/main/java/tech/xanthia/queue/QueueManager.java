package tech.xanthia.queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central queue management system.
 * 
 * Handles all queue operations with O(1) player lookups and O(n) matchmaking.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueManager {
    
    private final XDuels plugin;
    private final PartyManager partyManager;
    
    // Queue storage - separate lists per type for fast iteration
    private final Map<QueueType, List<QueueEntry>> queues;
    
    // Player/Party -> Queue Entry for O(1) lookup
    private final Map<UUID, QueueEntry> playerToEntry;
    private final Map<UUID, QueueEntry> partyToEntry;
    
    // Matchmaker
    private final QueueMatchmaker matchmaker;
    
    private int matchmakingTaskId = -1;
    
    public QueueManager(XDuels plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.queues = new ConcurrentHashMap<>();
        this.playerToEntry = new ConcurrentHashMap<>();
        this.partyToEntry = new ConcurrentHashMap<>();
        this.matchmaker = new QueueMatchmaker(this, plugin);
        
        // Initialize queue lists
        for (QueueType type : QueueType.values()) {
            queues.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Initialize the queue manager and start matchmaking
     */
    public void initialize() {
        // Start matchmaking task - runs every 20 ticks (1 second)
        matchmakingTaskId = Bukkit.getScheduler().runTaskTimer(plugin, matchmaker::tick, 20L, 20L).getTaskId();
    }
    
    /**
     * Shutdown the queue manager
     */
    public void shutdown() {
        if (matchmakingTaskId != -1) {
            Bukkit.getScheduler().cancelTask(matchmakingTaskId);
        }
        queues.clear();
        playerToEntry.clear();
        partyToEntry.clear();
    }
    
    /**
     * Adds a solo player to queue.
     * 
     * @param playerUUID Player UUID
     * @param queueType Queue type
     * @param kitName Kit name
     * @return True if added
     */
    public boolean addToQueue(UUID playerUUID, QueueType queueType, String kitName) {
        // Validation
        if (isInQueue(playerUUID)) {
            return false;
        }
        
        if (partyManager.isInParty(playerUUID)) {
            return false; // Use party queue instead
        }
        
        // Create entry
        QueueEntry entry;
        if (queueType.isRanked()) {
            int mmr = 1000; // TODO: Get from stats manager
            entry = new RankedQueueEntry(UUID.randomUUID(), queueType, kitName, 
                List.of(playerUUID), mmr, mmr, mmr);
        } else {
            entry = new QueueEntry(UUID.randomUUID(), queueType, kitName, 
                List.of(playerUUID));
        }
        
        // Add to queue
        queues.get(queueType).add(entry);
        playerToEntry.put(playerUUID, entry);
        
        // Notify
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage("§aYou have joined the " + queueType.getDisplayName() + 
                " queue for kit §e" + kitName + "§a!");
        }
        
        return true;
    }
    
    /**
     * Adds a party to queue.
     * 
     * @param partyUUID Party UUID
     * @param queueType Queue type
     * @param kitName Kit name
     * @return True if added
     */
    public boolean addPartyToQueue(UUID partyUUID, QueueType queueType, String kitName) {
        Party party = partyManager.getParty(partyUUID);
        if (party == null || !party.getCurrentState().canQueue()) {
            return false;
        }
        
        // Check if anyone in party is already in queue
        for (UUID memberUUID : party.getMemberUUIDs()) {
            if (isInQueue(memberUUID)) {
                return false;
            }
        }
        
        // Transition party state
        party.setState(PartyState.QUEUING);
        
        // Create entry
        List<UUID> players = new ArrayList<>(party.getMemberUUIDs());
        QueueEntry entry;
        
        if (queueType.isRanked()) {
            // Calculate party MMR
            int totalMMR = 0;
            int minMMR = Integer.MAX_VALUE;
            int maxMMR = Integer.MIN_VALUE;
            
            for (UUID playerUUID : players) {
                int mmr = 1000; // TODO: Get from stats manager
                totalMMR += mmr;
                minMMR = Math.min(minMMR, mmr);
                maxMMR = Math.max(maxMMR, mmr);
            }
            
            int avgMMR = totalMMR / players.size();
            entry = new RankedQueueEntry(partyUUID, queueType, kitName, players, 
                avgMMR, minMMR, maxMMR);
        } else {
            entry = new QueueEntry(partyUUID, queueType, kitName, players);
        }
        
        // Add to queue
        queues.get(queueType).add(entry);
        partyToEntry.put(partyUUID, entry);
        
        for (UUID playerUUID : players) {
            playerToEntry.put(playerUUID, entry);
        }
        
        // Notify party
        party.broadcast("§aYour party has joined the " + queueType.getDisplayName() + 
            " queue for kit §e" + kitName + "§a!");
        
        return true;
    }
    
    /**
     * Removes a player from queue.
     * 
     * @param playerUUID Player UUID
     * @return True if removed
     */
    public boolean removeFromQueue(UUID playerUUID) {
        QueueEntry entry = playerToEntry.remove(playerUUID);
        if (entry == null) {
            return false;
        }
        
        // Remove from queue list
        queues.get(entry.getQueueType()).remove(entry);
        
        // If party, remove all members and reset state
        if (entry.getPlayerCount() > 1) {
            for (UUID member : entry.getPlayers()) {
                playerToEntry.remove(member);
            }
            
            partyToEntry.remove(entry.getEntryId());
            
            Party party = partyManager.getParty(entry.getEntryId());
            if (party != null) {
                party.setState(PartyState.IDLE);
                party.broadcast("§cYour party has left the queue!");
            }
        } else {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage("§cYou have left the queue!");
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a player is in any queue.
     * 
     * @param playerUUID Player UUID
     * @return True if in queue
     */
    public boolean isInQueue(UUID playerUUID) {
        return playerToEntry.containsKey(playerUUID);
    }
    
    /**
     * Gets a player's current queue entry.
     * 
     * @param playerUUID Player UUID
     * @return Queue entry or null
     */
    public QueueEntry getQueueEntry(UUID playerUUID) {
        return playerToEntry.get(playerUUID);
    }
    
    /**
     * Gets all entries for a queue type.
     * 
     * @param queueType Queue type
     * @return List of entries
     */
    public List<QueueEntry> getQueueEntries(QueueType queueType) {
        return new ArrayList<>(queues.get(queueType));
    }
    
    /**
     * Gets total queue size for a type.
     * 
     * @param queueType Queue type
     * @return Queue size
     */
    public int getQueueSize(QueueType queueType) {
        return queues.get(queueType).size();
    }
    
    /**
     * Removes an entry (called by matchmaker).
     * 
     * @param entry Entry to remove
     */
    public void removeEntry(QueueEntry entry) {
        queues.get(entry.getQueueType()).remove(entry);
        
        for (UUID playerUUID : entry.getPlayers()) {
            playerToEntry.remove(playerUUID);
        }
        
        if (entry.getPlayerCount() > 1) {
            partyToEntry.remove(entry.getEntryId());
        }
    }
}
