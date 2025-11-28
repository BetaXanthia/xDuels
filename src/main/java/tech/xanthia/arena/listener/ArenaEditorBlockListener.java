package tech.xanthia.arena.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import tech.xanthia.XDuels;
import tech.xanthia.arena.ArenaEditorSession;
import tech.xanthia.arena.gui.ArenaEditorGUI;

/**
 * Listener for block events during arena editor mode.
 * 
 * Prevents block breaking/placing when a player is in editor mode
 * to avoid accidental world modifications while setting locations.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaEditorBlockListener implements Listener {
    
    private final XDuels plugin;
    
    /**
     * Creates a new arena editor block listener.
     * 
     * @param plugin Plugin instance
     */
    public ArenaEditorBlockListener(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Prevents block breaking while in editor mode.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active editor session with location setting active
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session != null && session.hasActiveLocationType()) {
            // Cancel block break to prevent accidental destruction
            event.setCancelled(true);
            player.sendMessage("§cBlock breaking disabled while setting arena locations.");
            player.sendMessage("§7Right-click air or type §e/xduels arena editor§7 to cancel.");
        }
    }
    
    /**
     * Prevents block placing while in editor mode.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active editor session with location setting active
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session != null && session.hasActiveLocationType()) {
            // Cancel block place to prevent accidental building
            event.setCancelled(true);
            player.sendMessage("§cBlock placing disabled while setting arena locations.");
            player.sendMessage("§7Right-click air or type §e/xduels arena editor§7 to cancel.");
        }
    }
}
