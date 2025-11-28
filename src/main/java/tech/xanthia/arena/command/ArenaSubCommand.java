package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Base interface for arena subcommands.
 * 
 * All arena subcommands must implement this interface to provide
 * consistent structure and tab completion support.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ArenaSubCommand {
    
    /**
     * Gets the name of this subcommand.
     * 
     * @return Subcommand name
     */
    String getName();
    
    /**
     * Gets the description of this subcommand.
     * 
     * @return Description
     */
    String getDescription();
    
    /**
     * Gets the usage syntax for this subcommand.
     * 
     * @return Usage string
     */
    String getUsage();
    
    /**
     * Gets the permission required to use this subcommand.
     * 
     * @return Permission node
     */
    String getPermission();
    
    /**
     * Gets aliases for this subcommand.
     * 
     * @return List of aliases
     */
    default List<String> getAliases() {
        return Collections.emptyList();
    }
    
    /**
     * Executes the subcommand.
     * 
     * @param sender Command sender
     * @param args Command arguments (excluding subcommand name)
     */
    void execute(CommandSender sender, String[] args);
    
    /**
     * Provides tab completion for this subcommand.
     * 
     * @param sender Command sender
     * @param args Current arguments
     * @return List of completion options
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
