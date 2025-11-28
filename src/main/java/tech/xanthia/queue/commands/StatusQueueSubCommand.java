package tech.xanthia.queue.commands;

import org.bukkit.entity.Player;
import tech.xanthia.queue.QueueEntry;
import tech.xanthia.queue.QueueManager;
import tech.xanthia.queue.RankedQueueEntry;

/**
 * /queue status - Shows current queue status.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusQueueSubCommand implements QueueSubCommand {
    
    private final QueueManager queueManager;
    
    public StatusQueueSubCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }
    
    @Override
    public String getName() {
        return "status";
    }
    
    @Override
    public String getDescription() {
        return "View queue status";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        QueueEntry entry = queueManager.getQueueEntry(player.getUniqueId());
        
        if (entry == null) {
            player.sendMessage("§cYou are not in a queue!");
            return;
        }
        
        player.sendMessage("§6§l=== Queue Status ===");
        player.sendMessage("§eQueue Type: §f" + entry.getQueueType().getDisplayName());
        player.sendMessage("§eKit: §f" + entry.getKitName());
        player.sendMessage("§eTime in Queue: §f" + formatTime(entry.getQueueDuration()));
        player.sendMessage("§ePlayers: §f" + entry.getPlayerCount());
        
        if (entry instanceof RankedQueueEntry) {
            RankedQueueEntry ranked = (RankedQueueEntry) entry;
            player.sendMessage("§eAverage MMR: §f" + ranked.getAverageMMR());
            
            int[] range = ranked.getCurrentMMRRange();
            player.sendMessage("§eMMR Range: §f" + range[0] + " - " + range[1]);
        }
        
        player.sendMessage("§7Searching for match...");
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        
        return seconds + "s";
    }
}
