package tech.xanthia.kits.command;

import org.bukkit.command.CommandSender;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand to create a new kit.
 * 
 * Usage: /xduels kit create <name>
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitSubCommandCreate implements KitSubCommand {
    
    private final XDuels plugin;
    
    public KitSubCommandCreate(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }
        
        String kitName = args[0];
        
        // Check if kit already exists
        if (plugin.getKitManager().kitExists(kitName)) {
            sender.sendMessage("§cA kit with that name already exists!");
            return true;
        }
        
        // Create kit
        Kit kit = plugin.getKitManager().createKit(kitName);
        if (kit == null) {
            sender.sendMessage("§cFailed to create kit. Check console for errors.");
            return true;
        }
        
        sender.sendMessage("§aKit §e" + kitName + "§a created successfully!");
        sender.sendMessage("§7Use §e/xduels kit edit " + kitName + "§7 to configure it.");
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "create";
    }
    
    @Override
    public String getPermission() {
        return "xduels.kit.create";
    }
    
    @Override
    public String getUsage() {
        return "/xduels kit create <name> §7- Create a new kit";
    }
}
