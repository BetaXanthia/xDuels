package tech.xanthia.queue;

import tech.xanthia.XDuels;

import java.util.List;

/**
 * Matchmaking system for queues.
 * 
 * Runs every tick (1 second) to find matches with O(n) performance.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueMatchmaker {
    
    private final QueueManager queueManager;
    private final XDuels plugin;
    
    // MMR expansion every 10 seconds
    private static final long MMR_EXPAND_INTERVAL = 10000L;
    
    public QueueMatchmaker(QueueManager queueManager, XDuels plugin) {
        this.queueManager = queueManager;
        this.plugin = plugin;
    }
    
    /**
     * Main matchmaking tick - runs every 1 second.
     */
    public void tick() {
        for (QueueType type : QueueType.values()) {
            if (type.isPartyQueue()) {
                matchPartyQueues(type);
            } else {
                matchStandardQueues(type);
            }
        }
    }
    
    /**
     * Matches standard queues (1v1, 2v2, etc.).
     * 
     * @param queueType Queue type
     */
    private void matchStandardQueues(QueueType queueType) {
        List<QueueEntry> entries = queueManager.getQueueEntries(queueType);
        
        if (entries.size() < 2) {
            return;
        }
        
        // Expand MMR for old entries
        for (QueueEntry entry : entries) {
            if (entry.getQueueDuration() % MMR_EXPAND_INTERVAL == 0) {
                entry.expandMmrRange();
            }
        }
        
        // Match entries
        for (int i = 0; i < entries.size() - 1; i++) {
            QueueEntry entry1 = entries.get(i);
            
            for (int j = i + 1; j < entries.size(); j++) {
                QueueEntry entry2 = entries.get(j);
                
                // Check kit match
                if (!entry1.getKitName().equals(entry2.getKitName())) {
                    continue;
                }
                
                // Check MMR compatibility for ranked
                if (queueType.isRanked()) {
                    RankedQueueEntry ranked1 = (RankedQueueEntry) entry1;
                    RankedQueueEntry ranked2 = (RankedQueueEntry) entry2;
                    
                    if (!ranked1.isMMRCompatible(ranked2)) {
                        continue;
                    }
                }
                
                // Match found!
                createMatch(entry1, entry2);
                return; // Exit after one match per tick
            }
        }
    }
    
    /**
     * Matches party queues.
     * 
     * @param queueType Queue type
     */
    private void matchPartyQueues(QueueType queueType) {
        List<QueueEntry> entries = queueManager.getQueueEntries(queueType);
        
        if (entries.size() < 2) {
            return;
        }
        
        // For party FFA, need only 1 party
        if (queueType == QueueType.PARTY_FFA && entries.size() >= 1) {
            createFFAMatch(entries.get(0));
            return;
        }
        
        // For party vs party, match two parties of similar size
        for (int i = 0; i < entries.size() - 1; i++) {
            QueueEntry entry1 = entries.get(i);
            
            for (int j = i + 1; j < entries.size(); j++) {
                QueueEntry entry2 = entries.get(j);
                
                // Check kit match
                if (!entry1.getKitName().equals(entry2.getKitName())) {
                    continue;
                }
                
                // Check size compatibility (within 2 players)
                int sizeDiff = Math.abs(entry1.getPlayerCount() - entry2.getPlayerCount());
                if (sizeDiff <= 2) {
                    createMatch(entry1, entry2);
                    return;
                }
            }
        }
    }
    
    /**
     * Creates a match between two entries.
     * 
     * @param entry1 First entry
     * @param entry2 Second entry
     */
    private void createMatch(QueueEntry entry1, QueueEntry entry2) {
        // Remove from queue
        queueManager.removeEntry(entry1);
        queueManager.removeEntry(entry2);
        
        // TODO: Create duel through DuelManager
        // DuelManager will handle:
        // - Arena selection
        // - Kit application
        // - Teleportation
        // - Party state transition to FIGHTING
        // - ELO calculation for ranked matches
    }
    
    /**
     * Creates an FFA match for a party.
     * 
     * @param entry Party entry
     */
    private void createFFAMatch(QueueEntry entry) {
        // Remove from queue
        queueManager.removeEntry(entry);
        
        // TODO: Create FFA duel through DuelManager
    }
}
