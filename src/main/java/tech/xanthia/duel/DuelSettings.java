package tech.xanthia.duel;

import tech.xanthia.kits.Kit;

/**
 * DuelSettings.java
 * Encapsulates all duel configuration settings.
 * 
 * Settings are merged from:
 * - Kit configuration (Section 3)
 * - Duel overrides
 * - Arena restrictions
 * - Global config.yml rules
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelSettings {
    
    // Combat settings
    private final boolean damageEnabled;
    private final boolean fallDamageEnabled;
    private final boolean hungerEnabled;
    private final boolean buildAllowed;
    private final boolean breakAllowed;
    private final double knockbackMultiplier;
    private final int hitDelay; // Ticks
    
    // Potion settings
    private final boolean potionsEnabled;
    private final boolean splashPotionsEnabled;
    
    // Shield settings
    private final boolean shieldsEnabled;
    private final boolean customShieldSounds;
    
    // Health settings
    private final boolean healthIndicatorsEnabled;
    private final String healthIndicatorType; // "hearts", "actionbar", "both"
    
    // World border settings
    private final boolean worldBorderEnabled;
    private final int worldBorderRadius;
    private final int worldBorderShrinkDelay; // Seconds
    private final int worldBorderShrinkDuration; // Seconds
    
    // Special mode settings
    private final int boxingHitThreshold; // Hits to win in boxing
    private final double sumoFailY; // Y-level for sumo elimination
    private final boolean spleefRegeneration; // Regenerate spleef floor
    
    // Round settings
    private final int roundDuration; // Seconds (0 = unlimited)
    private final int roundCountdown; // Seconds
    
    // Respawn settings
    private final boolean respawnEnabled;
    private final int respawnDelay; // Ticks
    
    // Miscellaneous
    private final boolean allowSpectators;
    private final boolean allowForfeit;
    private final int afkTimeout; // Seconds
    
    /**
     * Create settings from kit configuration
     * 
     * @param kit Kit to extract settings from
     */
    public DuelSettings(Kit kit) {
        // Combat settings (from kit or defaults)
        this.damageEnabled = kit.getConfigBoolean("damage-enabled", true);
        this.fallDamageEnabled = kit.getConfigBoolean("fall-damage", true);
        this.hungerEnabled = kit.getConfigBoolean("hunger-enabled", false);
        this.buildAllowed = kit.getConfigBoolean("build-allowed", false);
        this.breakAllowed = kit.getConfigBoolean("break-allowed", false);
        this.knockbackMultiplier = kit.getConfigDouble("knockback-multiplier", 1.0);
        this.hitDelay = kit.getConfigInt("hit-delay", 20);
        
        // Potion settings
        this.potionsEnabled = kit.getConfigBoolean("potions-enabled", true);
        this.splashPotionsEnabled = kit.getConfigBoolean("splash-potions-enabled", true);
        
        // Shield settings
        this.shieldsEnabled = kit.getConfigBoolean("shields-enabled", true);
        this.customShieldSounds = kit.getConfigBoolean("custom-shield-sounds", true);
        
        // Health settings
        this.healthIndicatorsEnabled = kit.getConfigBoolean("health-indicators", true);
        this.healthIndicatorType = kit.getConfigString("health-indicator-type", "both");
        
        // World border settings
        this.worldBorderEnabled = kit.getConfigBoolean("world-border-enabled", false);
        this.worldBorderRadius = kit.getConfigInt("world-border-radius", 50);
        this.worldBorderShrinkDelay = kit.getConfigInt("world-border-shrink-delay", 60);
        this.worldBorderShrinkDuration = kit.getConfigInt("world-border-shrink-duration", 120);
        
        // Special mode settings
        this.boxingHitThreshold = kit.getConfigInt("boxing-hit-threshold", 100);
        this.sumoFailY = kit.getConfigDouble("sumo-fail-y", 60.0);
        this.spleefRegeneration = kit.getConfigBoolean("spleef-regeneration", true);
        
        // Round settings
        this.roundDuration = kit.getConfigInt("round-duration", 300); // 5 minutes default
        this.roundCountdown = kit.getConfigInt("round-countdown", 3);
        
        // Respawn settings
        this.respawnEnabled = kit.getConfigBoolean("respawn-enabled", false);
        this.respawnDelay = kit.getConfigInt("respawn-delay", 40); // 2 seconds
        
        // Miscellaneous
        this.allowSpectators = kit.getConfigBoolean("allow-spectators", true);
        this.allowForfeit = kit.getConfigBoolean("allow-forfeit", true);
        this.afkTimeout = kit.getConfigInt("afk-timeout", 30);
    }
    
    /**
     * Create settings with custom overrides
     */
    public DuelSettings(Kit kit, boolean damageOverride, double knockbackOverride) {
        this(kit);
        // TODO: Implement override constructor if needed
    }
    
    // Getters
    
    public boolean isDamageEnabled() {
        return damageEnabled;
    }
    
    public boolean isFallDamageEnabled() {
        return fallDamageEnabled;
    }
    
    public boolean isHungerEnabled() {
        return hungerEnabled;
    }
    
    public boolean isBuildAllowed() {
        return buildAllowed;
    }
    
    public boolean isBreakAllowed() {
        return breakAllowed;
    }
    
    public double getKnockbackMultiplier() {
        return knockbackMultiplier;
    }
    
    public int getHitDelay() {
        return hitDelay;
    }
    
    public boolean arePotionsEnabled() {
        return potionsEnabled;
    }
    
    public boolean areSplashPotionsEnabled() {
        return splashPotionsEnabled;
    }
    
    public boolean areShieldsEnabled() {
        return shieldsEnabled;
    }
    
    public boolean hasCustomShieldSounds() {
        return customShieldSounds;
    }
    
    public boolean areHealthIndicatorsEnabled() {
        return healthIndicatorsEnabled;
    }
    
    public String getHealthIndicatorType() {
        return healthIndicatorType;
    }
    
    public boolean isWorldBorderEnabled() {
        return worldBorderEnabled;
    }
    
    public int getWorldBorderRadius() {
        return worldBorderRadius;
    }
    
    public int getWorldBorderShrinkDelay() {
        return worldBorderShrinkDelay;
    }
    
    public int getWorldBorderShrinkDuration() {
        return worldBorderShrinkDuration;
    }
    
    public int getBoxingHitThreshold() {
        return boxingHitThreshold;
    }
    
    public double getSumoFailY() {
        return sumoFailY;
    }
    
    public boolean isSpleefRegeneration() {
        return spleefRegeneration;
    }
    
    public int getRoundDuration() {
        return roundDuration;
    }
    
    public int getRoundCountdown() {
        return roundCountdown;
    }
    
    public boolean isRespawnEnabled() {
        return respawnEnabled;
    }
    
    public int getRespawnDelay() {
        return respawnDelay;
    }
    
    public boolean areSpectatorsAllowed() {
        return allowSpectators;
    }
    
    public boolean isForfeitAllowed() {
        return allowForfeit;
    }
    
    public int getAfkTimeout() {
        return afkTimeout;
    }
}
