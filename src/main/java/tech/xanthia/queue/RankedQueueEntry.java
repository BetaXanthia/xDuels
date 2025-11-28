package tech.xanthia.queue;

import java.util.List;
import java.util.UUID;

/**
 * Ranked queue entry with MMR tracking.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class RankedQueueEntry extends QueueEntry {
    
    private final int averageMMR;
    private final int minMMR;
    private final int maxMMR;
    
    public RankedQueueEntry(UUID entryId, QueueType queueType, String kitName, 
                            List<UUID> players, int averageMMR, int minMMR, int maxMMR) {
        super(entryId, queueType, kitName, players);
        this.averageMMR = averageMMR;
        this.minMMR = minMMR;
        this.maxMMR = maxMMR;
    }
    
    public int getAverageMMR() {
        return averageMMR;
    }
    
    public int getMinMMR() {
        return minMMR;
    }
    
    public int getMaxMMR() {
        return maxMMR;
    }
    
    /**
     * Gets current MMR search range.
     * 
     * @return [min, max] MMR range
     */
    public int[] getCurrentMMRRange() {
        int expansion = getMmrExpansion();
        return new int[] {
            Math.max(0, minMMR - expansion),
            maxMMR + expansion
        };
    }
    
    /**
     * Checks if this entry can match with another based on MMR.
     * 
     * @param other Other entry
     * @return True if MMR compatible
     */
    public boolean isMMRCompatible(RankedQueueEntry other) {
        int[] thisRange = getCurrentMMRRange();
        int[] otherRange = other.getCurrentMMRRange();
        
        // Check if ranges overlap
        return !(thisRange[1] < otherRange[0] || thisRange[0] > otherRange[1]);
    }
}
