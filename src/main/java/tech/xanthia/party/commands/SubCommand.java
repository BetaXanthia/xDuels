package tech.xanthia.party.commands;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Interface for party subcommands.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SubCommand {
    
    /**
     * Gets the subcommand name.
     * 
     * @return Name
     */
    String getName();
    
    /**
     * Gets the subcommand description.
     * 
     * @return Description
     */
    String getDescription();
    
    /**
     * Gets the subcommand aliases.
     * 
     * @return List of aliases
     */
    default List<String> getAliases() {
        return Collections.emptyList();
    }
    
    /**
     * Executes the subcommand.
     * 
     * @param player Player executing
     * @param args Arguments
     */
    void execute(Player player, String[] args);
    
    /**
     * Tab completion for the subcommand.
     * 
     * @param player Player
     * @param args Arguments
     * @return Completions
     */
    default List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
