package tech.xanthia.kits.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Root command handler for /xduels kit.
 * 
 * Dispatches to subcommands:
 * - create
 * - delete
 * - edit
 * - editor
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitCommand implements CommandExecutor, TabCompleter {
    
    private final XDuels plugin;
    private final Map<String, KitSubCommand> subCommands;
    
    /**
     * Creates a new kit command handler.
     * 
     * @param plugin Plugin instance
     */
    public KitCommand(XDuels plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        
        // Register subcommands
        registerSubCommand(new KitSubCommandCreate(plugin));
        registerSubCommand(new KitSubCommandDelete(plugin));
        registerSubCommand(new KitSubCommandEdit(plugin));
        registerSubCommand(new KitSubCommandEditor(plugin));
    }
    
    /**
     * Registers a subcommand.
     * 
     * @param subCommand Subcommand to register
     */
    private void registerSubCommand(KitSubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // No args - show help
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        // Get subcommand
        String subCommandName = args[0].toLowerCase();
        KitSubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            sender.sendMessage("§cUnknown subcommand: §e" + subCommandName);
            sendHelp(sender);
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        // Execute subcommand
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            if (!subCommand.execute(sender, subArgs)) {
                sender.sendMessage("§cUsage: §e" + subCommand.getUsage());
            }
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred while executing this command.");
            plugin.getLogger().severe("[ KitCommand ] Error executing " + subCommandName + " - Error: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // First arg - subcommand names
        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0].toLowerCase();
            return subCommands.values().stream()
                .filter(sub -> sender.hasPermission(sub.getPermission()))
                .map(KitSubCommand::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
        }
        
        // Delegate to subcommand
        if (args.length > 1) {
            KitSubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subArgs);
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Sends help message to sender.
     * 
     * @param sender Command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lxDuels Kit Commands:");
        sender.sendMessage("");
        
        for (KitSubCommand subCommand : subCommands.values()) {
            if (sender.hasPermission(subCommand.getPermission())) {
                sender.sendMessage("§e" + subCommand.getUsage());
            }
        }
    }
}
