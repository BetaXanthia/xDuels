package tech.xanthia.kits.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Interface for kit subcommands.
 * 
 * All kit subcommands must implement this interface.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public interface KitSubCommand {
    
    /**
     * Executes the subcommand.
     * 
     * @param sender Command sender
     * @param args Command arguments (after subcommand name)
     * @return True if command executed successfully
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * Provides tab completion for the subcommand.
     * 
     * @param sender Command sender
     * @param args Current arguments
     * @return List of completion suggestions
     */
    List<String> tabComplete(CommandSender sender, String[] args);
    
    /**
     * Gets the subcommand name.
     * 
     * @return Subcommand name
     */
    String getName();
    
    /**
     * Gets the permission node required for this subcommand.
     * 
     * @return Permission node
     */
    String getPermission();
    
    /**
     * Gets the usage message for this subcommand.
     * 
     * @return Usage message
     */
    String getUsage();
}
