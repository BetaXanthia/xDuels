package tech.xanthia.arena.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.arena.Arena;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI for selecting an arena for duels.
 * 
 * Shows all available arenas filtered by kit compatibility.
 * Players can click an arena to:
 * - Challenge another player to a duel
 * - Join a queue for that arena
 * - View arena details
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSelectorGUI {
    
    private final XDuels plugin;
    private final String kitId;
    private int currentPage;
    
    private static final int ITEMS_PER_PAGE = 45;
    
    /**
     * Creates a new arena selector GUI.
     * 
     * @param plugin Plugin instance
     * @param kitId Kit ID to filter arenas by (null for all)
     */
    public ArenaSelectorGUI(XDuels plugin, String kitId) {
        this.plugin = plugin;
        this.kitId = kitId;
        this.currentPage = 0;
    }
    
    /**
     * Opens the arena selector for a player.
     * 
     * @param player Player to show GUI to
     */
    public void open(Player player) {
        open(player, 0);
    }
    
    /**
     * Opens the arena selector at a specific page.
     * 
     * @param player Player to show GUI to
     * @param page Page number (0-indexed)
     */
    public void open(Player player, int page) {
        this.currentPage = page;
        
        // Get arenas based on kit filter
        List<Arena> arenas = kitId != null
            ? plugin.getArenaManager().getArenasForKit(kitId)
            : new ArrayList<>(plugin.getArenaManager().getEnabledArenas());
        
        // Calculate pagination
        int totalPages = (int) Math.ceil((double) arenas.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, arenas.size());
        
        List<Arena> pageArenas = arenas.subList(startIndex, endIndex);
        
        // Create inventory
        String title = kitId != null
            ? "§6§lSelect Arena - " + kitId
            : "§6§lSelect Arena";
        
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        // Add arena items
        int slot = 0;
        for (Arena arena : pageArenas) {
            inv.setItem(slot++, createArenaItem(arena));
        }
        
        // Navigation
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "§7« Previous Page", 
                List.of("§7Page " + page + " of " + totalPages)));
        }
        
        if (page < totalPages - 1) {
            inv.setItem(53, createItem(Material.ARROW, "§7Next Page »", 
                List.of("§7Page " + (page + 2) + " of " + totalPages)));
        }
        
        // Controls
        inv.setItem(49, createItem(Material.BARRIER, "§c§lClose", List.of()));
        
        // Filter info
        inv.setItem(48, createItem(Material.COMPASS, "§e§lFilter",
            List.of(
                "§7Showing: §f" + arenas.size() + " arenas",
                kitId != null ? "§7Kit: §f" + kitId : "§7Kit: §fAll",
                "",
                "§eClick to change filter"
            )));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an item representing an arena.
     * 
     * @param arena Arena
     * @return ItemStack
     */
    private ItemStack createArenaItem(Arena arena) {
        Material material;
        String statusColor;
        String statusText;
        
        if (arena.isInUse()) {
            material = Material.RED_WOOL;
            statusColor = "§c";
            statusText = "■ In Use";
        } else if (arena.isOnCooldown()) {
            material = Material.YELLOW_WOOL;
            statusColor = "§e";
            statusText = "■ Cooldown";
        } else {
            material = Material.LIME_WOOL;
            statusColor = "§a";
            statusText = "■ Available";
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §f" + arena.getId());
        lore.add("");
        lore.add(statusColor + statusText);
        lore.add("");
        
        if (arena.hasGroup()) {
            var group = plugin.getArenaManager().getGroup(arena.getGroupId());
            if (group != null) {
                lore.add("§7Group: §f" + group.getDisplayName());
                lore.add("§7Group Arenas: §f" + group.getArenaCount());
            }
        }
        
        if (!arena.getAllowedKits().isEmpty()) {
            lore.add("§7Allowed Kits: §f" + arena.getAllowedKits().size());
        }
        
        lore.add("");
        
        if (arena.isAvailable()) {
            lore.add("§a§lCLICK TO SELECT");
        } else {
            lore.add("§c§lUNAVAILABLE");
        }
        
        return createItem(material, "§e" + arena.getDisplayName(), lore);
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
    
    /**
     * Gets the current page number.
     * 
     * @return Page number
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the kit filter being used.
     * 
     * @return Kit ID or null
     */
    public String getKitId() {
        return kitId;
    }
}
