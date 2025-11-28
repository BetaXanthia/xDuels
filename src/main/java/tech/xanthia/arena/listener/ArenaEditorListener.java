package tech.xanthia.arena.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaEditorSession;
import tech.xanthia.arena.ArenaLocationType;
import tech.xanthia.arena.gui.ArenaEditorGUI;

/**
 * Listener for arena editor interactions.
 * 
 * Handles:
 * - Block click events to set arena locations
 * - Position confirmation for editor mode
 * - Session management
 * 
 * Only active when a player has an active editor session.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaEditorListener implements Listener {
    
    private final XDuels plugin;
    
    /**
     * Creates a new arena editor listener.
     * 
     * @param plugin Plugin instance
     */
    public ArenaEditorListener(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player interact events for arena location setting.
     * 
     * When a player with an active editor session clicks a block,
     * the clicked location is set as the active location type.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active editor session
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        
        // Only handle left-click on blocks
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        // Check if player is setting a location
        if (!session.hasActiveLocationType()) {
            return;
        }
        
        // Cancel the event to prevent block breaking
        event.setCancelled(true);
        
        Location clickedLocation = event.getClickedBlock().getLocation();
        
        // Apply location to session
        ArenaLocationType locationType = session.getActiveLocationType();
        
        // For player-facing locations (spawns, center), use player's exact position with look direction
        Location locationToSet;
        if (locationType.isSpawnPoint() || locationType == ArenaLocationType.CENTER || 
            locationType == ArenaLocationType.PARTY_CENTER) {
            // Use player's position (with yaw/pitch) centered on block
            locationToSet = player.getLocation().clone();
            locationToSet.setX(clickedLocation.getBlockX() + 0.5);
            locationToSet.setY(clickedLocation.getBlockY() + 1.0); // Stand on top of block
            locationToSet.setZ(clickedLocation.getBlockZ() + 0.5);
        } else {
            // For corners, use exact block position
            locationToSet = clickedLocation;
        }
        
        // Apply the location
        if (session.applyLocation(locationToSet)) {
            player.sendMessage(plugin.getMessage("arena.location-set", "§aSet {location} for {arena} at {x}, {y}, {z}.")
                .replace("{location}", locationType.getDisplayName())
                .replace("{arena}", session.getArena().getDisplayName())
                .replace("{x}", String.valueOf(locationToSet.getBlockX()))
                .replace("{y}", String.valueOf(locationToSet.getBlockY()))
                .replace("{z}", String.valueOf(locationToSet.getBlockZ())));
            
            // Play sound
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
            
            // Save arena
            plugin.getArenaManager().saveArena(session.getArena());
            
            // Reopen editor GUI
            ArenaEditorGUI gui = new ArenaEditorGUI(plugin);
            gui.openEditor(player, session.getArena());
        }
    }
}
