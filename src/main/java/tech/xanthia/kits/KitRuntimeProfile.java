package tech.xanthia.kits;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime profile for a kit used during an active duel.
 * 
 * Generated once when a duel instance is created, not per-tick.
 * Stores cached data for maximum performance during duel execution.
 * 
 * Boxing hit counters are stored here per-player.
 * KB sync state is tracked per-duel.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitRuntimeProfile {
    
    private final Kit kit;
    
    // Cached attributes
    private final KitAttributeSet attributeSet;
    private final List<KitPotionEffect> potionEffects;
    private final KitRuleSet ruleSet;
    
    // Cached loadout (to avoid re-cloning every round)
    private final ItemStack[] contentsSnapshot;
    private final ItemStack[] armorSnapshot;
    
    // Boxing mode hit tracking
    private final Map<String, Integer> boxingHits; // player UUID -> hit count
    
    // KB sync state
    private boolean kbSyncActive;
    
    // Round timer override
    private int roundTimerOverride; // -1 = use default
    
    /**
     * Creates a new runtime profile from a kit.
     * 
     * @param kit Source kit
     */
    public KitRuntimeProfile(Kit kit) {
        this.kit = kit;
        
        // Cache attributes and effects
        this.attributeSet = kit.getAttributeSet().clone();
        this.potionEffects = List.copyOf(kit.getPotionEffects());
        this.ruleSet = kit.getRuleSet().clone();
        
        // Snapshot loadout
        this.contentsSnapshot = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            if (kit.getContents()[i] != null) {
                this.contentsSnapshot[i] = kit.getContents()[i].clone();
            }
        }
        
        this.armorSnapshot = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            if (kit.getArmorContents()[i] != null) {
                this.armorSnapshot[i] = kit.getArmorContents()[i].clone();
            }
        }
        
        // Initialize boxing tracking
        this.boxingHits = new HashMap<>();
        
        // Initialize KB sync
        this.kbSyncActive = kit.isKbSync();
        
        // Initialize round timer
        this.roundTimerOverride = -1;
    }
    
    /**
     * Gets the source kit.
     * 
     * @return Source kit
     */
    public Kit getKit() {
        return kit;
    }
    
    /**
     * Gets the cached attribute set.
     * 
     * @return Attribute set
     */
    public KitAttributeSet getAttributeSet() {
        return attributeSet;
    }
    
    /**
     * Gets the cached potion effects.
     * 
     * @return Immutable list of potion effects
     */
    public List<KitPotionEffect> getPotionEffects() {
        return potionEffects;
    }
    
    /**
     * Gets the cached rule set.
     * 
     * @return Rule set
     */
    public KitRuleSet getRuleSet() {
        return ruleSet;
    }
    
    /**
     * Gets a fresh copy of the inventory contents.
     * 
     * @return Cloned inventory contents
     */
    public ItemStack[] getContentsSnapshot() {
        ItemStack[] copy = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            if (contentsSnapshot[i] != null) {
                copy[i] = contentsSnapshot[i].clone();
            }
        }
        return copy;
    }
    
    /**
     * Gets a fresh copy of the armor contents.
     * 
     * @return Cloned armor contents
     */
    public ItemStack[] getArmorSnapshot() {
        ItemStack[] copy = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            if (armorSnapshot[i] != null) {
                copy[i] = armorSnapshot[i].clone();
            }
        }
        return copy;
    }
    
    /**
     * Gets boxing hits for a player.
     * 
     * @param playerUUID Player UUID
     * @return Hit count
     */
    public int getBoxingHits(String playerUUID) {
        return boxingHits.getOrDefault(playerUUID, 0);
    }
    
    /**
     * Increments boxing hits for a player.
     * 
     * @param playerUUID Player UUID
     * @return New hit count
     */
    public int incrementBoxingHits(String playerUUID) {
        int hits = boxingHits.getOrDefault(playerUUID, 0) + 1;
        boxingHits.put(playerUUID, hits);
        return hits;
    }
    
    /**
     * Resets boxing hits for a player.
     * 
     * @param playerUUID Player UUID
     */
    public void resetBoxingHits(String playerUUID) {
        boxingHits.put(playerUUID, 0);
    }
    
    /**
     * Resets all boxing hits (called at round start).
     */
    public void resetAllBoxingHits() {
        boxingHits.clear();
    }
    
    /**
     * Checks if KB sync is active.
     * 
     * @return True if KB sync enabled
     */
    public boolean isKbSyncActive() {
        return kbSyncActive;
    }
    
    /**
     * Sets KB sync state.
     * 
     * @param kbSyncActive KB sync state
     */
    public void setKbSyncActive(boolean kbSyncActive) {
        this.kbSyncActive = kbSyncActive;
    }
    
    /**
     * Gets the round timer override.
     * 
     * @return Round timer in seconds (-1 for default)
     */
    public int getRoundTimerOverride() {
        return roundTimerOverride;
    }
    
    /**
     * Sets the round timer override.
     * 
     * @param roundTimerOverride Round timer in seconds (-1 for default)
     */
    public void setRoundTimerOverride(int roundTimerOverride) {
        this.roundTimerOverride = roundTimerOverride;
    }
    
    /**
     * Gets the effective round timer.
     * 
     * @param defaultTimer Default timer from duel config
     * @return Effective timer in seconds
     */
    public int getEffectiveRoundTimer(int defaultTimer) {
        return roundTimerOverride == -1 ? defaultTimer : roundTimerOverride;
    }
    
    /**
     * Checks if this is a boxing mode kit.
     * 
     * @return True if boxing mode
     */
    public boolean isBoxingMode() {
        return kit.isBoxingMode();
    }
    
    /**
     * Checks if this is a sumo mode kit.
     * 
     * @return True if sumo mode
     */
    public boolean isSumoMode() {
        return kit.isSumoMode();
    }
    
    /**
     * Checks if this is a spleef mode kit.
     * 
     * @return True if spleef mode
     */
    public boolean isSpleefMode() {
        return kit.isSpleefMode();
    }
    
    /**
     * Checks if health indicators are enabled.
     * 
     * @return True if health indicators enabled
     */
    public boolean isHealthIndicators() {
        return kit.isHealthIndicators();
    }
}
