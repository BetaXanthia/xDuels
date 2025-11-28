package tech.xanthia.queue.commands;

import org.bukkit.entity.Player;
import tech.xanthia.queue.QueueManager;
import tech.xanthia.queue.QueueType;

/**
 * /queue list - Lists all queue sizes.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ListQueueSubCommand implements QueueSubCommand {
    
    private final QueueManager queueManager;
    
    public ListQueueSubCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }
    
    @Override
    public String getName() {
        return "list";
    }
    
    @Override
    public String getDescription() {
        return "List all queues";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage("§6§l=== Queue List ===");
        
        player.sendMessage("§e§lUnranked:");
        for (QueueType type : QueueType.values()) {
            if (!type.isRanked() && !type.isPartyQueue()) {
                int size = queueManager.getQueueSize(type);
                player.sendMessage("  §7" + type.getDisplayName() + ": §f" + size + " players");
            }
        }
        
        player.sendMessage("§c§lRanked:");
        for (QueueType type : QueueType.values()) {
            if (type.isRanked()) {
                int size = queueManager.getQueueSize(type);
                player.sendMessage("  §7" + type.getDisplayName() + ": §f" + size + " players");
            }
        }
        
        player.sendMessage("§b§lParty:");
        for (QueueType type : QueueType.values()) {
            if (type.isPartyQueue()) {
                int size = queueManager.getQueueSize(type);
                player.sendMessage("  §7" + type.getDisplayName() + ": §f" + size + " parties");
            }
        }
    }
}
