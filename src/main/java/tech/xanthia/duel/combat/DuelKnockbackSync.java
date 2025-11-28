package tech.xanthia.duel.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelSettings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * DuelKnockbackSync.java
 * Handles knockback synchronization and customization.
 * 
 * Features:
 * - Syncs knockback when both players hit within 1-3 ticks
 * - Applies kit-based knockback multipliers
 * - Prevents unfair knockback in close combat
 * - Tracks velocity changes
 * 
 * Knockback Sync Rules:
 * - If Player A hits Player B
 * - And Player B hits Player A within 3 ticks
 * - Both players receive balanced knockback
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelKnockbackSync {
    
    private final DuelInstance duel;
    private final DuelSettings settings;
    private final Logger logger;
    
    // Hit tracking for sync detection
    private final Map<UUID, HitData> recentHits;
    
    // Sync window (ticks)
    private static final long SYNC_WINDOW_MS = 150; // 3 ticks = 150ms
    
    private static class HitData {
        final UUID attacker;
        final long timestamp;
        final Vector velocity;
        
        HitData(UUID attacker, long timestamp, Vector velocity) {
            this.attacker = attacker;
            this.timestamp = timestamp;
            this.velocity = velocity;
        }
    }
    
    public DuelKnockbackSync(DuelInstance duel, Logger logger) {
        this.duel = duel;
        this.settings = duel.getSettings();
        this.logger = logger;
        this.recentHits = new ConcurrentHashMap<>();
    }
    
    /**
     * Handle knockback for damage event
     * 
     * @param event Damage event
     * @param attacker Attacking player
     * @param victim Victim player
     */
    public void handleKnockback(EntityDamageByEntityEvent event, Player attacker, Player victim) {
        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Check if victim recently hit attacker (within sync window)
        HitData victimRecentHit = recentHits.get(attackerUUID);
        
        if (victimRecentHit != null && 
            victimRecentHit.attacker.equals(victimUUID) && 
            (now - victimRecentHit.timestamp) <= SYNC_WINDOW_MS) {
            
            // Sync knockback - both players hit each other within window
            syncKnockback(attacker, victim, victimRecentHit.velocity);
            
            // Clear recent hit
            recentHits.remove(attackerUUID);
            recentHits.remove(victimUUID);
            
            logger.fine("Knockback synced between " + attacker.getName() + " and " + victim.getName());
            
        } else {
            // Normal knockback
            applyNormalKnockback(attacker, victim);
            
            // Record this hit
            Vector victimVelocity = victim.getVelocity().clone();
            recentHits.put(victimUUID, new HitData(attackerUUID, now, victimVelocity));
        }
        
        // Cleanup old hits
        cleanupOldHits(now);
    }
    
    /**
     * Apply synchronized knockback to both players
     * 
     * @param player1 First player
     * @param player2 Second player
     * @param originalVelocity Original velocity of first hit
     */
    private void syncKnockback(Player player1, Player player2, Vector originalVelocity) {
        double multiplier = settings.getKnockbackMultiplier();
        
        // Calculate balanced knockback
        Vector direction1 = player2.getLocation().toVector().subtract(player1.getLocation().toVector()).normalize();
        Vector direction2 = direction1.clone().multiply(-1);
        
        // Apply equal knockback in opposite directions
        double horizontalKnockback = 0.4 * multiplier;
        double verticalKnockback = 0.4 * multiplier;
        
        Vector velocity1 = direction1.multiply(horizontalKnockback).setY(verticalKnockback);
        Vector velocity2 = direction2.multiply(horizontalKnockback).setY(verticalKnockback);
        
        player1.setVelocity(velocity1);
        player2.setVelocity(velocity2);
    }
    
    /**
     * Apply normal (non-synced) knockback
     * 
     * @param attacker Attacking player
     * @param victim Victim player
     */
    private void applyNormalKnockback(Player attacker, Player victim) {
        double multiplier = settings.getKnockbackMultiplier();
        
        if (multiplier == 1.0) {
            // Use vanilla knockback
            return;
        }
        
        // Calculate direction from attacker to victim
        Vector direction = victim.getLocation().toVector()
                .subtract(attacker.getLocation().toVector())
                .normalize();
        
        // Apply custom knockback
        double horizontalKnockback = 0.4 * multiplier;
        double verticalKnockback = 0.4 * multiplier;
        
        Vector velocity = direction.multiply(horizontalKnockback).setY(verticalKnockback);
        victim.setVelocity(victim.getVelocity().add(velocity));
    }
    
    /**
     * Apply custom knockback multiplier
     * 
     * @param victim Victim player
     * @param baseKnockback Base knockback value
     */
    public void applyCustomKnockback(Player victim, double baseKnockback) {
        double multiplier = settings.getKnockbackMultiplier();
        Vector currentVelocity = victim.getVelocity();
        
        // Multiply existing velocity by multiplier
        currentVelocity.multiply(multiplier);
        victim.setVelocity(currentVelocity);
    }
    
    /**
     * Cleanup old hit data
     * 
     * @param currentTime Current timestamp
     */
    private void cleanupOldHits(long currentTime) {
        recentHits.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue().timestamp) > SYNC_WINDOW_MS * 2
        );
    }
    
    /**
     * Clear all tracking data (for round reset)
     */
    public void clearTracking() {
        recentHits.clear();
    }
    
    /**
     * Get knockback multiplier for duel
     * 
     * @return Knockback multiplier
     */
    public double getKnockbackMultiplier() {
        return settings.getKnockbackMultiplier();
    }
}
