package tech.xanthia.arena.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;

import java.util.*;

/**
 * Root command handler for /xduels arena
 * 
 * Manages all arena-related subcommands:
 * - create: Create new arenas
 * - editor: Open arena editor GUI
 * - teleport: Teleport to arena locations
 * - enable/disable: Toggle arena availability
 * - delete: Remove arenas
 * - clone: Duplicate arenas
 * - group: Manage arena groups
 * - list: List all arenas
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaCommand implements CommandExecutor, TabCompleter {
    
    private final XDuels plugin;
    private final Map<String, ArenaSubCommand> subCommands;
    
    /**
     * Creates the arena command.
     * 
     * @param plugin Plugin instance
     */
    public ArenaCommand(XDuels plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        
        registerSubCommands();
    }
    
    /**
     * Registers all subcommands.
     */
    private void registerSubCommands() {
        registerSubCommand(new ArenaSubCommandCreate(plugin));
        registerSubCommand(new ArenaSubCommandEditor(plugin));
        registerSubCommand(new ArenaSubCommandTeleport(plugin));
        registerSubCommand(new ArenaSubCommandEnable(plugin));
        registerSubCommand(new ArenaSubCommandDisable(plugin));
        registerSubCommand(new ArenaSubCommandDelete(plugin));
        registerSubCommand(new ArenaSubCommandClone(plugin));
        registerSubCommand(new ArenaSubCommandGroup(plugin));
        registerSubCommand(new ArenaSubCommandList(plugin));
    }
    
    /**
     * Registers a subcommand.
     * 
     * @param subCommand Subcommand to register
     */
    private void registerSubCommand(ArenaSubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("xduels.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission", "§cYou do not have permission to manage arenas."));
            return true;
        }
        
        // Show help if no subcommand provided
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        // Get and execute subcommand
        String subCommandName = args[0].toLowerCase();
        ArenaSubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            sender.sendMessage("§cUnknown arena subcommand: §e" + args[0]);
            return true;
        }
        
        // Check subcommand permission
        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(plugin.getMessage("no-permission", "§cYou do not have permission to use this command."));
            return true;
        }
        
        // Execute subcommand
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length <= 1) {
            // Tab complete subcommand names
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            for (ArenaSubCommand subCommand : new HashSet<>(subCommands.values())) {
                if (sender.hasPermission(subCommand.getPermission()) && 
                    subCommand.getName().toLowerCase().startsWith(partial)) {
                    completions.add(subCommand.getName());
                }
            }
        } else if (args.length > 1) {
            // Tab complete subcommand arguments
            ArenaSubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                completions.addAll(subCommand.tabComplete(sender, subArgs));
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
    
    /**
     * Sends help message showing all available subcommands.
     * 
     * @param sender Command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l╔══════════════════════════════════╗");
        sender.sendMessage("§6§l║     §e§lArena Commands§6§l            ║");
        sender.sendMessage("§6§l╚══════════════════════════════════╝");
        sender.sendMessage("");
        
        Set<ArenaSubCommand> uniqueSubCommands = new HashSet<>(subCommands.values());
        for (ArenaSubCommand subCommand : uniqueSubCommands) {
            if (sender.hasPermission(subCommand.getPermission())) {
                sender.sendMessage("§e/xduels arena " + subCommand.getName() + " §7- §f" + subCommand.getDescription());
            }
        }
        
        sender.sendMessage("");
    }
}
