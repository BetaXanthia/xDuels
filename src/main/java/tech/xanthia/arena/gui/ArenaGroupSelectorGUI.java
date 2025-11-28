package tech.xanthia.arena.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting and managing arena groups.
 * 
 * Displays all arena groups with their properties and statistics.
 * Allows players to:
 * - View group details
 * - Select a group for matchmaking
 * - Filter arenas by group
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaGroupSelectorGUI {
    
    private final XDuels plugin;
    
    /**
     * Creates a new arena group selector GUI.
     * 
     * @param plugin Plugin instance
     */
    public ArenaGroupSelectorGUI(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Opens the group selector for a player.
     * 
     * @param player Player to show GUI to
     */
    public void open(Player player) {
        List<ArenaGroup> groups = new ArrayList<>(plugin.getArenaManager().getAllGroups());
        
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lArena Groups");
        
        // Add group items
        int slot = 0;
        for (ArenaGroup group : groups) {
            if (slot >= 45) break;
            inv.setItem(slot++, createGroupItem(group));
        }
        
        // Controls
        inv.setItem(49, createItem(Material.BARRIER, "§c§lClose", List.of()));
        
        // Statistics
        int totalGroups = groups.size();
        int totalArenas = plugin.getArenaManager().getArenaCount();
        int standaloneArenas = (int) plugin.getArenaManager().getAllArenas().stream()
            .filter(a -> !a.hasGroup())
            .count();
        
        inv.setItem(45, createItem(Material.BOOK, "§e§lStatistics",
            List.of(
                "§7Total Groups: §f" + totalGroups,
                "§7Total Arenas: §f" + totalArenas,
                "§7Standalone: §f" + standaloneArenas,
                "§7Grouped: §f" + (totalArenas - standaloneArenas)
            )));
        
        // All arenas option
        inv.setItem(48, createItem(Material.COMPASS, "§b§lAll Arenas",
            List.of(
                "§7View all arenas regardless of group",
                "",
                "§eClick to browse all arenas"
            )));
        
        // Create new group (admin only)
        if (player.hasPermission("xduels.arena.group.create")) {
            inv.setItem(53, createItem(Material.EMERALD, "§a§lCreate New Group",
                List.of(
                    "§7Create a new arena group",
                    "",
                    "§eClick to create"
                )));
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Opens a detailed view of a specific group.
     * 
     * @param player Player to show GUI to
     * @param group Group to display
     */
    public void openGroupDetails(Player player, ArenaGroup group) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6§l" + group.getDisplayName());
        
        // Group info
        inv.setItem(4, createItem(group.getDisplayItem(), "§e§l" + group.getDisplayName(),
            List.of(
                "§7ID: §f" + group.getGroupId(),
                "§7Arenas: §f" + group.getArenaCount(),
                "§7Shared Kits: §f" + (group.getSharedKits().isEmpty() ? "All" : group.getSharedKits().size()),
                "",
                "§7Regeneration: " + (group.isRegenerationEnabled() ? "§aEnabled" : "§cDisabled"),
                "§7Mode: §f" + (group.isUseBlockTracking() ? "Block Tracking" : "Schematic"),
                "§7Rounds: §f" + group.getRoundCount(),
                "§7Tournament Mode: " + (group.isTournamentMode() ? "§aYes" : "§7No")
            )));
        
        // Arena list
        List<Arena> groupArenas = plugin.getArenaManager().getArenasByGroup(group.getGroupId());
        int slot = 18;
        for (Arena arena : groupArenas) {
            if (slot >= 36) break;
            inv.setItem(slot++, createArenaItem(arena));
        }
        
        // Actions
        inv.setItem(45, createItem(Material.ARROW, "§7« Back to Groups", List.of()));
        
        inv.setItem(48, createItem(Material.DIAMOND_SWORD, "§e§lSelect Random Arena",
            List.of(
                "§7Selects a random available arena",
                "§7from this group for dueling",
                "",
                "§eClick to select"
            )));
        
        inv.setItem(49, createItem(Material.BOOK, "§b§lView All Arenas",
            List.of(
                "§7Opens arena selector filtered",
                "§7to this group only",
                "",
                "§eClick to browse"
            )));
        
        // Edit (admin only)
        if (player.hasPermission("xduels.arena.group.edit")) {
            inv.setItem(50, createItem(Material.WRITABLE_BOOK, "§e§lEdit Group",
                List.of(
                    "§7Modify group settings",
                    "",
                    "§eClick to edit"
                )));
        }
        
        // Delete (admin only)
        if (player.hasPermission("xduels.arena.group.delete")) {
            inv.setItem(53, createItem(Material.BARRIER, "§c§lDelete Group",
                List.of(
                    "§c§lWARNING: Cannot be undone!",
                    "§7Arenas will become standalone",
                    "",
                    "§eClick twice to confirm"
                )));
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an item representing a group.
     * 
     * @param group Arena group
     * @return ItemStack
     */
    private ItemStack createGroupItem(ArenaGroup group) {
        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §f" + group.getGroupId());
        lore.add("");
        lore.add("§7Arenas: §f" + group.getArenaCount());
        
        // Count available arenas
        long availableCount = plugin.getArenaManager().getArenasByGroup(group.getGroupId())
            .stream()
            .filter(Arena::isAvailable)
            .count();
        
        lore.add("§7Available: §f" + availableCount);
        lore.add("");
        
        if (!group.getSharedKits().isEmpty()) {
            lore.add("§7Kits: §f" + String.join(", ", group.getSharedKits()));
        } else {
            lore.add("§7Kits: §fAll");
        }
        
        lore.add("");
        lore.add("§eClick for details");
        
        return createItem(group.getDisplayItem(), "§e" + group.getDisplayName(), lore);
    }
    
    /**
     * Creates an item representing an arena.
     * 
     * @param arena Arena
     * @return ItemStack
     */
    private ItemStack createArenaItem(Arena arena) {
        Material material = arena.isAvailable() ? Material.LIME_WOOL : Material.RED_WOOL;
        String status = arena.isAvailable() ? "§a■ Available" : "§c■ Unavailable";
        
        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §f" + arena.getId());
        lore.add("");
        lore.add(status);
        lore.add("");
        lore.add("§eClick for arena selector");
        
        return createItem(material, "§f" + arena.getDisplayName(), lore);
    }
    
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
