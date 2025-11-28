package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena create <id> <displayName>
 * 
 * Creates a new arena with the specified ID and display name.
 * The arena is created in a basic state and requires configuration
 * through the editor before it can be used.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandCreate implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandCreate(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "create";
    }
    
    @Override
    public String getDescription() {
        return "Create a new arena";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena create <id> <displayName>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.create";
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }
        
        String arenaId = args[0];
        String displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        
        // Check if arena already exists
        if (plugin.getArenaManager().getArena(arenaId) != null) {
            sender.sendMessage(plugin.getMessage("arena.already-exists", "§cArena {arena} already exists!")
                .replace("{arena}", arenaId));
            return;
        }
        
        // Create arena
        Arena arena = plugin.getArenaManager().createArena(arenaId, displayName);
        
        if (arena != null) {
            sender.sendMessage(plugin.getMessage("arena.created", "§aArena {arena} ({id}) created successfully!")
                .replace("{arena}", displayName)
                .replace("{id}", arenaId));
            sender.sendMessage("§aUse §e/xduels arena editor " + arenaId + "§a to configure locations.");
        } else {
            sender.sendMessage("§cFailed to create arena. Check console for errors.");
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("<arena-id>");
        } else if (args.length == 2) {
            completions.add("<display-name>");
        }
        
        return completions;
    }
}
