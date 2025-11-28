package tech.xanthia.queue.commands;

import org.bukkit.entity.Player;
import tech.xanthia.queue.QueueManager;

/**
 * /queue leave - Leaves the current queue.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class LeaveQueueSubCommand implements QueueSubCommand {
    
    private final QueueManager queueManager;
    
    public LeaveQueueSubCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }
    
    @Override
    public String getName() {
        return "leave";
    }
    
    @Override
    public String getDescription() {
        return "Leave the queue";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (!queueManager.isInQueue(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a queue!");
            return;
        }
        
        boolean removed = queueManager.removeFromQueue(player.getUniqueId());
        
        if (removed) {
            player.sendMessage("§aYou left the queue!");
        } else {
            player.sendMessage("§cFailed to leave queue!");
        }
    }
}
