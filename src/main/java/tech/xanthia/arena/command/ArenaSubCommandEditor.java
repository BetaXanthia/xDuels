package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.gui.ArenaEditorGUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena editor [arenaId]
 * 
 * Opens the arena editor GUI for the specified arena.
 * If no arena is specified, opens the arena selector.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandEditor implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandEditor(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "editor";
    }
    
    @Override
    public String getDescription() {
        return "Open arena editor";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena editor [arenaId]";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.editor";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("edit", "configure");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open arena selector
            ArenaEditorGUI gui = new ArenaEditorGUI(plugin);
            gui.openArenaSelector(player);
            return;
        }
        
        String arenaId = args[0];
        Arena arena = plugin.getArenaManager().getArena(arenaId);
        
        if (arena == null) {
            player.sendMessage(plugin.getMessage("arena.not-found", "§cArena {arena} was not found!")
                .replace("{arena}", arenaId));
            return;
        }
        
        // Check if arena is in use
        if (arena.isInUse()) {
            player.sendMessage(plugin.getMessage("arena.in-use", "§cArena {arena} is currently in use!")
                .replace("{arena}", arena.getDisplayName()));
            return;
        }
        
        // Open editor GUI
        ArenaEditorGUI gui = new ArenaEditorGUI(plugin);
        gui.openEditor(player, arena);
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
