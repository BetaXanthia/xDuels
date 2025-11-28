package tech.xanthia.duel.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.xanthia.arena.Arena;
import tech.xanthia.arena.ArenaRegenerationManager;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DuelCleanup.java
 * Handles cleanup operations for duels.
 * 
 * Cleanup Operations:
 * - Clear dropped items
 * - Remove projectiles
 * - Reset player states
 * - Free arena
 * - Regenerate blocks
 * - Remove entities
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelCleanup {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final ArenaRegenerationManager regenManager;
    
    public DuelCleanup(JavaPlugin plugin, ArenaRegenerationManager regenManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.regenManager = regenManager;
    }
    
    /**
     * Perform full cleanup for duel
     * 
     * @param duel Duel to cleanup
     */
    public void cleanup(DuelInstance duel) {
        logger.info("Starting cleanup for duel " + duel.getDuelId());
        
        // Clear entities
        clearEntities(duel.getArena());
        
        // Reset player states
        resetPlayerStates(duel);
        
        // Regenerate arena
        regenerateArena(duel.getArena());
        
        logger.info("Cleanup complete for duel " + duel.getDuelId());
    }
    
    /**
     * Clear all entities in arena
     * 
     * @param arena Arena to clear
     */
    private void clearEntities(Arena arena) {
        Location center = arena.getCenter();
        double radius = arena.getRadius();
        
        List<Entity> entitiesToRemove = new ArrayList<>();
        
        // Find all entities in arena
        for (Entity entity : center.getWorld().getEntities()) {
            if (entity instanceof Player) {
                continue; // Don't remove players
            }
            
            if (entity.getLocation().distance(center) <= radius) {
                // Entity is in arena
                if (entity instanceof Item || 
                    entity instanceof org.bukkit.entity.Projectile ||
                    entity instanceof org.bukkit.entity.ExperienceOrb) {
                    entitiesToRemove.add(entity);
                }
            }
        }
        
        // Remove entities
        for (Entity entity : entitiesToRemove) {
            entity.remove();
        }
        
        logger.fine("Cleared " + entitiesToRemove.size() + " entities from arena " + arena.getName());
    }
    
    /**
     * Reset all player states
     * 
     * @param duel Duel instance
     */
    private void resetPlayerStates(DuelInstance duel) {
        for (DuelParticipant participant : duel.getParticipants()) {
            Player player = participant.getPlayer();
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Clear inventory
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            
            // Clear effects
            player.getActivePotionEffects().forEach(effect -> {
                player.removePotionEffect(effect.getType());
            });
            
            // Reset health/food
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20);
            
            // Reset fire
            player.setFireTicks(0);
            
            // Reset fall distance
            player.setFallDistance(0);
            
            // Unfreeze
            participant.setFrozen(false);
        }
    }
    
    /**
     * Regenerate arena blocks
     * 
     * @param arena Arena to regenerate
     */
    private void regenerateArena(Arena arena) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            regenManager.regenerateArena(arena);
            logger.fine("Regenerated arena " + arena.getName());
        });
    }
    
    /**
     * Quick cleanup (remove entities only)
     * 
     * @param arena Arena to cleanup
     */
    public void quickCleanup(Arena arena) {
        clearEntities(arena);
    }
    
    /**
     * Clean specific player
     * 
     * @param player Player to clean
     */
    public void cleanPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getActivePotionEffects().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
    }
}
