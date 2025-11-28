package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena group <arenaId> <groupId|none>
 * 
 * Assigns an arena to a group or removes it from its current group.
 * Use "none" to remove the arena from any group.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandGroup implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandGroup(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "group";
    }
    
    @Override
    public String getDescription() {
        return "Assign arena to a group";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena group <arenaId> <groupId|none>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.group";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("setgroup", "assigngroup");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }
        
        String arenaId = args[0];
        String groupId = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaId);
        
        if (arena == null) {
            sender.sendMessage(plugin.getMessage("arena.not-found", "§cArena {arena} was not found!")
                .replace("{arena}", arenaId));
            return;
        }
        
        // Remove from group
        if (groupId.equalsIgnoreCase("none") || groupId.equalsIgnoreCase("null")) {
            boolean success = plugin.getArenaManager().removeArenaFromGroup(arenaId);
            
            if (success) {
                sender.sendMessage(plugin.getMessage("arena.group-removed", "§aRemoved {arena} from its group.")
                    .replace("{arena}", arena.getDisplayName()));
            } else {
                sender.sendMessage("§cArena is not in any group.");
            }
            return;
        }
        
        // Check if group exists, create if it doesn't
        ArenaGroup group = plugin.getArenaManager().getGroup(groupId);
        if (group == null) {
            sender.sendMessage("§eGroup '" + groupId + "' doesn't exist. Creating...");
            group = plugin.getArenaManager().createGroup(groupId, groupId);
            
            if (group == null) {
                sender.sendMessage("§cFailed to create group.");
                return;
            }
        }
        
        // Assign to group
        boolean success = plugin.getArenaManager().assignArenaToGroup(arenaId, groupId);
        
        if (success) {
            sender.sendMessage(plugin.getMessage("arena.group-assigned", "§aAssigned {arena} to {group}.")
                .replace("{arena}", arena.getDisplayName())
                .replace("{group}", group.getDisplayName()));
        } else {
            sender.sendMessage("§cFailed to assign arena to group.");
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
            String partial = args[1].toLowerCase();
            completions.add("none");
            plugin.getArenaManager().getAllGroups().forEach(group -> {
                if (group.getGroupId().toLowerCase().startsWith(partial)) {
                    completions.add(group.getGroupId());
                }
            });
        }
        
        return completions;
    }
}
