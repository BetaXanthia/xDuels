package tech.xanthia.kits.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;

import java.util.List;

/**
 * GUI for editing kit round configuration.
 * 
 * Allows setting:
 * - Unranked round count
 * - Ranked round count
 * - Queue availability (unranked/ranked)
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitRoundGUI {
    
    private static final int ROWS = 3;
    
    private final XDuels plugin;
    private final Kit kit;
    
    public KitRoundGUI(XDuels plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }
    
    /**
     * Opens the round configuration GUI.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lRounds: §e" + kit.getName());
        
        // Unranked rounds
        inv.setItem(10, createRoundItem("§aUnranked Rounds", kit.getUnrankedRounds(), Material.IRON_SWORD));
        
        // Ranked rounds
        inv.setItem(12, createRoundItem("§6Ranked Rounds", kit.getRankedRounds(), Material.DIAMOND_SWORD));
        
        // Queue toggles
        inv.setItem(14, createToggleItem("§aAllow in Unranked Queue", kit.isAllowInQueueUnranked(), Material.COMPARATOR));
        inv.setItem(16, createToggleItem("§6Allow in Ranked Queue", kit.isAllowInQueueRanked(), Material.REDSTONE_TORCH));
        
        // Bottom row
        inv.setItem(22, createItem(Material.ARROW, "§aBack", List.of()));
        inv.setItem(26, createItem(Material.EMERALD, "§aSave", List.of("§7Click to save changes")));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates a round count item.
     */
    private ItemStack createRoundItem(String name, int rounds, Material material) {
        return createItem(material, name, List.of(
            "",
            "§7Current: §e" + rounds + " round" + (rounds == 1 ? "" : "s"),
            "",
            "§eLeft-click: §a+1 round",
            "§eRight-click: §c-1 round",
            "§eShift-click: §6±5 rounds",
            "",
            "§7Must be at least 1"
        ));
    }
    
    /**
     * Creates a toggle item.
     */
    private ItemStack createToggleItem(String name, boolean enabled, Material material) {
        return createItem(material, name, List.of(
            "",
            enabled ? "§aEnabled" : "§cDisabled",
            "",
            "§eClick to toggle"
        ));
    }
    
    /**
     * Creates a simple item.
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
     * Adjusts unranked rounds.
     */
    public void adjustUnrankedRounds(int delta) {
        int newRounds = Math.max(1, kit.getUnrankedRounds() + delta);
        kit.setUnrankedRounds(newRounds);
    }
    
    /**
     * Adjusts ranked rounds.
     */
    public void adjustRankedRounds(int delta) {
        int newRounds = Math.max(1, kit.getRankedRounds() + delta);
        kit.setRankedRounds(newRounds);
    }
    
    /**
     * Toggles unranked queue availability.
     */
    public void toggleUnrankedQueue() {
        kit.setAllowInQueueUnranked(!kit.isAllowInQueueUnranked());
    }
    
    /**
     * Toggles ranked queue availability.
     */
    public void toggleRankedQueue() {
        kit.setAllowInQueueRanked(!kit.isAllowInQueueRanked());
    }
}
