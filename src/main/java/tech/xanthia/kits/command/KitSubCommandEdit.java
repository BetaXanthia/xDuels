package tech.xanthia.kits.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.gui.KitEditorGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Subcommand to open kit editor GUI.
 * 
 * Usage: /xduels kit edit <name>
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitSubCommandEdit implements KitSubCommand {
    
    private final XDuels plugin;
    
    public KitSubCommandEdit(XDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        if (args.length < 1) {
            return false;
        }
        
        Player player = (Player) sender;
        String kitName = args[0];
        
        // Get kit
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            sender.sendMessage("§cKit not found: §e" + kitName);
            return true;
        }
        
        // Open editor GUI
        KitEditorGUI gui = new KitEditorGUI(plugin, kit);
        gui.open(player);
        
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
        return "edit";
    }
    
    @Override
    public String getPermission() {
        return "xduels.kit.edit";
    }
    
    @Override
    public String getUsage() {
        return "/xduels kit edit <name> §7- Open kit editor GUI";
    }
}
