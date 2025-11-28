package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena clone <sourceId> <newId> [offset]
 * 
 * Clones an existing arena with a new ID.
 * Optionally applies a position offset to all coordinates.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandClone implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandClone(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "clone";
    }
    
    @Override
    public String getDescription() {
        return "Clone an existing arena";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena clone <sourceId> <newId> [offset]";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.clone";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("copy", "duplicate");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            sender.sendMessage("§7Offset format: x,y,z (e.g., 100,0,50)");
            return;
        }
        
        String sourceId = args[0];
        String newId = args[1];
        
        Arena sourceArena = plugin.getArenaManager().getArena(sourceId);
        
        if (sourceArena == null) {
            sender.sendMessage(plugin.getMessage("arena.not-found", "§cArena {arena} was not found!")
                .replace("{arena}", sourceId));
            return;
        }
        
        if (plugin.getArenaManager().getArena(newId) != null) {
            sender.sendMessage(plugin.getMessage("arena.already-exists", "§cArena {arena} already exists!")
                .replace("{arena}", newId));
            return;
        }
        
        // Parse offset if provided
        Vector offset = new Vector(0, 0, 0);
        if (args.length >= 3) {
            try {
                String[] parts = args[2].split(",");
                if (parts.length == 3) {
                    offset = new Vector(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2])
                    );
                } else {
                    sender.sendMessage("§cInvalid offset format. Use: x,y,z");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid offset values. Must be numbers.");
                return;
            }
        }
        
        Arena clonedArena = plugin.getArenaManager().cloneArena(sourceId, newId, offset);
        
        if (clonedArena != null) {
            sender.sendMessage(plugin.getMessage("arena.cloned", "§aCloned {source} into {clone}.")
                .replace("{source}", sourceArena.getDisplayName())
                .replace("{clone}", newId));
            
            if (offset.length() > 0) {
                sender.sendMessage("§aOffset applied: §e" + offset.getX() + ", " + offset.getY() + ", " + offset.getZ());
            }
        } else {
            sender.sendMessage("§cFailed to clone arena. Check console for errors.");
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
        } else if (args.length == 2) {
            completions.add("<new-arena-id>");
        } else if (args.length == 3) {
            completions.add("0,0,0");
            completions.add("100,0,0");
            completions.add("0,0,100");
        }
        
        return completions;
    }
}
