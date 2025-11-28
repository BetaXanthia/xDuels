package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena enable <arenaId>
 * 
 * Enables a disabled arena, making it available for duels.
 * The arena must pass validation before it can be enabled.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandEnable implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandEnable(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "enable";
    }
    
    @Override
    public String getDescription() {
        return "Enable an arena";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena enable <arenaId>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.enable";
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
        
        if (arena.isEnabled()) {
            sender.sendMessage(plugin.getMessage("arena.already-enabled", "§cArena {arena} is already enabled!")
                .replace("{arena}", arena.getDisplayName()));
            return;
        }
        
        // Validate arena before enabling
        if (!plugin.getArenaManager().isArenaValid(arenaId)) {
            sender.sendMessage(plugin.getMessage("arena.validation-failed", "§cArena {arena} failed validation!")
                .replace("{arena}", arena.getDisplayName()));
            sender.sendMessage("§cReason: " + plugin.getArenaManager().getValidator().getValidationErrors(arena));
            return;
        }
        
        arena.setEnabled(true);
        plugin.getArenaManager().saveArena(arena);
        
        sender.sendMessage(plugin.getMessage("arena.enabled", "§aArena {arena} enabled.")
            .replace("{arena}", arena.getDisplayName()));
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            plugin.getArenaManager().getAllArenas().forEach(arena -> {
                if (!arena.isEnabled() && arena.getId().toLowerCase().startsWith(partial)) {
                    completions.add(arena.getId());
                }
            });
        }
        
        return completions;
    }
}
