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
 * Main kit editor GUI.
 * 
 * Provides access to:
 * - Enable/disable toggle
 * - Display item editor
 * - Rule editor
 * - Attribute editor
 * - Potion effects editor
 * - Round configuration
 * - Mode toggles (boxing, sumo, spleef, KB sync)
 * - Loadout editor
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitEditorGUI {
    
    private static final int ROWS = 6;
    
    private final XDuels plugin;
    private final Kit kit;
    
    public KitEditorGUI(XDuels plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }
    
    /**
     * Opens the main editor GUI.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lEdit Kit: §e" + kit.getName());
        
        // Row 1: Status and Display
        inv.setItem(10, createStatusItem());
        inv.setItem(11, createDisplayItem());
        inv.setItem(12, createQueueSettingsItem());
        
        // Row 2: Configuration Editors
        inv.setItem(19, createRuleEditorItem());
        inv.setItem(20, createAttributeEditorItem());
        inv.setItem(21, createEffectsEditorItem());
        inv.setItem(22, createRoundEditorItem());
        
        // Row 3: Special Modes
        inv.setItem(28, createModeToggleItem("§6Boxing Mode", kit.isBoxingMode(), Material.IRON_SWORD));
        inv.setItem(29, createModeToggleItem("§6Sumo Mode", kit.isSumoMode(), Material.STICK));
        inv.setItem(30, createModeToggleItem("§6Spleef Mode", kit.isSpleefMode(), Material.DIAMOND_SHOVEL));
        inv.setItem(31, createModeToggleItem("§6KB Sync", kit.isKbSync(), Material.SLIME_BALL));
        
        // Row 4: Additional Settings
        inv.setItem(37, createToggleItem("§6Health Indicators", kit.isHealthIndicators(), Material.REDSTONE));
        inv.setItem(38, createToggleItem("§6Item Drops", kit.isItemDropAllowed(), Material.DROPPER));
        inv.setItem(39, createToggleItem("§6Item Pickup", kit.isItemPickupAllowed(), Material.HOPPER));
        inv.setItem(40, createToggleItem("§6Virtual Editor", kit.isVirtualEditorAllowed(), Material.CRAFTING_TABLE));
        
        // Row 5: Loadout Editor
        inv.setItem(49, createLoadoutEditorItem());
        
        // Bottom row: Navigation
        inv.setItem(45, createItem(Material.ARROW, "§aBack to Kit List", List.of()));
        inv.setItem(53, createItem(Material.BARRIER, "§cClose", List.of()));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates the status toggle item.
     */
    private ItemStack createStatusItem() {
        Material mat = kit.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = kit.isEnabled() ? "§aEnabled" : "§cDisabled";
        
        return createItem(mat, "§6Kit Status: " + status, List.of(
            "",
            "§eClick to toggle"
        ));
    }
    
    /**
     * Creates the display item editor item.
     */
    private ItemStack createDisplayItem() {
        return createItem(Material.ITEM_FRAME, "§6Display Item", List.of(
            "",
            "§7Current: §e" + kit.getDisplayItem().getType().name(),
            "",
            "§eClick to change"
        ));
    }
    
    /**
     * Creates the queue settings item.
     */
    private ItemStack createQueueSettingsItem() {
        return createItem(Material.COMPARATOR, "§6Queue Settings", List.of(
            "",
            "§7Unranked: " + (kit.isAllowInQueueUnranked() ? "§aEnabled" : "§cDisabled"),
            "§7Ranked: " + (kit.isAllowInQueueRanked() ? "§aEnabled" : "§cDisabled"),
            "",
            "§eClick to configure"
        ));
    }
    
    /**
     * Creates the rule editor item.
     */
    private ItemStack createRuleEditorItem() {
        return createItem(Material.BOOK, "§6Rule Editor", List.of(
            "",
            "§7Configure gameplay rules",
            "§7Hunger, damage, building, etc.",
            "",
            "§eClick to open"
        ));
    }
    
    /**
     * Creates the attribute editor item.
     */
    private ItemStack createAttributeEditorItem() {
        return createItem(Material.ENCHANTED_BOOK, "§6Attribute Editor", List.of(
            "",
            "§7Health: §e" + kit.getAttributeSet().getMaxHealth(),
            "§7Speed: §e" + kit.getAttributeSet().getMovementSpeed(),
            "§7Damage: §e" + kit.getAttributeSet().getAttackDamage(),
            "",
            "§eClick to edit"
        ));
    }
    
    /**
     * Creates the effects editor item.
     */
    private ItemStack createEffectsEditorItem() {
        return createItem(Material.POTION, "§6Potion Effects", List.of(
            "",
            "§7Active Effects: §e" + kit.getPotionEffects().size(),
            "",
            "§eClick to manage"
        ));
    }
    
    /**
     * Creates the round editor item.
     */
    private ItemStack createRoundEditorItem() {
        return createItem(Material.CLOCK, "§6Round Configuration", List.of(
            "",
            "§7Unranked: §e" + kit.getUnrankedRounds() + " rounds",
            "§7Ranked: §e" + kit.getRankedRounds() + " rounds",
            "",
            "§eClick to edit"
        ));
    }
    
    /**
     * Creates a mode toggle item.
     */
    private ItemStack createModeToggleItem(String name, boolean enabled, Material material) {
        return createItem(material, name, List.of(
            "",
            enabled ? "§aEnabled" : "§cDisabled",
            "",
            "§eClick to toggle"
        ));
    }
    
    /**
     * Creates a simple toggle item.
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
     * Creates the loadout editor item.
     */
    private ItemStack createLoadoutEditorItem() {
        return createItem(Material.CHEST, "§6§lEdit Loadout", List.of(
            "",
            "§7Opens vanilla kit editor",
            "§7Edit inventory and armor",
            "",
            "§eClick to open"
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
     * Gets the kit being edited.
     * 
     * @return Kit
     */
    public Kit getKit() {
        return kit;
    }
}
