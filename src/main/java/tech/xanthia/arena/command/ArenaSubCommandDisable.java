package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena disable <arenaId>
 * 
 * Disables an arena, preventing it from being used in duels.
 * Cannot disable arenas that are currently in use.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandDisable implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandDisable(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "disable";
    }
    
    @Override
    public String getDescription() {
        return "Disable an arena";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena disable <arenaId>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.disable";
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
        
        if (!arena.isEnabled()) {
            sender.sendMessage(plugin.getMessage("arena.already-disabled", "§cArena {arena} is already disabled!")
                .replace("{arena}", arena.getDisplayName()));
            return;
        }
        
        if (arena.isInUse()) {
            sender.sendMessage(plugin.getMessage("arena.in-use", "§cArena {arena} is currently in use!")
                .replace("{arena}", arena.getDisplayName()));
            return;
        }
        
        arena.setEnabled(false);
        plugin.getArenaManager().saveArena(arena);
        
        sender.sendMessage(plugin.getMessage("arena.disabled", "§cArena {arena} disabled.")
            .replace("{arena}", arena.getDisplayName()));
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            plugin.getArenaManager().getAllArenas().forEach(arena -> {
                if (arena.isEnabled() && arena.getId().toLowerCase().startsWith(partial)) {
                    completions.add(arena.getId());
                }
            });
        }
        
        return completions;
    }
}
