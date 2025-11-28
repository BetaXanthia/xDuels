package tech.xanthia.arena.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.xanthia.XDuels;
import tech.xanthia.arena.ArenaEditorSession;
import tech.xanthia.arena.gui.ArenaEditorGUI;

/**
 * Listener for player disconnect events during arena editing.
 * 
 * Ensures that editor sessions are properly cleaned up when
 * players log out to prevent memory leaks and session conflicts.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaEditorQuitListener implements Listener {
    
    private final XDuels plugin;
    
    /**
     * Creates a new arena editor quit listener.
     * 
     * @param plugin Plugin instance
     */
    public ArenaEditorQuitListener(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Cleans up editor sessions when players disconnect.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active editor session
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session != null) {
            // Auto-save if modifications were made
            if (session.isModified()) {
                plugin.getArenaManager().saveArena(session.getArena());
                plugin.getLogger().info("[ ArenaEditor ] Auto-saved changes for " + 
                    session.getArena().getId() + " by disconnecting player " + player.getName());
            }
            
            // Remove session
            ArenaEditorGUI.removeSession(player.getUniqueId());
            
            plugin.getLogger().info("[ ArenaEditor ] Cleaned up editor session for " + player.getName() +
                " (duration: " + session.getFormattedDuration() + ")");
        }
    }
}
