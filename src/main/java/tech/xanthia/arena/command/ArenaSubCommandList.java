package tech.xanthia.arena.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Subcommand: /xduels arena list [filter]
 * 
 * Lists all arenas with their status.
 * Optionally filter by: enabled, disabled, available, inuse, group:<groupId>
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandList implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandList(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "list";
    }
    
    @Override
    public String getDescription() {
        return "List all arenas";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena list [filter]";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.list";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("ls", "show");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getAllArenas());
        
        // Apply filter if provided
        String filter = args.length > 0 ? args[0].toLowerCase() : "all";
        
        switch (filter) {
            case "enabled":
                arenas = arenas.stream().filter(Arena::isEnabled).collect(Collectors.toList());
                break;
            case "disabled":
                arenas = arenas.stream().filter(a -> !a.isEnabled()).collect(Collectors.toList());
                break;
            case "available":
                arenas = arenas.stream().filter(Arena::isAvailable).collect(Collectors.toList());
                break;
            case "inuse":
                arenas = arenas.stream().filter(Arena::isInUse).collect(Collectors.toList());
                break;
            default:
                if (filter.startsWith("group:")) {
                    String groupId = filter.substring(6);
                    arenas = arenas.stream()
                        .filter(a -> groupId.equals(a.getGroupId()))
                        .collect(Collectors.toList());
                }
                break;
        }
        
        if (arenas.isEmpty()) {
            sender.sendMessage("§cNo arenas found" + (filter.equals("all") ? "." : " matching filter: " + filter));
            return;
        }
        
        // Header
        sender.sendMessage("§6§l╔══════════════════════════════════════════╗");
        sender.sendMessage("§6§l║        §e§lArena List§6§l (" + arenas.size() + ")              ║");
        sender.sendMessage("§6§l╚══════════════════════════════════════════╝");
        sender.sendMessage("");
        
        // Group arenas by their group
        Map<String, List<Arena>> byGroup = arenas.stream()
            .collect(Collectors.groupingBy(a -> a.hasGroup() ? a.getGroupId() : ""));
        
        // List standalone arenas first
        if (byGroup.containsKey("")) {
            sender.sendMessage("§e§lStandalone Arenas:");
            for (Arena arena : byGroup.get("")) {
                sendArenaInfo(sender, arena);
            }
            sender.sendMessage("");
        }
        
        // List grouped arenas
        for (Map.Entry<String, List<Arena>> entry : byGroup.entrySet()) {
            if (entry.getKey().isEmpty()) continue;
            
            ArenaGroup group = plugin.getArenaManager().getGroup(entry.getKey());
            String groupName = group != null ? group.getDisplayName() : entry.getKey();
            
            sender.sendMessage("§e§lGroup: " + groupName);
            for (Arena arena : entry.getValue()) {
                sendArenaInfo(sender, arena);
            }
            sender.sendMessage("");
        }
        
        // Footer with statistics
        int total = plugin.getArenaManager().getArenaCount();
        int enabled = (int) plugin.getArenaManager().getAllArenas().stream().filter(Arena::isEnabled).count();
        int available = plugin.getArenaManager().getAvailableArenaCount();
        int groups = plugin.getArenaManager().getGroupCount();
        
        sender.sendMessage("§7Total: §f" + total + " §7| §aEnabled: §f" + enabled + 
            " §7| §bAvailable: §f" + available + " §7| §eGroups: §f" + groups);
    }
    
    /**
     * Sends formatted arena information to the sender.
     * 
     * @param sender Command sender
     * @param arena Arena to display
     */
    private void sendArenaInfo(CommandSender sender, Arena arena) {
        String status;
        if (arena.isInUse()) {
            status = "§c■ IN USE";
        } else if (!arena.isEnabled()) {
            status = "§7■ DISABLED";
        } else if (arena.isOnCooldown()) {
            status = "§e■ COOLDOWN";
        } else {
            status = "§a■ AVAILABLE";
        }
        
        boolean valid = plugin.getArenaManager().isArenaValid(arena.getId());
        String validMarker = valid ? "§a✓" : "§c✗";
        
        sender.sendMessage("  " + status + " §f" + arena.getDisplayName() + 
            " §7(" + arena.getId() + ") " + validMarker);
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> filters = List.of("all", "enabled", "disabled", "available", "inuse");
            
            for (String filter : filters) {
                if (filter.startsWith(partial)) {
                    completions.add(filter);
                }
            }
            
            // Add group filters
            plugin.getArenaManager().getAllGroups().forEach(group -> {
                String groupFilter = "group:" + group.getGroupId();
                if (groupFilter.startsWith(partial)) {
                    completions.add(groupFilter);
                }
            });
        }
        
        return completions;
    }
}
