package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena delete <arenaId>
 * 
 * Permanently deletes an arena and its associated data.
 * Cannot delete arenas that are currently in use.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandDelete implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandDelete(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "delete";
    }
    
    @Override
    public String getDescription() {
        return "Delete an arena";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena delete <arenaId>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.delete";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("remove", "del");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }
        
        String arenaId = args[0];
        Arena arena = plugin.getArenaManager().getArena(arenaId);
        
        if (arena == null) {
            sender.sendMessage(plugin.getMessage("arena.not-found", "§cArena {arena} was not found!")
                .replace("{arena}", arenaId));
            return;
        }
        
        if (arena.isInUse()) {
            sender.sendMessage(plugin.getMessage("arena.in-use", "§cArena {arena} is currently in use!")
                .replace("{arena}", arena.getDisplayName()));
            return;
        }
        
        boolean success = plugin.getArenaManager().deleteArena(arenaId);
        
        if (success) {
            sender.sendMessage(plugin.getMessage("arena.deleted", "§aDeleted arena {arena}.")
                .replace("{arena}", arena.getDisplayName()));
            
            // Delete schematic if exists
            plugin.getRegenerationManager().deleteSchematic(arena);
        } else {
            sender.sendMessage("§cFailed to delete arena. Check console for errors.");
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            plugin.getArenaManager().getAllArenas().forEach(arena -> {
                if (arena.getId().toLowerCase().startsWith(partial)) {
                    completions.add(arena.getId());
                }
            });
        }
        
        return completions;
    }
}
