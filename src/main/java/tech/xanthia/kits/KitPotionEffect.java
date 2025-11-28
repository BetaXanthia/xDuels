package tech.xanthia.kits;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents a custom potion effect configuration for kits.
 * 
 * Wraps Bukkit's PotionEffect with serialization support and
 * special handling for infinite duration effects.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitPotionEffect {
    
    private final PotionEffectType type;
    private final int amplifier;
    private final int duration; // -1 = infinite
    
    /**
     * Creates a new kit potion effect.
     * 
     * @param type Effect type
     * @param amplifier Effect amplifier (0 = level I)
     * @param duration Duration in ticks (-1 for infinite)
     */
    public KitPotionEffect(PotionEffectType type, int amplifier, int duration) {
        this.type = type;
        this.amplifier = amplifier;
        this.duration = duration;
    }
    
    /**
     * Gets the effect type.
     * 
     * @return Effect type
     */
    public PotionEffectType getType() {
        return type;
    }
    
    /**
     * Gets the effect amplifier.
     * 
     * @return Amplifier (0 = level I)
     */
    public int getAmplifier() {
        return amplifier;
    }
    
    /**
     * Gets the effect duration in ticks.
     * 
     * @return Duration (-1 for infinite)
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Checks if this effect is infinite.
     * 
     * @return True if duration is -1
     */
    public boolean isInfinite() {
        return duration == -1;
    }
    
    /**
     * Converts this to a Bukkit PotionEffect.
     * 
     * @return Bukkit potion effect
     */
    public PotionEffect toBukkit() {
        int effectDuration = isInfinite() ? Integer.MAX_VALUE : duration;
        return new PotionEffect(type, effectDuration, amplifier, false, false, true);
    }
    
    /**
     * Applies this effect to a player.
     * 
     * @param player Target player
     */
    public void applyTo(Player player) {
        player.addPotionEffect(toBukkit());
    }
    
    /**
     * Removes this effect from a player.
     * 
     * @param player Target player
     */
    public void removeFrom(Player player) {
        player.removePotionEffect(type);
    }
    
    /**
     * Serializes this effect to YAML.
     * 
     * @param section Configuration section to write to
     * @param path Path within section
     */
    public void serialize(ConfigurationSection section, String path) {
        section.set(path + ".type", type.getName());
        section.set(path + ".amplifier", amplifier);
        section.set(path + ".duration", duration);
    }
    
    /**
     * Deserializes an effect from YAML.
     * 
     * @param section Configuration section to read from
     * @param path Path within section
     * @return Deserialized effect or null if invalid
     */
    public static KitPotionEffect deserialize(ConfigurationSection section, String path) {
        String typeName = section.getString(path + ".type");
        if (typeName == null) {
            return null;
        }
        
        PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) {
            return null;
        }
        
        int amplifier = section.getInt(path + ".amplifier", 0);
        int duration = section.getInt(path + ".duration", -1);
        
        return new KitPotionEffect(type, amplifier, duration);
    }
    
    /**
     * Creates a copy of this effect.
     * 
     * @return Cloned effect
     */
    public KitPotionEffect clone() {
        return new KitPotionEffect(type, amplifier, duration);
    }
    
    @Override
    public String toString() {
        return type.getName() + " " + (amplifier + 1) + " " + 
               (isInfinite() ? "∞" : duration + "t");
    }
}
