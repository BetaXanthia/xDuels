package tech.xanthia.queue.commands;

import org.bukkit.entity.Player;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitManager;
import tech.xanthia.queue.QueueManager;
import tech.xanthia.queue.QueueType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /queue join <type> <kit> - Joins a queue.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class JoinQueueSubCommand implements QueueSubCommand {
    
    private final QueueManager queueManager;
    private final KitManager kitManager;
    
    public JoinQueueSubCommand(QueueManager queueManager, KitManager kitManager) {
        this.queueManager = queueManager;
        this.kitManager = kitManager;
    }
    
    @Override
    public String getName() {
        return "join";
    }
    
    @Override
    public String getDescription() {
        return "Join a queue";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /queue join <type> <kit>");
            player.sendMessage("§7Types: 1v1, 2v2, 3v3, 4v4, 5v5 (add -ranked for ranked)");
            return;
        }
        
        if (queueManager.isInQueue(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a queue!");
            return;
        }
        
        String typeStr = args[0].toUpperCase();
        String kitName = args[1];
        
        // Parse queue type
        boolean ranked = typeStr.endsWith("-RANKED");
        if (ranked) {
            typeStr = typeStr.replace("-RANKED", "");
        }
        
        QueueType queueType;
        try {
            queueType = QueueType.valueOf((ranked ? "RANKED_" : "UNRANKED_") + typeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid queue type! Use: 1v1, 2v2, 3v3, 4v4, or 5v5");
            return;
        }
        
        boolean joined = queueManager.addToQueue(player.getUniqueId(), queueType, kitName);
        
        if (!joined) {
            player.sendMessage("§cFailed to join queue!");
        }
    }
    
    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            List<String> types = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String type : List.of("1v1", "2v2", "3v3", "4v4", "5v5", 
                                       "1v1-ranked", "2v2-ranked", "3v3-ranked", 
                                       "4v4-ranked", "5v5-ranked")) {
                if (type.startsWith(input)) {
                    types.add(type);
                }
            }
            
            return types;
        }
        
        if (args.length == 2) {
            String input = args[1].toLowerCase();
            return kitManager.getEnabledKits().stream()
                .map(Kit::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
        }
        
        return List.of();
    }
}
