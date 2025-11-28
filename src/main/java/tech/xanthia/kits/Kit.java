package tech.xanthia.kits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Represents a complete duel kit profile.
 * 
 * Contains all data needed for a duel configuration including:
 * - Display information
 * - Inventory loadouts
 * - Rules and attributes
 * - Potion effects
 * - Special modes (boxing, sumo, spleef)
 * - Round configuration
 * 
 * All kits are cached in RAM for high performance.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class Kit {
    
    // Core identification
    private final String name;
    private boolean enabled;
    
    // Display
    private ItemStack displayItem;
    
    // Round configuration
    private int unrankedRounds;
    private int rankedRounds;
    private boolean allowInQueueUnranked;
    private boolean allowInQueueRanked;
    
    // Inventory & Equipment (stored as Base64)
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    
    // Rules and attributes
    private KitRuleSet ruleSet;
    private KitAttributeSet attributeSet;
    
    // Potion effects
    private List<KitPotionEffect> potionEffects;
    
    // Mode flags
    private boolean lokaDamage;
    private boolean boxingMode;
    private boolean sumoMode;
    private boolean spleefMode;
    private boolean healthIndicators;
    private boolean kbSync;
    private boolean itemDropAllowed;
    private boolean itemPickupAllowed;
    private boolean virtualEditorAllowed;
    
    // Entity overrides
    private int tntFuseOverride; // -1 = default
    private int creeperIgniteOverride; // -1 = default
    private int arrowDespawnOverride; // -1 = default
    
    /**
     * Creates a new kit with default values.
     * 
     * @param name Kit name (must be unique)
     */
    public Kit(String name) {
        this.name = name;
        this.enabled = false;
        this.displayItem = new ItemStack(Material.DIAMOND_SWORD);
        this.unrankedRounds = 1;
        this.rankedRounds = 3;
        this.allowInQueueUnranked = true;
        this.allowInQueueRanked = true;
        this.contents = new ItemStack[36];
        this.armorContents = new ItemStack[4];
        this.ruleSet = new KitRuleSet();
        this.attributeSet = new KitAttributeSet();
        this.potionEffects = new ArrayList<>();
        this.lokaDamage = false;
        this.boxingMode = false;
        this.sumoMode = false;
        this.spleefMode = false;
        this.healthIndicators = true;
        this.kbSync = false;
        this.itemDropAllowed = true;
        this.itemPickupAllowed = true;
        this.virtualEditorAllowed = true;
        this.tntFuseOverride = -1;
        this.creeperIgniteOverride = -1;
        this.arrowDespawnOverride = -1;
    }
    
    // Core getters
    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public ItemStack getDisplayItem() { return displayItem; }
    public int getUnrankedRounds() { return unrankedRounds; }
    public int getRankedRounds() { return rankedRounds; }
    public boolean isAllowInQueueUnranked() { return allowInQueueUnranked; }
    public boolean isAllowInQueueRanked() { return allowInQueueRanked; }
    public ItemStack[] getContents() { return contents; }
    public ItemStack[] getArmorContents() { return armorContents; }
    public KitRuleSet getRuleSet() { return ruleSet; }
    public KitAttributeSet getAttributeSet() { return attributeSet; }
    public List<KitPotionEffect> getPotionEffects() { return potionEffects; }
    
    // Mode getters
    public boolean isLokaDamage() { return lokaDamage; }
    public boolean isBoxingMode() { return boxingMode; }
    public boolean isSumoMode() { return sumoMode; }
    public boolean isSpleefMode() { return spleefMode; }
    public boolean isHealthIndicators() { return healthIndicators; }
    public boolean isKbSync() { return kbSync; }
    public boolean isItemDropAllowed() { return itemDropAllowed; }
    public boolean isItemPickupAllowed() { return itemPickupAllowed; }
    public boolean isVirtualEditorAllowed() { return virtualEditorAllowed; }
    
    // Override getters
    public int getTntFuseOverride() { return tntFuseOverride; }
    public int getCreeperIgniteOverride() { return creeperIgniteOverride; }
    public int getArrowDespawnOverride() { return arrowDespawnOverride; }
    
    // Core setters
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDisplayItem(ItemStack displayItem) { this.displayItem = displayItem; }
    public void setUnrankedRounds(int unrankedRounds) { 
        this.unrankedRounds = Math.max(1, unrankedRounds); 
    }
    public void setRankedRounds(int rankedRounds) { 
        this.rankedRounds = Math.max(1, rankedRounds); 
    }
    public void setAllowInQueueUnranked(boolean allowInQueueUnranked) { 
        this.allowInQueueUnranked = allowInQueueUnranked; 
    }
    public void setAllowInQueueRanked(boolean allowInQueueRanked) { 
        this.allowInQueueRanked = allowInQueueRanked; 
    }
    public void setContents(ItemStack[] contents) { this.contents = contents; }
    public void setArmorContents(ItemStack[] armorContents) { this.armorContents = armorContents; }
    public void setRuleSet(KitRuleSet ruleSet) { this.ruleSet = ruleSet; }
    public void setAttributeSet(KitAttributeSet attributeSet) { this.attributeSet = attributeSet; }
    public void setPotionEffects(List<KitPotionEffect> potionEffects) { 
        this.potionEffects = potionEffects; 
    }
    
    // Mode setters
    public void setLokaDamage(boolean lokaDamage) { this.lokaDamage = lokaDamage; }
    public void setBoxingMode(boolean boxingMode) { this.boxingMode = boxingMode; }
    public void setSumoMode(boolean sumoMode) { this.sumoMode = sumoMode; }
    public void setSpleefMode(boolean spleefMode) { this.spleefMode = spleefMode; }
    public void setHealthIndicators(boolean healthIndicators) { 
        this.healthIndicators = healthIndicators; 
    }
    public void setKbSync(boolean kbSync) { this.kbSync = kbSync; }
    public void setItemDropAllowed(boolean itemDropAllowed) { 
        this.itemDropAllowed = itemDropAllowed; 
    }
    public void setItemPickupAllowed(boolean itemPickupAllowed) { 
        this.itemPickupAllowed = itemPickupAllowed; 
    }
    public void setVirtualEditorAllowed(boolean virtualEditorAllowed) { 
        this.virtualEditorAllowed = virtualEditorAllowed; 
    }
    
    // Override setters
    public void setTntFuseOverride(int tntFuseOverride) { 
        this.tntFuseOverride = tntFuseOverride; 
    }
    public void setCreeperIgniteOverride(int creeperIgniteOverride) { 
        this.creeperIgniteOverride = creeperIgniteOverride; 
    }
    public void setArrowDespawnOverride(int arrowDespawnOverride) { 
        this.arrowDespawnOverride = arrowDespawnOverride; 
    }
    
    /**
     * Adds a potion effect to this kit.
     * 
     * @param effect Effect to add
     */
    public void addPotionEffect(KitPotionEffect effect) {
        potionEffects.add(effect);
    }
    
    /**
     * Removes a potion effect from this kit.
     * 
     * @param effect Effect to remove
     */
    public void removePotionEffect(KitPotionEffect effect) {
        potionEffects.remove(effect);
    }
    
    /**
     * Clears all potion effects.
     */
    public void clearPotionEffects() {
        potionEffects.clear();
    }
    
    /**
     * Serializes this kit to YAML.
     * 
     * @param section Configuration section to write to
     */
    public void serialize(ConfigurationSection section) {
        section.set("enabled", enabled);
        section.set("displayItem", displayItem);
        section.set("unrankedRounds", unrankedRounds);
        section.set("rankedRounds", rankedRounds);
        section.set("allowInQueueUnranked", allowInQueueUnranked);
        section.set("allowInQueueRanked", allowInQueueRanked);
        
        // Serialize inventories as Base64
        section.set("contents", itemStackArrayToBase64(contents));
        section.set("armorContents", itemStackArrayToBase64(armorContents));
        
        // Serialize rules and attributes
        ruleSet.serialize(section, "rules");
        attributeSet.serialize(section, "attributes");
        
        // Serialize potion effects
        List<String> effectStrings = new ArrayList<>();
        for (int i = 0; i < potionEffects.size(); i++) {
            KitPotionEffect effect = potionEffects.get(i);
            effect.serialize(section, "effects." + i);
        }
        
        // Mode flags
        section.set("lokaDamage", lokaDamage);
        section.set("boxingMode", boxingMode);
        section.set("sumoMode", sumoMode);
        section.set("spleefMode", spleefMode);
        section.set("healthIndicators", healthIndicators);
        section.set("kbSync", kbSync);
        section.set("itemDropAllowed", itemDropAllowed);
        section.set("itemPickupAllowed", itemPickupAllowed);
        section.set("virtualEditorAllowed", virtualEditorAllowed);
        
        // Overrides
        if (tntFuseOverride != -1) section.set("tntFuseOverride", tntFuseOverride);
        if (creeperIgniteOverride != -1) section.set("creeperIgniteOverride", creeperIgniteOverride);
        if (arrowDespawnOverride != -1) section.set("arrowDespawnOverride", arrowDespawnOverride);
    }
    
    /**
     * Deserializes a kit from YAML.
     * 
     * @param name Kit name
     * @param section Configuration section to read from
     * @return Deserialized kit
     */
    public static Kit deserialize(String name, ConfigurationSection section) {
        Kit kit = new Kit(name);
        
        kit.enabled = section.getBoolean("enabled", false);
        kit.displayItem = section.getItemStack("displayItem", new ItemStack(Material.DIAMOND_SWORD));
        kit.unrankedRounds = section.getInt("unrankedRounds", 1);
        kit.rankedRounds = section.getInt("rankedRounds", 3);
        kit.allowInQueueUnranked = section.getBoolean("allowInQueueUnranked", true);
        kit.allowInQueueRanked = section.getBoolean("allowInQueueRanked", true);
        
        // Deserialize inventories from Base64
        String contentsBase64 = section.getString("contents", "");
        String armorBase64 = section.getString("armorContents", "");
        kit.contents = itemStackArrayFromBase64(contentsBase64, 36);
        kit.armorContents = itemStackArrayFromBase64(armorBase64, 4);
        
        // Deserialize rules and attributes
        kit.ruleSet = KitRuleSet.deserialize(section, "rules");
        kit.attributeSet = KitAttributeSet.deserialize(section, "attributes");
        
        // Deserialize potion effects
        ConfigurationSection effectsSection = section.getConfigurationSection("effects");
        if (effectsSection != null) {
            for (String key : effectsSection.getKeys(false)) {
                KitPotionEffect effect = KitPotionEffect.deserialize(section, "effects." + key);
                if (effect != null) {
                    kit.potionEffects.add(effect);
                }
            }
        }
        
        // Mode flags
        kit.lokaDamage = section.getBoolean("lokaDamage", false);
        kit.boxingMode = section.getBoolean("boxingMode", false);
        kit.sumoMode = section.getBoolean("sumoMode", false);
        kit.spleefMode = section.getBoolean("spleefMode", false);
        kit.healthIndicators = section.getBoolean("healthIndicators", true);
        kit.kbSync = section.getBoolean("kbSync", false);
        kit.itemDropAllowed = section.getBoolean("itemDropAllowed", true);
        kit.itemPickupAllowed = section.getBoolean("itemPickupAllowed", true);
        kit.virtualEditorAllowed = section.getBoolean("virtualEditorAllowed", true);
        
        // Overrides
        kit.tntFuseOverride = section.getInt("tntFuseOverride", -1);
        kit.creeperIgniteOverride = section.getInt("creeperIgniteOverride", -1);
        kit.arrowDespawnOverride = section.getInt("arrowDespawnOverride", -1);
        
        return kit;
    }
    
    /**
     * Converts an ItemStack array to Base64 string.
     * 
     * @param items Item array
     * @return Base64 encoded string
     */
    private static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Converts a Base64 string to ItemStack array.
     * 
     * @param data Base64 encoded string
     * @param size Expected array size
     * @return ItemStack array
     */
    private static ItemStack[] itemStackArrayFromBase64(String data, int size) {
        ItemStack[] items = new ItemStack[size];
        
        if (data == null || data.isEmpty()) {
            return items;
        }
        
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            int length = dataInput.readInt();
            for (int i = 0; i < length && i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Creates a deep copy of this kit.
     * 
     * @return Cloned kit
     */
    public Kit clone() {
        Kit clone = new Kit(this.name + "_copy");
        clone.enabled = this.enabled;
        clone.displayItem = this.displayItem.clone();
        clone.unrankedRounds = this.unrankedRounds;
        clone.rankedRounds = this.rankedRounds;
        clone.allowInQueueUnranked = this.allowInQueueUnranked;
        clone.allowInQueueRanked = this.allowInQueueRanked;
        
        // Clone inventories
        clone.contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            if (this.contents[i] != null) {
                clone.contents[i] = this.contents[i].clone();
            }
        }
        
        clone.armorContents = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            if (this.armorContents[i] != null) {
                clone.armorContents[i] = this.armorContents[i].clone();
            }
        }
        
        clone.ruleSet = this.ruleSet.clone();
        clone.attributeSet = this.attributeSet.clone();
        
        // Clone potion effects
        for (KitPotionEffect effect : this.potionEffects) {
            clone.potionEffects.add(effect.clone());
        }
        
        // Copy mode flags
        clone.lokaDamage = this.lokaDamage;
        clone.boxingMode = this.boxingMode;
        clone.sumoMode = this.sumoMode;
        clone.spleefMode = this.spleefMode;
        clone.healthIndicators = this.healthIndicators;
        clone.kbSync = this.kbSync;
        clone.itemDropAllowed = this.itemDropAllowed;
        clone.itemPickupAllowed = this.itemPickupAllowed;
        clone.virtualEditorAllowed = this.virtualEditorAllowed;
        
        // Copy overrides
        clone.tntFuseOverride = this.tntFuseOverride;
        clone.creeperIgniteOverride = this.creeperIgniteOverride;
        clone.arrowDespawnOverride = this.arrowDespawnOverride;
        
        return clone;
    }
    
    // ========================================
    // CONFIG HELPER METHODS
    // ========================================
    
    /**
     * Get a boolean configuration value with default.
     * This method is used by DuelSettings for backward compatibility.
     * 
     * @param key Config key
     * @param defaultValue Default value
     * @return Boolean value
     */
    public boolean getConfigBoolean(String key, boolean defaultValue) {
        // For now, return defaults based on common kit settings
        // This would normally read from kit metadata or config section
        switch (key) {
            case "damage-enabled": return true;
            case "fall-damage": return true;
            case "hunger-enabled": return false;
            case "build-allowed": return false;
            case "break-allowed": return false;
            case "potions-enabled": return true;
            case "splash-potions-enabled": return true;
            case "shields-enabled": return true;
            case "custom-shield-sounds": return true;
            case "health-indicators": return healthIndicators;
            case "world-border-enabled": return false;
            case "spleef-regeneration": return spleefMode;
            case "respawn-enabled": return false;
            case "allow-spectators": return true;
            case "allow-forfeit": return true;
            default: return defaultValue;
        }
    }
    
    /**
     * Get an integer configuration value with default.
     * 
     * @param key Config key
     * @param defaultValue Default value
     * @return Integer value
     */
    public int getConfigInt(String key, int defaultValue) {
        switch (key) {
            case "hit-delay": return 20;
            case "world-border-radius": return 50;
            case "world-border-shrink-delay": return 60;
            case "world-border-shrink-duration": return 120;
            case "boxing-hit-threshold": return 100;
            case "round-duration": return 300;
            case "round-countdown": return 3;
            case "respawn-delay": return 40;
            case "afk-timeout": return 30;
            default: return defaultValue;
        }
    }
    
    /**
     * Get a double configuration value with default.
     * 
     * @param key Config key
     * @param defaultValue Default value
     * @return Double value
     */
    public double getConfigDouble(String key, double defaultValue) {
        switch (key) {
            case "knockback-multiplier": return 1.0;
            case "sumo-fail-y": return 60.0;
            default: return defaultValue;
        }
    }
    
    /**
     * Get a string configuration value with default.
     * 
     * @param key Config key
     * @param defaultValue Default value
     * @return String value
     */
    public String getConfigString(String key, String defaultValue) {
        switch (key) {
            case "health-indicator-type": return "both";
            default: return defaultValue;
        }
    }
}
