package tech.xanthia.queue.utils;

import tech.xanthia.queue.QueueEntry;
import tech.xanthia.queue.QueueManager;

/**
 * Handles queue timeout and cleanup.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueTimeoutHandler {
    
    private static final long MAX_QUEUE_TIME = 5 * 60 * 1000; // 5 minutes
    
    private final QueueManager queueManager;
    
    public QueueTimeoutHandler(QueueManager queueManager) {
        this.queueManager = queueManager;
    }
    
    /**
     * Checks if an entry has timed out.
     * 
     * @param entry Queue entry
     * @return True if timed out
     */
    public boolean hasTimedOut(QueueEntry entry) {
        return entry.getQueueDuration() >= MAX_QUEUE_TIME;
    }
    
    /**
     * Removes timed out entries from all queues.
     * 
     * @return Number of entries removed
     */
    public int removeTimedOutEntries() {
        int removed = 0;
        
        for (tech.xanthia.queue.QueueType type : tech.xanthia.queue.QueueType.values()) {
            for (QueueEntry entry : queueManager.getQueueEntries(type)) {
                if (hasTimedOut(entry)) {
                    queueManager.removeEntry(entry);
                    removed++;
                    
                    // Notify players
                    entry.getPlayers().forEach(uuid -> {
                        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.sendMessage("§cQueue timed out after 5 minutes!");
                        }
                    });
                }
            }
        }
        
        return removed;
    }
}
