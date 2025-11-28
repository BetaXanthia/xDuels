package tech.xanthia.duel.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import tech.xanthia.duel.*;
import tech.xanthia.duel.combat.DuelDamageController;
import tech.xanthia.duel.combat.DuelHealthIndicatorController;
import tech.xanthia.duel.combat.DuelKnockbackSync;
import tech.xanthia.duel.modes.DuelBoxingController;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * DuelCombatListener.java
 * Handles all combat-related events for duels.
 * 
 * Events:
 * - EntityDamageByEntityEvent (PvP, projectiles)
 * - EntityDamageEvent (fall, fire, void)
 * - PlayerDeathEvent
 * 
 * Integrates with:
 * - DuelDamageController
 * - DuelKnockbackSync
 * - DuelHealthIndicatorController
 * - Mode controllers (Boxing, Sumo, Spleef)
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelCombatListener implements Listener {
    
    private final DuelManager duelManager;
    private final Logger logger;
    
    public DuelCombatListener(DuelManager duelManager, Logger logger) {
        this.duelManager = duelManager;
        this.logger = logger;
    }
    
    /**
     * Handle entity damage by entity (PvP, projectiles)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        
        UUID victimUUID = victim.getUniqueId();
        DuelInstance duel = duelManager.getPlayerDuel(victimUUID);
        
        if (duel == null) {
            return; // Not in duel
        }
        
        // Get attacker
        Player attacker = getAttacker(event);
        if (attacker == null) {
            return; // Not player damage
        }
        
        UUID attackerUUID = attacker.getUniqueId();
        
        // Validate attacker in same duel
        if (!duel.isParticipant(attackerUUID)) {
            event.setCancelled(true);
            return;
        }
        
        // Check duel state
        if (!duel.getState().canDamage()) {
            event.setCancelled(true);
            return;
        }
        
        // Handle special modes
        String duelMode = duel.getDuelMode();
        
        if ("boxing".equals(duelMode)) {
            // Boxing mode - no damage, hit counter
            handleBoxingHit(event, duel, attacker, victim);
            return;
        }
        
        if ("sumo".equals(duelMode) || "spleef".equals(duelMode)) {
            // Sumo/Spleef - no damage
            event.setCancelled(true);
            return;
        }
        
        // Standard damage handling
        DuelDamageController damageController = getDamageController(duel);
        if (damageController != null) {
            boolean cancelled = damageController.handleDamage(event);
            if (cancelled) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Knockback sync
        DuelKnockbackSync knockbackSync = getKnockbackSync(duel);
        if (knockbackSync != null) {
            knockbackSync.handleKnockback(event, attacker, victim);
        }
        
        // Health indicator flash
        DuelHealthIndicatorController healthController = getHealthController(duel);
        if (healthController != null) {
            healthController.flashOnHit(victim);
        }
    }
    
    /**
     * Handle environmental damage (fall, fire, void, etc.)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        UUID playerUUID = player.getUniqueId();
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        
        if (duel == null) {
            return; // Not in duel
        }
        
        // Check state
        if (!duel.getState().canDamage()) {
            event.setCancelled(true);
            return;
        }
        
        // Handle environmental damage
        DuelDamageController damageController = getDamageController(duel);
        if (damageController != null) {
            boolean cancelled = damageController.handleEnvironmentalDamage(event);
            if (cancelled) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Handle player death
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        
        if (duel == null) {
            return; // Not in duel
        }
        
        // Cancel death (keep player alive)
        event.setCancelled(true);
        
        // Drop no items
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Reset player
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        
        // Record death
        DuelParticipant participant = duel.getParticipant(playerUUID);
        if (participant != null) {
            participant.recordDeath();
        }
        
        // TODO: Check win conditions via round controller
        
        logger.fine("Player " + player.getName() + " died in duel " + duel.getDuelId());
    }
    
    /**
     * Handle boxing mode hit
     */
    private void handleBoxingHit(EntityDamageByEntityEvent event, DuelInstance duel, 
                                  Player attacker, Player victim) {
        // Get boxing controller
        // TODO: Retrieve boxing controller from duel
        // For now, create inline
        DuelBoxingController boxingController = new DuelBoxingController(duel, logger);
        
        boolean roundWon = boxingController.handleBoxingHit(event);
        
        if (roundWon) {
            // TODO: Trigger round end via round controller
            logger.info("Boxing round won in duel " + duel.getDuelId());
        }
    }
    
    /**
     * Get attacker player from damage event
     */
    private Player getAttacker(EntityDamageByEntityEvent event) {
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
     * Get damage controller for duel
     * TODO: Implement controller retrieval from duel
     */
    private DuelDamageController getDamageController(DuelInstance duel) {
        // TODO: Retrieve from duel instance
        return new DuelDamageController(duel, logger);
    }
    
    /**
     * Get knockback sync for duel
     * TODO: Implement controller retrieval from duel
     */
    private DuelKnockbackSync getKnockbackSync(DuelInstance duel) {
        // TODO: Retrieve from duel instance
        return new DuelKnockbackSync(duel, logger);
    }
    
    /**
     * Get health indicator controller for duel
     * TODO: Implement controller retrieval from duel
     */
    private DuelHealthIndicatorController getHealthController(DuelInstance duel) {
        // TODO: Retrieve from duel instance
        return null;
    }
}
