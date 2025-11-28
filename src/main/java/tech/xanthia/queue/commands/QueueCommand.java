package tech.xanthia.queue.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tech.xanthia.kits.KitManager;
import tech.xanthia.queue.QueueManager;

import java.util.*;

/**
 * Root queue command with subcommand routing.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueueCommand implements CommandExecutor, TabCompleter {
    
    private final QueueManager queueManager;
    private final Map<String, QueueSubCommand> subCommands;
    
    public QueueCommand(QueueManager queueManager, KitManager kitManager) {
        this.queueManager = queueManager;
        this.subCommands = new HashMap<>();
        
        // Register subcommands
        registerSubCommand(new JoinQueueSubCommand(queueManager, kitManager));
        registerSubCommand(new LeaveQueueSubCommand(queueManager));
        registerSubCommand(new StatusQueueSubCommand(queueManager));
        registerSubCommand(new ListQueueSubCommand(queueManager));
    }
    
    private void registerSubCommand(QueueSubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommandName = args[0].toLowerCase();
        QueueSubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            player.sendMessage("§cUnknown subcommand! Use /queue help for a list of commands.");
            return true;
        }
        
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(player, subArgs);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        
        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String subCommandName : new TreeSet<>(subCommands.keySet())) {
                if (input.isEmpty() || subCommandName.startsWith(input)) {
                    completions.add(subCommandName);
                }
            }
            return completions;
        }
        
        String subCommandName = args[0].toLowerCase();
        QueueSubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.tabComplete(player, subArgs);
        }
        
        return Collections.emptyList();
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Queue Commands ===");
        player.sendMessage("§e/queue join <type> <kit> §7- Join a queue");
        player.sendMessage("§e/queue leave §7- Leave the queue");
        player.sendMessage("§e/queue status §7- View queue status");
        player.sendMessage("§e/queue list §7- List all queues");
    }
}
