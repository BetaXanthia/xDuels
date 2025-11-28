package tech.xanthia.kits.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Subcommand to delete a kit.
 * 
 * Usage: /xduels kit delete <name>
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitSubCommandDelete implements KitSubCommand {
    
    private final XDuels plugin;
    
    public KitSubCommandDelete(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }
        
        String kitName = args[0];
        
        // Check if kit exists
        if (!plugin.getKitManager().kitExists(kitName)) {
            sender.sendMessage("§cKit not found: §e" + kitName);
            return true;
        }
        
        // Delete kit
        boolean deleted = plugin.getKitManager().deleteKit(kitName);
        if (!deleted) {
            sender.sendMessage("§cFailed to delete kit. Check console for errors.");
            return true;
        }
        
        sender.sendMessage("§aKit §e" + kitName + "§a deleted successfully!");
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getKitManager().getAllKits().stream()
                .map(kit -> kit.getName())
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "delete";
    }
    
    @Override
    public String getPermission() {
        return "xduels.kit.delete";
    }
    
    @Override
    public String getUsage() {
        return "/xduels kit delete <name> §7- Delete a kit";
    }
}
