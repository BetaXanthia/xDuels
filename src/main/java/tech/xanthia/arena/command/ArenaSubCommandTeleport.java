package tech.xanthia.arena.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaLocationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand: /xduels arena teleport <arenaId> <locationType>
 * 
 * Teleports the player to a specific location within an arena.
 * Useful for checking arena configurations and testing spawns.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSubCommandTeleport implements ArenaSubCommand {
    
    private final XDuels plugin;
    
    public ArenaSubCommandTeleport(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "teleport";
    }
    
    @Override
    public String getDescription() {
        return "Teleport to arena location";
    }
    
    @Override
    public String getUsage() {
        return "/xduels arena teleport <arenaId> <locationType>";
    }
    
    @Override
    public String getPermission() {
        return "xduels.arena.teleport";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("tp", "goto");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: " + getUsage());
            return;
        }
        
        Player player = (Player) sender;
        String arenaId = args[0];
        String locationTypeName = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaId);
        
        if (arena == null) {
            player.sendMessage(plugin.getMessage("arena.not-found", "§cArena {arena} was not found!")
                .replace("{arena}", arenaId));
            return;
        }
        
        ArenaLocationType locationType = ArenaLocationType.fromString(locationTypeName);
        
        if (locationType == null) {
            player.sendMessage("§cInvalid location type. Valid types: center, corner1, corner2, redspawn, bluespawn, partycenter");
            return;
        }
        
        Location location = arena.getLocation(locationType);
        
        if (location == null) {
            player.sendMessage(plugin.getMessage("arena.location-not-set", "§c{location} is not set for {arena}.")
                .replace("{arena}", arena.getDisplayName())
                .replace("{location}", locationType.getDisplayName()));
            return;
        }
        
        player.teleport(location);
        player.sendMessage(plugin.getMessage("arena.teleported", "§aTeleported to {location} in {arena}.")
            .replace("{arena}", arena.getDisplayName())
            .replace("{location}", locationType.getDisplayName()));
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
            for (ArenaLocationType type : ArenaLocationType.values()) {
                if (type.getConfigKey().startsWith(partial)) {
                    completions.add(type.getConfigKey());
                }
            }
        }
        
        return completions;
    }
}
