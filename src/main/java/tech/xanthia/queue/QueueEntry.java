package tech.xanthia.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single queue entry (player or party).
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueEntry {
    
    private final UUID entryId;
    private final QueueType queueType;
    private final String kitName;
    private final List<UUID> players;
    private final long queueTime;
    private int mmrExpansion;
    
    public QueueEntry(UUID entryId, QueueType queueType, String kitName, List<UUID> players) {
        this.entryId = entryId;
        this.queueType = queueType;
        this.kitName = kitName;
        this.players = new ArrayList<>(players);
        this.queueTime = System.currentTimeMillis();
        this.mmrExpansion = 0;
    }
    
    public UUID getEntryId() {
        return entryId;
    }
    
    public QueueType getQueueType() {
        return queueType;
    }
    
    public String getKitName() {
        return kitName;
    }
    
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public long getQueueTime() {
        return queueTime;
    }
    
    public long getQueueDuration() {
        return System.currentTimeMillis() - queueTime;
    }
    
    public int getMmrExpansion() {
        return mmrExpansion;
    }
    
    public void expandMmrRange() {
        this.mmrExpansion += 50; // Expand by 50 MMR every interval
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean containsPlayer(UUID playerUUID) {
        return players.contains(playerUUID);
    }
}
