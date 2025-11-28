package tech.xanthia.kits;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Represents entity attributes for a kit.
 * 
 * Handles max health, movement speed, attack damage, knockback resistance,
 * and entity scale (1.20.2+).
 * 
 * All attribute modifications must be applied synchronously.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitAttributeSet {
    
    private static final String ATTRIBUTE_MODIFIER_NAME = "xDuels-Kit-Modifier";
    
    private double maxHealth;
    private double movementSpeed;
    private double attackDamage;
    private double knockbackResistance;
    private float scale;
    
    /**
     * Creates a default attribute set with vanilla values.
     */
    public KitAttributeSet() {
        this.maxHealth = 20.0;
        this.movementSpeed = 0.1;
        this.attackDamage = 1.0;
        this.knockbackResistance = 0.0;
        this.scale = 1.0f;
    }
    
    /**
     * Creates an attribute set with custom values.
     * 
     * @param maxHealth Max health (default 20.0)
     * @param movementSpeed Movement speed (default 0.1)
     * @param attackDamage Attack damage (default 1.0)
     * @param knockbackResistance Knockback resistance (default 0.0)
     * @param scale Entity scale (default 1.0)
     */
    public KitAttributeSet(double maxHealth, double movementSpeed, double attackDamage, 
                           double knockbackResistance, float scale) {
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.attackDamage = attackDamage;
        this.knockbackResistance = knockbackResistance;
        this.scale = scale;
    }
    
    // Getters
    public double getMaxHealth() { return maxHealth; }
    public double getMovementSpeed() { return movementSpeed; }
    public double getAttackDamage() { return attackDamage; }
    public double getKnockbackResistance() { return knockbackResistance; }
    public float getScale() { return scale; }
    
    // Setters
    public void setMaxHealth(double maxHealth) { 
        this.maxHealth = Math.max(1.0, Math.min(1024.0, maxHealth)); 
    }
    public void setMovementSpeed(double movementSpeed) { 
        this.movementSpeed = Math.max(0.0, Math.min(1.0, movementSpeed)); 
    }
    public void setAttackDamage(double attackDamage) { 
        this.attackDamage = Math.max(0.0, attackDamage); 
    }
    public void setKnockbackResistance(double knockbackResistance) { 
        this.knockbackResistance = Math.max(0.0, Math.min(1.0, knockbackResistance)); 
    }
    public void setScale(float scale) { 
        this.scale = Math.max(0.0625f, Math.min(16.0f, scale)); 
    }
    
    /**
     * Applies this attribute set to a player.
     * 
     * MUST be called synchronously on the main thread.
     * 
     * @param player Target player
     */
    public void applyTo(Player player) {
        // Max Health
        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(maxHealth);
            // Heal to max health
            player.setHealth(Math.min(player.getHealth(), maxHealth));
        }
        
        // Movement Speed
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(movementSpeed);
        }
        
        // Attack Damage
        AttributeInstance attackAttr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(attackDamage);
        }
        
        // Knockback Resistance
        AttributeInstance kbAttr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.setBaseValue(knockbackResistance);
        }
        
        // Scale (1.21+ only, not available in 1.20.2)
        // Commented out for 1.20.2 compatibility
        /*
        // GENERIC_SCALE not available in 1.20.2 Paper API
        /*
        try {
            AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(1.0);
            }
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            // Scale attribute not available in this version
        }
        */
    }
    
    /**
     * Removes kit attributes and restores vanilla defaults.
     * 
     * MUST be called synchronously on the main thread.
     * 
     * @param player Target player
     */
    public void removeFrom(Player player) {
        // Reset to vanilla defaults
        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(20.0);
            player.setHealth(Math.min(player.getHealth(), 20.0));
        }
        
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(0.1);
        }
        
        AttributeInstance attackAttr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(1.0);
        }
        
        AttributeInstance kbAttr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.setBaseValue(0.0);
        }
        
        // GENERIC_SCALE not available in 1.20.2 Paper API
        /*
        try {
            AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(1.0);
            }
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            // Scale attribute not available
        }
        */
    }
    
    /**
     * Serializes this attribute set to YAML.
     * 
     * @param section Configuration section to write to
     * @param path Path within section (typically "attributes")
     */
    public void serialize(ConfigurationSection section, String path) {
        section.set(path + ".maxHealth", maxHealth);
        section.set(path + ".movementSpeed", movementSpeed);
        section.set(path + ".attackDamage", attackDamage);
        section.set(path + ".knockbackResistance", knockbackResistance);
        section.set(path + ".scale", scale);
    }
    
    /**
     * Deserializes an attribute set from YAML.
     * 
     * @param section Configuration section to read from
     * @param path Path within section (typically "attributes")
     * @return Deserialized attribute set
     */
    public static KitAttributeSet deserialize(ConfigurationSection section, String path) {
        ConfigurationSection attrSection = section.getConfigurationSection(path);
        if (attrSection == null) {
            return new KitAttributeSet(); // Return defaults
        }
        
        double maxHealth = attrSection.getDouble("maxHealth", 20.0);
        double movementSpeed = attrSection.getDouble("movementSpeed", 0.1);
        double attackDamage = attrSection.getDouble("attackDamage", 1.0);
        double knockbackResistance = attrSection.getDouble("knockbackResistance", 0.0);
        float scale = (float) attrSection.getDouble("scale", 1.0);
        
        return new KitAttributeSet(maxHealth, movementSpeed, attackDamage, knockbackResistance, scale);
    }
    
    /**
     * Creates a copy of this attribute set.
     * 
     * @return Cloned attribute set
     */
    public KitAttributeSet clone() {
        return new KitAttributeSet(maxHealth, movementSpeed, attackDamage, knockbackResistance, scale);
    }
    
    /**
     * Checks if this attribute set uses vanilla defaults.
     * 
     * @return True if all attributes are vanilla
     */
    public boolean isVanilla() {
        return maxHealth == 20.0 && 
               movementSpeed == 0.1 && 
               attackDamage == 1.0 && 
               knockbackResistance == 0.0 && 
               scale == 1.0f;
    }
}
