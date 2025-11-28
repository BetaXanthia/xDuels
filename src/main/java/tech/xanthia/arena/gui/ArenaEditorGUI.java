package tech.xanthia.arena.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaEditorSession;
import tech.xanthia.arena.ArenaLocationType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI for editing arena properties and locations.
 * 
 * Provides an intuitive interface for:
 * - Setting arena locations (center, corners, spawns)
 * - Toggling arena settings (enabled, regeneration)
 * - Managing kit bindings
 * - Assigning to groups
 * - Capturing/restoring schematics
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaEditorGUI {
    
    private final XDuels plugin;
    private static final ConcurrentHashMap<UUID, ArenaEditorSession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Creates a new arena editor GUI handler.
     * 
     * @param plugin Plugin instance
     */
    public ArenaEditorGUI(XDuels plugin) {
        this.plugin = plugin;
    }
    
    // ========================================
    // SESSION MANAGEMENT
    // ========================================
    
    /**
     * Gets or creates an editor session for a player.
     * 
     * @param player Player
     * @param arena Arena being edited
     * @return Editor session
     */
    public static ArenaEditorSession getOrCreateSession(UUID playerId, Arena arena) {
        return activeSessions.computeIfAbsent(playerId, k -> new ArenaEditorSession(playerId, arena));
    }
    
    /**
     * Gets an active editor session.
     * 
     * @param playerId Player UUID
     * @return Editor session or null
     */
    public static ArenaEditorSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }
    
    /**
     * Removes an editor session.
     * 
     * @param playerId Player UUID
     */
    public static void removeSession(UUID playerId) {
        activeSessions.remove(playerId);
    }
    
    /**
     * Checks if a player has an active session.
     * 
     * @param playerId Player UUID
     * @return true if session exists
     */
    public static boolean hasSession(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }
    
    // ========================================
    // ARENA SELECTOR
    // ========================================
    
    /**
     * Opens the arena selector GUI.
     * 
     * @param player Player to show GUI to
     */
    public void openArenaSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lSelect Arena to Edit");
        
        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (slot >= 45) break; // Leave room for controls
            
            ItemStack item = createArenaItem(arena);
            inv.setItem(slot++, item);
        }
        
        // Controls
        inv.setItem(49, createItem(Material.BARRIER, "§c§lClose", List.of()));
        inv.setItem(45, createItem(Material.EMERALD, "§a§lCreate New Arena", 
            List.of("§7Click to create a new arena")));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an item representing an arena.
     * 
     * @param arena Arena
     * @return ItemStack
     */
    private ItemStack createArenaItem(Arena arena) {
        Material material = arena.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §f" + arena.getId());
        lore.add("");
        lore.add(arena.isEnabled() ? "§aEnabled" : "§cDisabled");
        lore.add(arena.isInUse() ? "§c■ In Use" : "§a■ Available");
        lore.add("");
        lore.add("§7Valid: " + (plugin.getArenaManager().isArenaValid(arena.getId()) ? "§a✓" : "§c✗"));
        lore.add("§7Group: §f" + (arena.hasGroup() ? arena.getGroupId() : "None"));
        lore.add("");
        lore.add("§eClick to edit");
        
        return createItem(material, "§e" + arena.getDisplayName(), lore);
    }
    
    // ========================================
    // ARENA EDITOR
    // ========================================
    
    /**
     * Opens the main arena editor GUI.
     * 
     * @param player Player to show GUI to
     * @param arena Arena to edit
     */
    public void openEditor(Player player, Arena arena) {
        // Create or get session
        ArenaEditorSession session = getOrCreateSession(player.getUniqueId(), arena);
        
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lEdit Arena: " + arena.getDisplayName());
        
        // Location buttons (row 1-2)
        inv.setItem(10, createLocationItem(arena, ArenaLocationType.CENTER));
        inv.setItem(11, createLocationItem(arena, ArenaLocationType.CORNER1));
        inv.setItem(12, createLocationItem(arena, ArenaLocationType.CORNER2));
        inv.setItem(13, createLocationItem(arena, ArenaLocationType.RED_SPAWN));
        inv.setItem(14, createLocationItem(arena, ArenaLocationType.BLUE_SPAWN));
        inv.setItem(15, createLocationItem(arena, ArenaLocationType.PARTY_CENTER));
        
        // Settings (row 3)
        inv.setItem(28, createToggleItem(Material.LEVER, 
            arena.isEnabled() ? "§a§lEnabled" : "§c§lDisabled",
            List.of("§7Click to " + (arena.isEnabled() ? "disable" : "enable")),
            arena.isEnabled()));
        
        inv.setItem(29, createToggleItem(Material.GRASS_BLOCK,
            "§e§lRegeneration",
            List.of(
                "§7Mode: " + (arena.isUseBlockTracking() ? "§fBlock Tracking" : "§fSchematic"),
                "§7Status: " + (arena.isRegenerationEnabled() ? "§aEnabled" : "§cDisabled"),
                "",
                "§eLeft-click: Toggle on/off",
                "§eRight-click: Change mode"
            ),
            arena.isRegenerationEnabled()));
        
        inv.setItem(30, createItem(Material.DIAMOND_SWORD, "§b§lKit Bindings",
            List.of(
                "§7Allowed kits: §f" + (arena.getAllowedKits().isEmpty() ? "All" : arena.getAllowedKits().size()),
                "",
                "§eClick to manage kits"
            )));
        
        inv.setItem(31, createItem(Material.BOOKSHELF, "§d§lGroup Assignment",
            List.of(
                "§7Current group: §f" + (arena.hasGroup() ? arena.getGroupId() : "None"),
                "",
                "§eClick to change group"
            )));
        
        // Actions (row 4)
        inv.setItem(37, createItem(Material.NAME_TAG, "§e§lRename Arena",
            List.of("§7Click to rename this arena")));
        
        inv.setItem(38, createItem(Material.PAPER, "§b§lCapture Schematic",
            List.of(
                "§7Captures the current arena state",
                "§7as a schematic for regeneration",
                "",
                "§eClick to capture"
            )));
        
        inv.setItem(39, createItem(Material.ENDER_PEARL, "§d§lTeleport to Center",
            List.of("§7Teleports you to the arena center")));
        
        inv.setItem(40, createItem(Material.BARRIER, "§c§lDelete Arena",
            List.of(
                "§c§lWARNING: This cannot be undone!",
                "",
                "§7Click twice to confirm deletion"
            )));
        
        // Navigation (bottom row)
        inv.setItem(45, createItem(Material.ARROW, "§7« Back to Arena List", List.of()));
        inv.setItem(49, createItem(Material.EMERALD, "§a§lSave & Close", 
            List.of("§7Saves all changes and closes the editor")));
        inv.setItem(53, createItem(Material.BARRIER, "§c§lCancel", 
            List.of("§7Closes without saving")));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates a location setting item.
     * 
     * @param arena Arena
     * @param type Location type
     * @return ItemStack
     */
    private ItemStack createLocationItem(Arena arena, ArenaLocationType type) {
        boolean isSet = arena.hasLocation(type);
        Material material = isSet ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        
        List<String> lore = new ArrayList<>();
        lore.add(isSet ? "§a✓ Location set" : "§c✗ Not set");
        lore.add("");
        lore.add("§eLeft-click: Set location");
        lore.add("§eRight-click: Teleport here");
        lore.add("§eShift-click: Clear location");
        
        return createItem(material, "§6" + type.getDisplayName(), lore);
    }
    
    /**
     * Creates a toggle item with on/off state.
     * 
     * @param material Material
     * @param name Display name
     * @param lore Lore
     * @param enabled Current state
     * @return ItemStack
     */
    private ItemStack createToggleItem(Material material, String name, List<String> lore, boolean enabled) {
        ItemStack item = createItem(material, name, lore);
        if (enabled) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // setEnchantmentGlintOverride not available in 1.20.2
                // meta.setEnchantmentGlintOverride(true);
                item.setItemMeta(meta);
            }
        }
        return item;
    }
    
    // ========================================
    // GROUP SELECTOR
    // ========================================
    
    /**
     * Opens the group selector GUI.
     * 
     * @param player Player
     * @param arena Arena to assign to a group
     */
    public void openGroupSelector(Player player, Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lSelect Group: " + arena.getDisplayName());
        
        // None option
        inv.setItem(0, createItem(Material.BARRIER, "§c§lNo Group",
            List.of("§7Remove from any group")));
        
        // Existing groups
        int slot = 1;
        for (var group : plugin.getArenaManager().getAllGroups()) {
            if (slot >= 26) break;
            
            boolean isCurrent = arena.hasGroup() && arena.getGroupId().equals(group.getGroupId());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Arenas: §f" + group.getArenaCount());
            lore.add("§7Kits: §f" + (group.getSharedKits().isEmpty() ? "All" : group.getSharedKits().size()));
            lore.add("");
            if (isCurrent) {
                lore.add("§a✓ Current group");
            } else {
                lore.add("§eClick to assign");
            }
            
            inv.setItem(slot++, createItem(
                isCurrent ? Material.LIME_WOOL : Material.WHITE_WOOL,
                "§e" + group.getDisplayName(),
                lore
            ));
        }
        
        // Create new group
        inv.setItem(26, createItem(Material.EMERALD, "§a§lCreate New Group",
            List.of("§7Click to create a new group")));
        
        player.openInventory(inv);
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Creates an item with name and lore.
     * 
     * @param material Material
     * @param name Display name
     * @param lore Lore lines
     * @return ItemStack
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
