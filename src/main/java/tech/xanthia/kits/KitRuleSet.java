package tech.xanthia.kits;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents gameplay rules for a kit.
 * 
 * Controls all mechanics including hunger, building, damage,
 * regeneration, drops, and various indicators.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitRuleSet {
    
    // Gameplay rules
    private boolean hunger;
    private boolean build;
    private boolean breakBlocks;
    private boolean onlyBreakPlayerPlaced;
    private boolean fallDamage;
    private boolean damageEnabled;
    private boolean regen;
    private boolean crafting;
    
    // Drop/Pickup rules
    private boolean dropsAllowed;
    private boolean pickupAllowed;
    
    // Start timer
    private int startTimerSeconds;
    
    // Indicators
    private boolean healthIndicators;
    private boolean kbSync;
    
    /**
     * Creates a default rule set with standard PvP settings.
     */
    public KitRuleSet() {
        this.hunger = true;
        this.build = false;
        this.breakBlocks = false;
        this.onlyBreakPlayerPlaced = false;
        this.fallDamage = true;
        this.damageEnabled = true;
        this.regen = true;
        this.crafting = false;
        this.dropsAllowed = true;
        this.pickupAllowed = true;
        this.startTimerSeconds = 3;
        this.healthIndicators = true;
        this.kbSync = false;
    }
    
    // Getters
    public boolean isHunger() { return hunger; }
    public boolean isBuild() { return build; }
    public boolean isBreakBlocks() { return breakBlocks; }
    public boolean isOnlyBreakPlayerPlaced() { return onlyBreakPlayerPlaced; }
    public boolean isFallDamage() { return fallDamage; }
    public boolean isDamageEnabled() { return damageEnabled; }
    public boolean isRegen() { return regen; }
    public boolean isCrafting() { return crafting; }
    public boolean isDropsAllowed() { return dropsAllowed; }
    public boolean isPickupAllowed() { return pickupAllowed; }
    public int getStartTimerSeconds() { return startTimerSeconds; }
    public boolean isHealthIndicators() { return healthIndicators; }
    public boolean isKbSync() { return kbSync; }
    
    // Setters
    public void setHunger(boolean hunger) { this.hunger = hunger; }
    public void setBuild(boolean build) { this.build = build; }
    public void setBreakBlocks(boolean breakBlocks) { this.breakBlocks = breakBlocks; }
    public void setOnlyBreakPlayerPlaced(boolean onlyBreakPlayerPlaced) { 
        this.onlyBreakPlayerPlaced = onlyBreakPlayerPlaced; 
    }
    public void setFallDamage(boolean fallDamage) { this.fallDamage = fallDamage; }
    public void setDamageEnabled(boolean damageEnabled) { this.damageEnabled = damageEnabled; }
    public void setRegen(boolean regen) { this.regen = regen; }
    public void setCrafting(boolean crafting) { this.crafting = crafting; }
    public void setDropsAllowed(boolean dropsAllowed) { this.dropsAllowed = dropsAllowed; }
    public void setPickupAllowed(boolean pickupAllowed) { this.pickupAllowed = pickupAllowed; }
    public void setStartTimerSeconds(int startTimerSeconds) { 
        this.startTimerSeconds = Math.max(0, startTimerSeconds); 
    }
    public void setHealthIndicators(boolean healthIndicators) { 
        this.healthIndicators = healthIndicators; 
    }
    public void setKbSync(boolean kbSync) { this.kbSync = kbSync; }
    
    /**
     * Serializes this rule set to YAML.
     * 
     * @param section Configuration section to write to
     * @param path Path within section (typically "rules")
     */
    public void serialize(ConfigurationSection section, String path) {
        section.set(path + ".hunger", hunger);
        section.set(path + ".build", build);
        section.set(path + ".breakBlocks", breakBlocks);
        section.set(path + ".onlyBreakPlayerPlaced", onlyBreakPlayerPlaced);
        section.set(path + ".fallDamage", fallDamage);
        section.set(path + ".damageEnabled", damageEnabled);
        section.set(path + ".regen", regen);
        section.set(path + ".crafting", crafting);
        section.set(path + ".dropsAllowed", dropsAllowed);
        section.set(path + ".pickupAllowed", pickupAllowed);
        section.set(path + ".startTimerSeconds", startTimerSeconds);
        section.set(path + ".healthIndicators", healthIndicators);
        section.set(path + ".kbSync", kbSync);
    }
    
    /**
     * Deserializes a rule set from YAML.
     * 
     * @param section Configuration section to read from
     * @param path Path within section (typically "rules")
     * @return Deserialized rule set
     */
    public static KitRuleSet deserialize(ConfigurationSection section, String path) {
        KitRuleSet rules = new KitRuleSet();
        
        ConfigurationSection rulesSection = section.getConfigurationSection(path);
        if (rulesSection == null) {
            return rules; // Return defaults
        }
        
        rules.hunger = rulesSection.getBoolean("hunger", true);
        rules.build = rulesSection.getBoolean("build", false);
        rules.breakBlocks = rulesSection.getBoolean("breakBlocks", false);
        rules.onlyBreakPlayerPlaced = rulesSection.getBoolean("onlyBreakPlayerPlaced", false);
        rules.fallDamage = rulesSection.getBoolean("fallDamage", true);
        rules.damageEnabled = rulesSection.getBoolean("damageEnabled", true);
        rules.regen = rulesSection.getBoolean("regen", true);
        rules.crafting = rulesSection.getBoolean("crafting", false);
        rules.dropsAllowed = rulesSection.getBoolean("dropsAllowed", true);
        rules.pickupAllowed = rulesSection.getBoolean("pickupAllowed", true);
        rules.startTimerSeconds = rulesSection.getInt("startTimerSeconds", 3);
        rules.healthIndicators = rulesSection.getBoolean("healthIndicators", true);
        rules.kbSync = rulesSection.getBoolean("kbSync", false);
        
        return rules;
    }
    
    /**
     * Creates a copy of this rule set.
     * 
     * @return Cloned rule set
     */
    public KitRuleSet clone() {
        KitRuleSet clone = new KitRuleSet();
        clone.hunger = this.hunger;
        clone.build = this.build;
        clone.breakBlocks = this.breakBlocks;
        clone.onlyBreakPlayerPlaced = this.onlyBreakPlayerPlaced;
        clone.fallDamage = this.fallDamage;
        clone.damageEnabled = this.damageEnabled;
        clone.regen = this.regen;
        clone.crafting = this.crafting;
        clone.dropsAllowed = this.dropsAllowed;
        clone.pickupAllowed = this.pickupAllowed;
        clone.startTimerSeconds = this.startTimerSeconds;
        clone.healthIndicators = this.healthIndicators;
        clone.kbSync = this.kbSync;
        return clone;
    }
    
    /**
     * Validates this rule set for conflicts.
     * 
     * @return True if valid
     */
    public boolean validate() {
        // If only break player-placed is enabled, breakBlocks must also be enabled
        if (onlyBreakPlayerPlaced && !breakBlocks) {
            return false;
        }
        
        // Start timer cannot be negative
        if (startTimerSeconds < 0) {
            return false;
        }
        
        return true;
    }
}
