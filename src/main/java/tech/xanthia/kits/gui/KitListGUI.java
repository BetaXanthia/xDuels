package tech.xanthia.kits.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;

import java.util.ArrayList;
import java.util.List;

/**
 * Kit list GUI for browsing all kits.
 * 
 * Displays all kits with their status and allows selection
 * for editing or management.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitListGUI {
    
    private static final int ROWS = 6;
    private static final int ITEMS_PER_PAGE = 45;
    
    private final XDuels plugin;
    private int currentPage;
    
    public KitListGUI(XDuels plugin) {
        this.plugin = plugin;
        this.currentPage = 0;
    }
    
    /**
     * Opens the kit list GUI for a player.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lKit Manager");
        
        // Get all kits
        List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
        
        // Calculate pagination
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, kits.size());
        
        // Add kit items
        for (int i = startIndex; i < endIndex; i++) {
            Kit kit = kits.get(i);
            int slot = i - startIndex;
            
            if (slot < 45) {
                inv.setItem(slot, createKitItem(kit));
            }
        }
        
        // Navigation items
        if (currentPage > 0) {
            inv.setItem(45, createItem(Material.ARROW, "§aPrevious Page", List.of()));
        }
        
        // Statistics
        inv.setItem(49, createItem(Material.BOOK, "§6Kit Statistics", List.of(
            "§7Total Kits: §e" + kits.size(),
            "§7Enabled: §a" + plugin.getKitManager().getEnabledKits().size(),
            "§7Unranked: §e" + plugin.getKitManager().getUnrankedKits().size(),
            "§7Ranked: §e" + plugin.getKitManager().getRankedKits().size()
        )));
        
        if (endIndex < kits.size()) {
            inv.setItem(53, createItem(Material.ARROW, "§aNext Page", List.of()));
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an item representing a kit.
     * 
     * @param kit Kit to represent
     * @return ItemStack
     */
    private ItemStack createKitItem(Kit kit) {
        ItemStack item = kit.getDisplayItem().clone();
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6§l" + kit.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(kit.isEnabled() ? "§aEnabled" : "§cDisabled");
        lore.add("");
        lore.add("§7Unranked Rounds: §e" + kit.getUnrankedRounds());
        lore.add("§7Ranked Rounds: §e" + kit.getRankedRounds());
        lore.add("");
        
        // Mode indicators
        if (kit.isBoxingMode()) lore.add("§e⚔ §7Boxing Mode");
        if (kit.isSumoMode()) lore.add("§e⚔ §7Sumo Mode");
        if (kit.isSpleefMode()) lore.add("§e⚔ §7Spleef Mode");
        if (kit.isKbSync()) lore.add("§e⚡ §7KB Sync");
        
        lore.add("");
        lore.add("§eClick to edit");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Creates a simple item with name and lore.
     * 
     * @param material Material
     * @param name Display name
     * @param lore Lore lines
     * @return ItemStack
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Goes to next page.
     */
    public void nextPage() {
        currentPage++;
    }
    
    /**
     * Goes to previous page.
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }
}
