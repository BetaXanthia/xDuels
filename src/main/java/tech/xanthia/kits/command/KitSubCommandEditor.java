package tech.xanthia.kits.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.kits.gui.KitListGUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand to open vanilla kit editor or kit list.
 * 
 * Usage: /xduels kit editor [kitName]
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitSubCommandEditor implements KitSubCommand {
    
    private final XDuels plugin;
    
    public KitSubCommandEditor(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Open kit list GUI
        KitListGUI gui = new KitListGUI(plugin);
        gui.open(player);
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "editor";
    }
    
    @Override
    public String getPermission() {
        return "xduels.kit.editor";
    }
    
    @Override
    public String getUsage() {
        return "/xduels kit editor §7- Open kit list";
    }
}
