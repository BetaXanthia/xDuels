package tech.xanthia.kits.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitRuleSet;

import java.util.List;

/**
 * GUI for editing kit rules.
 * 
 * Allows toggling all gameplay rules:
 * - Hunger, damage, fall damage
 * - Building, block breaking
 * - Regeneration, crafting
 * - Drops, pickup
 * - Start timer
 * - Health indicators, KB sync
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitRuleEditorGUI {
    
    private static final int ROWS = 5;
    
    private final XDuels plugin;
    private final Kit kit;
    
    public KitRuleEditorGUI(XDuels plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }
    
    /**
     * Opens the rule editor GUI.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lRules: §e" + kit.getName());
        
        KitRuleSet rules = kit.getRuleSet();
        
        // Row 1: Core mechanics
        inv.setItem(10, createToggle("§6Hunger", rules.isHunger(), Material.COOKED_BEEF));
        inv.setItem(11, createToggle("§6Damage", rules.isDamageEnabled(), Material.DIAMOND_SWORD));
        inv.setItem(12, createToggle("§6Fall Damage", rules.isFallDamage(), Material.FEATHER));
        inv.setItem(13, createToggle("§6Regeneration", rules.isRegen(), Material.GOLDEN_APPLE));
        inv.setItem(14, createToggle("§6Crafting", rules.isCrafting(), Material.CRAFTING_TABLE));
        
        // Row 2: Building
        inv.setItem(19, createToggle("§6Building", rules.isBuild(), Material.BRICKS));
        inv.setItem(20, createToggle("§6Break Blocks", rules.isBreakBlocks(), Material.DIAMOND_PICKAXE));
        inv.setItem(21, createToggle("§6Only Break Player Placed", rules.isOnlyBreakPlayerPlaced(), Material.GRASS_BLOCK));
        
        // Row 3: Items
        inv.setItem(28, createToggle("§6Item Drops", rules.isDropsAllowed(), Material.DROPPER));
        inv.setItem(29, createToggle("§6Item Pickup", rules.isPickupAllowed(), Material.HOPPER));
        
        // Row 4: Indicators
        inv.setItem(37, createToggle("§6Health Indicators", rules.isHealthIndicators(), Material.REDSTONE));
        inv.setItem(38, createToggle("§6KB Sync", rules.isKbSync(), Material.SLIME_BALL));
        
        // Start timer
        inv.setItem(40, createTimerItem(rules.getStartTimerSeconds()));
        
        // Bottom row
        inv.setItem(44, createItem(Material.ARROW, "§aBack", List.of()));
        inv.setItem(49, createItem(Material.EMERALD, "§aSave", List.of("§7Click to save changes")));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates a toggle item.
     */
    private ItemStack createToggle(String name, boolean enabled, Material material) {
        return createItem(material, name, List.of(
            "",
            enabled ? "§aEnabled" : "§cDisabled",
            "",
            "§eClick to toggle"
        ));
    }
    
    /**
     * Creates the start timer item.
     */
    private ItemStack createTimerItem(int seconds) {
        return createItem(Material.CLOCK, "§6Start Timer", List.of(
            "",
            "§7Current: §e" + seconds + " seconds",
            "",
            "§eLeft-click: §a+1 second",
            "§eRight-click: §c-1 second",
            "§eShift-click: §6±5 seconds"
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
}
