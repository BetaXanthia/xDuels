package tech.xanthia.duel.combat;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;
import tech.xanthia.duel.DuelSettings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * DuelDamageController.java
 * Handles all damage-related logic for duels.
 * 
 * Responsibilities:
 * - Process melee damage
 * - Process projectile damage
 * - Apply damage modifiers
 * - Handle critical hits
 * - Enforce damage rules (no-damage modes)
 * - Track damage statistics
 * - Handle death detection
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelDamageController {
    
    private final DuelInstance duel;
    private final DuelSettings settings;
    private final Logger logger;
    
    // Last hit tracking for combo detection
    private final Map<UUID, Long> lastHitTime;
    private final Map<UUID, UUID> lastHitBy;
    
    // Hit delay enforcement (prevent spam)
    private final Map<UUID, Long> hitCooldowns;
    
    public DuelDamageController(DuelInstance duel, Logger logger) {
        this.duel = duel;
        this.settings = duel.getSettings();
        this.logger = logger;
        this.lastHitTime = new ConcurrentHashMap<>();
        this.lastHitBy = new ConcurrentHashMap<>();
        this.hitCooldowns = new ConcurrentHashMap<>();
    }
    
    /**
     * Handle entity damage event
     * 
     * @param event Damage event
     * @return true if event should be cancelled
     */
    public boolean handleDamage(EntityDamageByEntityEvent event) {
        // Validate victim is player
        if (!(event.getEntity() instanceof Player victim)) {
            return true; // Cancel non-player damage
        }
        
        DuelParticipant victimParticipant = duel.getParticipant(victim.getUniqueId());
        if (victimParticipant == null) {
            return true; // Not a participant
        }
        
        // Check if damage enabled in settings
        if (!settings.isDamageEnabled()) {
            event.setCancelled(true);
            return true;
        }
        
        // Get damager
        Player attacker = getDamager(event);
        if (attacker == null) {
            return false; // Allow environmental damage
        }
        
        DuelParticipant attackerParticipant = duel.getParticipant(attacker.getUniqueId());
        if (attackerParticipant == null) {
            return true; // Attacker not in duel
        }
        
        // Prevent team damage (friendly fire)
        if (attackerParticipant.getTeam() == victimParticipant.getTeam()) {
            event.setCancelled(true);
            return true;
        }
        
        // Check hit delay cooldown
        if (isOnHitCooldown(attacker.getUniqueId())) {
            event.setCancelled(true);
            return true;
        }
        
        // Apply damage
        processDamage(event, attackerParticipant, victimParticipant);
        
        return false;
    }
    
    /**
     * Handle non-entity damage (fall, fire, etc.)
     * 
     * @param event Damage event
     * @return true if event should be cancelled
     */
    public boolean handleEnvironmentalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return true;
        }
        
        DuelParticipant participant = duel.getParticipant(player.getUniqueId());
        if (participant == null) {
            return true;
        }
        
        // Handle fall damage
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (!settings.isFallDamageEnabled()) {
                event.setCancelled(true);
                return true;
            }
        }
        
        // Handle void damage
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            // Instant elimination
            participant.recordDeath();
            event.setDamage(1000);
            return false;
        }
        
        return false;
    }
    
    /**
     * Process damage and update statistics
     * 
     * @param event Damage event
     * @param attacker Attacking participant
     * @param victim Victim participant
     */
    private void processDamage(EntityDamageByEntityEvent event, 
                                DuelParticipant attacker, 
                                DuelParticipant victim) {
        double damage = event.getFinalDamage();
        
        // Record damage statistics
        attacker.recordHit(damage);
        victim.recordDamageTaken(damage);
        
        // Track last hit
        lastHitTime.put(victim.getPlayerUUID(), System.currentTimeMillis());
        lastHitBy.put(victim.getPlayerUUID(), attacker.getPlayerUUID());
        
        // Set hit cooldown
        long cooldownTicks = settings.getHitDelay();
        hitCooldowns.put(attacker.getPlayerUUID(), System.currentTimeMillis() + (cooldownTicks * 50));
        
        // Check if critical hit
        if (event.getDamager() instanceof Player damager && damager.getFallDistance() > 0) {
            // Critical hit (falling)
            // TODO: Apply critical hit effects
        }
        
        // Check death
        Player victimPlayer = victim.getPlayer();
        if (victimPlayer != null) {
            double healthAfter = victimPlayer.getHealth() - damage;
            if (healthAfter <= 0) {
                handleDeath(victim, attacker);
                event.setCancelled(true); // Prevent actual death
                victimPlayer.setHealth(victimPlayer.getMaxHealth()); // Reset health
            }
        }
    }
    
    /**
     * Handle player death
     * 
     * @param victim Victim participant
     * @param killer Killer participant
     */
    private void handleDeath(DuelParticipant victim, DuelParticipant killer) {
        victim.recordDeath();
        killer.recordKill();
        
        logger.info("Player " + victim.getPlayerName() + " killed by " + 
                    killer.getPlayerName() + " in duel " + duel.getDuelId());
        
        // TODO: Handle respawn if enabled
        // TODO: Drop items if configured
        // TODO: Play death effects
    }
    
    /**
     * Get attacking player from damage event
     * 
     * @param event Damage event
     * @return Attacking player or null
     */
    private Player getDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        } else if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }
    
    /**
     * Check if player is on hit cooldown
     * 
     * @param playerUUID Player UUID
     * @return true if on cooldown
     */
    private boolean isOnHitCooldown(UUID playerUUID) {
        Long cooldownEnd = hitCooldowns.get(playerUUID);
        if (cooldownEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            hitCooldowns.remove(playerUUID);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get time since player last hit opponent
     * 
     * @param playerUUID Player UUID
     * @return Milliseconds since last hit, or -1 if never hit
     */
    public long getTimeSinceLastHit(UUID playerUUID) {
        Long lastHit = lastHitTime.get(playerUUID);
        if (lastHit == null) {
            return -1;
        }
        return System.currentTimeMillis() - lastHit;
    }
    
    /**
     * Get player's last attacker
     * 
     * @param playerUUID Player UUID
     * @return Last attacker UUID or null
     */
    public UUID getLastAttacker(UUID playerUUID) {
        return lastHitBy.get(playerUUID);
    }
    
    /**
     * Clear all tracking data (for round reset)
     */
    public void clearTracking() {
        lastHitTime.clear();
        lastHitBy.clear();
        hitCooldowns.clear();
    }
}
