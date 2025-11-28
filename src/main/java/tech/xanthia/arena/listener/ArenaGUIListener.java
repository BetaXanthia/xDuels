package tech.xanthia.arena.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaEditorSession;
import tech.xanthia.arena.ArenaLocationType;
import tech.xanthia.arena.gui.ArenaEditorGUI;
import tech.xanthia.arena.gui.ArenaGroupSelectorGUI;
import tech.xanthia.arena.gui.ArenaSelectorGUI;

/**
 * Listener for arena GUI interactions.
 * 
 * Handles clicks in:
 * - Arena Editor GUI
 * - Arena Selector GUI
 * - Arena Group Selector GUI
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaGUIListener implements Listener {
    
    private final XDuels plugin;
    
    /**
     * Creates a new arena GUI listener.
     * 
     * @param plugin Plugin instance
     */
    public ArenaGUIListener(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles inventory click events for arena GUIs.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        String title = event.getView().getTitle();
        
        // Check which GUI is open
        if (title.contains("Edit Arena:")) {
            event.setCancelled(true);
            handleEditorGUIClick(player, event.getSlot(), clicked);
        } else if (title.contains("Select Arena")) {
            event.setCancelled(true);
            handleSelectorGUIClick(player, event.getSlot(), clicked, title);
        } else if (title.contains("Arena Groups") || title.contains("Arena Group")) {
            event.setCancelled(true);
            handleGroupGUIClick(player, event.getSlot(), clicked, title);
        } else if (title.contains("Select Group:")) {
            event.setCancelled(true);
            handleGroupAssignClick(player, event.getSlot(), clicked);
        }
    }
    
    /**
     * Handles clicks in the arena editor GUI.
     */
    private void handleEditorGUIClick(Player player, int slot, ItemStack clicked) {
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }
        
        Arena arena = session.getArena();
        ArenaEditorGUI gui = new ArenaEditorGUI(plugin);
        
        // Location buttons (slots 10-15)
        if (slot >= 10 && slot <= 15) {
            ArenaLocationType[] types = {
                ArenaLocationType.CENTER,
                ArenaLocationType.CORNER1,
                ArenaLocationType.CORNER2,
                ArenaLocationType.RED_SPAWN,
                ArenaLocationType.BLUE_SPAWN,
                ArenaLocationType.PARTY_CENTER
            };
            
            ArenaLocationType type = types[slot - 10];
            
            // Set as active location to be set on next block click
            session.setActiveLocationType(type);
            player.closeInventory();
            player.sendMessage("§eClick a block to set §6" + type.getDisplayName() + "§e for arena §6" + arena.getDisplayName());
            player.sendMessage("§7Right-click air to cancel.");
            
        } else if (slot == 28) {
            // Toggle enabled/disabled
            arena.setEnabled(!arena.isEnabled());
            session.markModified();
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage("§eArena " + (arena.isEnabled() ? "§aenabled" : "§cdisabled"));
            gui.openEditor(player, arena);
            
        } else if (slot == 29) {
            // Toggle regeneration or change mode
            if (clicked.getType() == Material.GRASS_BLOCK) {
                arena.setRegenerationEnabled(!arena.isRegenerationEnabled());
                session.markModified();
                plugin.getArenaManager().saveArena(arena);
                player.sendMessage("§eRegeneration " + (arena.isRegenerationEnabled() ? "§aenabled" : "§cdisabled"));
                gui.openEditor(player, arena);
            }
            
        } else if (slot == 31) {
            // Group assignment
            player.closeInventory();
            gui.openGroupSelector(player, arena);
            
        } else if (slot == 38) {
            // Capture schematic
            player.closeInventory();
            player.sendMessage("§eCapturing schematic for arena §6" + arena.getDisplayName() + "§e...");
            
            if (plugin.getRegenerationManager().captureSchematic(arena)) {
                player.sendMessage("§aSchematic captured successfully!");
            } else {
                player.sendMessage("§cFailed to capture schematic. Check console for errors.");
            }
            
        } else if (slot == 39) {
            // Teleport to center
            if (arena.hasLocation(ArenaLocationType.CENTER)) {
                player.teleport(arena.getLocation(ArenaLocationType.CENTER));
                player.sendMessage("§aTeleported to arena center.");
            } else {
                player.sendMessage("§cCenter location not set!");
            }
            player.closeInventory();
            
        } else if (slot == 40) {
            // Delete arena (requires confirmation)
            player.sendMessage("§cType §l/xduels arena delete " + arena.getId() + "§c to confirm deletion.");
            player.closeInventory();
            
        } else if (slot == 45) {
            // Back to arena list
            player.closeInventory();
            gui.openArenaSelector(player);
            
        } else if (slot == 49) {
            // Save & close
            if (session.isModified()) {
                plugin.getArenaManager().saveArena(arena);
                player.sendMessage("§aArena saved successfully!");
            }
            ArenaEditorGUI.removeSession(player.getUniqueId());
            player.closeInventory();
            
        } else if (slot == 53) {
            // Cancel
            ArenaEditorGUI.removeSession(player.getUniqueId());
            player.closeInventory();
            player.sendMessage("§7Editor closed without saving.");
        }
    }
    
    /**
     * Handles clicks in the arena selector GUI.
     */
    private void handleSelectorGUIClick(Player player, int slot, ItemStack clicked, String title) {
        // Navigation
        if (slot == 45 && clicked.getType() == Material.ARROW) {
            // Previous page (would need to track current page)
            player.sendMessage("§7Previous page");
        } else if (slot == 53 && clicked.getType() == Material.ARROW) {
            // Next page
            player.sendMessage("§7Next page");
        } else if (slot == 49 && clicked.getType() == Material.BARRIER) {
            // Close
            player.closeInventory();
        } else if (slot < 45) {
            // Arena selection (implement based on your duel system)
            player.sendMessage("§eArena selected!");
            player.closeInventory();
        }
    }
    
    /**
     * Handles clicks in the group selector/manager GUI.
     */
    private void handleGroupGUIClick(Player player, int slot, ItemStack clicked, String title) {
        if (slot == 49 && clicked != null && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (slot == 48) {
            // View all arenas
            ArenaSelectorGUI selector = new ArenaSelectorGUI(plugin, null);
            selector.open(player);
        }
    }
    
    /**
     * Handles clicks in the group assignment GUI.
     */
    private void handleGroupAssignClick(Player player, int slot, ItemStack clicked) {
        ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }
        
        Arena arena = session.getArena();
        
        if (slot == 0 && clicked.getType() == Material.BARRIER) {
            // Remove from group
            plugin.getArenaManager().removeArenaFromGroup(arena.getId());
            player.sendMessage("§eArena removed from group.");
            session.markModified();
        } else if (slot == 26 && clicked.getType() == Material.EMERALD) {
            // Create new group
            player.sendMessage("§eUse command: §6/xduels arena group " + arena.getId() + " <groupId>");
            player.closeInventory();
            return;
        } else if (slot > 0 && slot < 26) {
            // Assign to selected group (would need to extract group ID from item)
            player.sendMessage("§eGroup assignment updated!");
            session.markModified();
        }
        
        // Reopen editor
        ArenaEditorGUI gui = new ArenaEditorGUI(plugin);
        gui.openEditor(player, arena);
    }
    
    /**
     * Handles inventory close events to clean up sessions.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        // If closing editor without explicit save, keep session active for block clicks
        if (title.contains("Edit Arena:")) {
            ArenaEditorSession session = ArenaEditorGUI.getSession(player.getUniqueId());
            if (session != null && session.hasActiveLocationType()) {
                // Session stays active for location setting
                return;
            }
        }
    }
}
