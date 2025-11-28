package tech.xanthia.kits.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitPotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for managing kit potion effects.
 * 
 * Allows:
 * - Adding new effects
 * - Removing existing effects
 * - Modifying amplifier and duration
 * - Setting infinite duration
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitEffectsGUI {
    
    private static final int ROWS = 6;
    
    private final XDuels plugin;
    private final Kit kit;
    private int currentPage;
    
    public KitEffectsGUI(XDuels plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
        this.currentPage = 0;
    }
    
    /**
     * Opens the effects editor GUI.
     * 
     * @param player Player to open for
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lEffects: §e" + kit.getName());
        
        List<KitPotionEffect> effects = kit.getPotionEffects();
        
        // Display active effects (slots 0-35)
        int startIndex = currentPage * 36;
        int endIndex = Math.min(startIndex + 36, effects.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            KitPotionEffect effect = effects.get(i);
            int slot = i - startIndex;
            inv.setItem(slot, createEffectItem(effect));
        }
        
        // Add effect button
        inv.setItem(45, createItem(Material.EMERALD, "§a§lAdd Effect", List.of(
            "",
            "§7Click to add a new effect"
        )));
        
        // Navigation
        if (currentPage > 0) {
            inv.setItem(48, createItem(Material.ARROW, "§aPrevious Page", List.of()));
        }
        
        if (endIndex < effects.size()) {
            inv.setItem(50, createItem(Material.ARROW, "§aNext Page", List.of()));
        }
        
        // Back button
        inv.setItem(49, createItem(Material.ARROW, "§aBack", List.of()));
        
        // Clear all
        if (!effects.isEmpty()) {
            inv.setItem(53, createItem(Material.BARRIER, "§cClear All Effects", List.of(
                "",
                "§7Click to remove all effects"
            )));
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Opens the effect selector GUI.
     * 
     * @param player Player to open for
     */
    public void openSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, ROWS * 9, "§6§lSelect Effect");
        
        // List common effects (using getByName for 1.20.2+ compatibility)
        PotionEffectType[] commonEffects = {
            PotionEffectType.getByName("speed"),
            PotionEffectType.getByName("slowness"),
            PotionEffectType.getByName("haste"),
            PotionEffectType.getByName("mining_fatigue"),
            PotionEffectType.getByName("strength"),
            PotionEffectType.getByName("instant_health"),
            PotionEffectType.getByName("instant_damage"),
            PotionEffectType.getByName("jump_boost"),
            PotionEffectType.getByName("nausea"),
            PotionEffectType.getByName("regeneration"),
            PotionEffectType.getByName("resistance"),
            PotionEffectType.getByName("fire_resistance"),
            PotionEffectType.getByName("water_breathing"),
            PotionEffectType.getByName("invisibility"),
            PotionEffectType.getByName("blindness"),
            PotionEffectType.getByName("night_vision"),
            PotionEffectType.getByName("hunger"),
            PotionEffectType.getByName("weakness"),
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.ABSORPTION,
            PotionEffectType.SATURATION,
            PotionEffectType.GLOWING,
            PotionEffectType.LEVITATION,
            PotionEffectType.LUCK,
            PotionEffectType.UNLUCK,
            PotionEffectType.SLOW_FALLING
        };
        
        for (int i = 0; i < commonEffects.length && i < 45; i++) {
            PotionEffectType type = commonEffects[i];
            inv.setItem(i, createEffectTypeItem(type));
        }
        
        // Back button
        inv.setItem(49, createItem(Material.ARROW, "§aBack", List.of()));
        
        player.openInventory(inv);
    }
    
    /**
     * Creates an item representing a potion effect.
     */
    private ItemStack createEffectItem(KitPotionEffect effect) {
        return createItem(Material.POTION, "§6" + formatEffectName(effect.getType()), List.of(
            "",
            "§7Amplifier: §e" + (effect.getAmplifier() + 1),
            "§7Duration: §e" + (effect.isInfinite() ? "Infinite" : formatDuration(effect.getDuration())),
            "",
            "§eLeft-click: §aIncrease amplifier",
            "§eRight-click: §cDecrease amplifier",
            "§eShift-left: §6Toggle infinite",
            "§eShift-right: §cRemove"
        ));
    }
    
    /**
     * Creates an item representing an effect type.
     */
    private ItemStack createEffectTypeItem(PotionEffectType type) {
        return createItem(Material.POTION, "§6" + formatEffectName(type), List.of(
            "",
            "§eClick to add this effect"
        ));
    }
    
    /**
     * Formats an effect name.
     */
    private String formatEffectName(PotionEffectType type) {
        String name = type.getName().replace("_", " ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : name.split(" ")) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }
        
        return formatted.toString();
    }
    
    /**
     * Formats duration in ticks to readable format.
     */
    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
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
     * Adds an effect to the kit.
     */
    public void addEffect(PotionEffectType type) {
        kit.addPotionEffect(new KitPotionEffect(type, 0, -1));
    }
    
    /**
     * Removes an effect from the kit.
     */
    public void removeEffect(int index) {
        List<KitPotionEffect> effects = kit.getPotionEffects();
        if (index >= 0 && index < effects.size()) {
            effects.remove(index);
        }
    }
    
    /**
     * Clears all effects.
     */
    public void clearEffects() {
        kit.clearPotionEffects();
    }
}
