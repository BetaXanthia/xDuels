package tech.xanthia.kits.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitAttributeSet;

import java.util.List;

/**
 * GUI for editing kit attributes.
 * 
 * Allows modification of:
 * - Max Health
 * - Movement Speed
 * - Attack Damage
 * - Knockback Resistance
 * - Entity Scale
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitAttributeGUI {
    
    private static final int ROWS = 4;
    
    private final XDuels plugin;
    private final Kit kit;
    
    public KitAttributeGUI(XDuels plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
    }
    
    /**
     * Opens the attribute editor GUI.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lAttributes: §e" + kit.getName());
        
        KitAttributeSet attrs = kit.getAttributeSet();
        
        // Attribute items
        inv.setItem(10, createAttributeItem("§cMax Health", attrs.getMaxHealth(), Material.REDSTONE, 
            1.0, 1024.0, 1.0));
        inv.setItem(11, createAttributeItem("§bMovement Speed", attrs.getMovementSpeed(), Material.SUGAR,
            0.0, 1.0, 0.01));
        inv.setItem(12, createAttributeItem("§6Attack Damage", attrs.getAttackDamage(), Material.DIAMOND_SWORD,
            0.0, 100.0, 0.5));
        inv.setItem(13, createAttributeItem("§aKnockback Resistance", attrs.getKnockbackResistance(), Material.IRON_CHESTPLATE,
            0.0, 1.0, 0.1));
        inv.setItem(14, createAttributeItem("§dEntity Scale", attrs.getScale(), Material.SLIME_BALL,
            0.0625, 16.0, 0.125));
        
        // Presets
        inv.setItem(19, createPresetItem("§aVanilla", "Reset to vanilla values"));
        inv.setItem(20, createPresetItem("§6Tank", "High health, slow speed"));
        inv.setItem(21, createPresetItem("§bSpeed", "Normal health, high speed"));
        inv.setItem(22, createPresetItem("§cGlass Cannon", "Low health, high damage"));
        
        // Bottom row
        inv.setItem(26, createItem(Material.ARROW, "§aBack", List.of()));
        inv.setItem(31, createItem(Material.EMERALD, "§aSave", List.of("§7Click to save changes")));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an attribute item.
     */
    private ItemStack createAttributeItem(String name, double value, Material material,
                                          double min, double max, double increment) {
        return createItem(material, name, List.of(
            "",
            "§7Current: §e" + String.format("%.2f", value),
            "§7Range: §e" + min + " - " + max,
            "",
            "§eLeft-click: §a+" + increment,
            "§eRight-click: §c-" + increment,
            "§eShift-click: §6±" + (increment * 5)
        ));
    }
    
    /**
     * Creates a preset item.
     */
    private ItemStack createPresetItem(String name, String description) {
        return createItem(Material.BOOK, name, List.of(
            "",
            "§7" + description,
            "",
            "§eClick to apply"
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
     * Applies a preset to the kit.
     */
    public void applyPreset(String preset) {
        KitAttributeSet attrs = kit.getAttributeSet();
        
        switch (preset.toLowerCase()) {
            case "vanilla":
                attrs.setMaxHealth(20.0);
                attrs.setMovementSpeed(0.1);
                attrs.setAttackDamage(1.0);
                attrs.setKnockbackResistance(0.0);
                attrs.setScale(1.0f);
                break;
            case "tank":
                attrs.setMaxHealth(40.0);
                attrs.setMovementSpeed(0.08);
                attrs.setAttackDamage(1.5);
                attrs.setKnockbackResistance(0.3);
                attrs.setScale(1.2f);
                break;
            case "speed":
                attrs.setMaxHealth(20.0);
                attrs.setMovementSpeed(0.15);
                attrs.setAttackDamage(1.0);
                attrs.setKnockbackResistance(0.0);
                attrs.setScale(0.9f);
                break;
            case "glass cannon":
                attrs.setMaxHealth(10.0);
                attrs.setMovementSpeed(0.12);
                attrs.setAttackDamage(3.0);
                attrs.setKnockbackResistance(0.0);
                attrs.setScale(0.8f);
                break;
        }
    }
}
