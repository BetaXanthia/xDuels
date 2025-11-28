package tech.xanthia.queue.commands;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Interface for queue subcommands.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public interface QueueSubCommand {
    
    String getName();
    
    String getDescription();
    
    default List<String> getAliases() {
        return Collections.emptyList();
    }
    
    void execute(Player player, String[] args);
    
    default List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
