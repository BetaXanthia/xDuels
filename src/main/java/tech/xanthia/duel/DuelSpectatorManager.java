package tech.xanthia.duel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * DuelSpectatorManager.java
 * Manages spectators for active duels.
 * 
 * Features:
 * - Invisible to participants
 * - No collision
 * - View-only mode
 * - Teleport around arena
 * - Leave spectator mode
 * - Spectate specific duel
 * 
 * Restrictions:
 * - Cannot spectate own duel
 * - Cannot interfere with duel
 * - Cannot drop/pickup items
 * - Cannot damage entities
 * - Cannot open inventories
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelSpectatorManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final DuelManager duelManager;
    
    // Spectator tracking
    private final Map<UUID, UUID> spectatorToDuel; // spectator -> duelId
    private final Map<UUID, UUID> spectatorToTarget; // spectator -> target player
    
    // Saved states (for restoration)
    private final Map<UUID, SpectatorState> savedStates;
    
    private static class SpectatorState {
        GameMode gameMode;
        Location location;
        boolean flying;
        boolean allowFlight;
        
        SpectatorState(Player player) {
            this.gameMode = player.getGameMode();
            this.location = player.getLocation().clone();
            this.flying = player.isFlying();
            this.allowFlight = player.getAllowFlight();
        }
    }
    
    public DuelSpectatorManager(JavaPlugin plugin, DuelManager duelManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.duelManager = duelManager;
        this.spectatorToDuel = new ConcurrentHashMap<>();
        this.spectatorToTarget = new ConcurrentHashMap<>();
        this.savedStates = new ConcurrentHashMap<>();
        
        logger.info("DuelSpectatorManager initialized");
    }
    
    /**
     * Start spectating a duel
     * 
     * @param spectator Spectator player
     * @param duelId Duel ID to spectate
     * @return true if successfully started spectating
     */
    public boolean startSpectating(Player spectator, UUID duelId) {
        UUID spectatorUUID = spectator.getUniqueId();
        
        // Validate duel exists
        DuelInstance duel = duelManager.getDuel(duelId);
        if (duel == null) {
            spectator.sendMessage(ChatColor.RED + "That duel no longer exists.");
            return false;
        }
        
        // Check if player is in a duel
        if (duelManager.isInDuel(spectatorUUID)) {
            spectator.sendMessage(ChatColor.RED + "You cannot spectate while in a duel!");
            return false;
        }
        
        // Check if duel allows spectators
        if (!duel.getSettings().areSpectatorsAllowed()) {
            spectator.sendMessage(ChatColor.RED + "This duel does not allow spectators.");
            return false;
        }
        
        // Check if duel is in valid state for spectating
        if (!duel.getState().allowsSpectators()) {
            spectator.sendMessage(ChatColor.RED + "You cannot spectate this duel right now.");
            return false;
        }
        
        // Save current state
        savedStates.put(spectatorUUID, new SpectatorState(spectator));
        
        // Add to duel spectators
        if (!duel.addSpectator(spectatorUUID)) {
            spectator.sendMessage(ChatColor.RED + "Failed to join as spectator.");
            savedStates.remove(spectatorUUID);
            return false;
        }
        
        // Track spectator
        spectatorToDuel.put(spectatorUUID, duelId);
        
        // Apply spectator mode
        applySpectatorMode(spectator, duel);
        
        // Announce
        spectator.sendMessage(ChatColor.GREEN + "You are now spectating the duel.");
        duel.broadcast(ChatColor.GRAY + spectator.getName() + " is now spectating.");
        
        logger.info("Player " + spectator.getName() + " started spectating duel " + duelId);
        
        return true;
    }
    
    /**
     * Start spectating specific player in duel
     * 
     * @param spectator Spectator player
     * @param targetName Target player name
     * @return true if successful
     */
    public boolean startSpectating(Player spectator, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            spectator.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return false;
        }
        
        // Find target's duel
        DuelInstance duel = duelManager.getPlayerDuel(target.getUniqueId());
        if (duel == null) {
            spectator.sendMessage(ChatColor.RED + target.getName() + " is not in a duel.");
            return false;
        }
        
        // Start spectating
        if (startSpectating(spectator, duel.getDuelId())) {
            spectatorToTarget.put(spectator.getUniqueId(), target.getUniqueId());
            spectator.teleport(target.getLocation());
            return true;
        }
        
        return false;
    }
    
    /**
     * Stop spectating
     * 
     * @param spectator Spectator player
     */
    public void stopSpectating(Player spectator) {
        UUID spectatorUUID = spectator.getUniqueId();
        
        // Get duel
        UUID duelId = spectatorToDuel.remove(spectatorUUID);
        if (duelId == null) {
            return; // Not spectating
        }
        
        DuelInstance duel = duelManager.getDuel(duelId);
        if (duel != null) {
            duel.removeSpectator(spectatorUUID);
        }
        
        // Remove target tracking
        spectatorToTarget.remove(spectatorUUID);
        
        // Restore state
        SpectatorState savedState = savedStates.remove(spectatorUUID);
        if (savedState != null) {
            spectator.setGameMode(savedState.gameMode);
            spectator.teleport(savedState.location);
            spectator.setFlying(savedState.flying);
            spectator.setAllowFlight(savedState.allowFlight);
        } else {
            // Fallback restoration
            spectator.setGameMode(GameMode.SURVIVAL);
            spectator.setFlying(false);
            spectator.setAllowFlight(false);
        }
        
        // Remove spectator effects
        removeSpectatorMode(spectator);
        
        spectator.sendMessage(ChatColor.YELLOW + "You are no longer spectating.");
        
        logger.info("Player " + spectator.getName() + " stopped spectating duel " + duelId);
    }
    
    /**
     * Apply spectator mode to player
     * 
     * @param spectator Spectator player
     * @param duel Duel being spectated
     */
    private void applySpectatorMode(Player spectator, DuelInstance duel) {
        // Set spectator game mode
        spectator.setGameMode(GameMode.SPECTATOR);
        
        // Teleport to arena center (above)
        Location spectatorLoc = duel.getArena().getCenter().clone().add(0, 10, 0);
        spectator.teleport(spectatorLoc);
        
        // Enable flight
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        
        // Clear inventory
        spectator.getInventory().clear();
        
        // Add invisibility (redundant with spectator mode, but ensures invisibility)
        spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
    }
    
    /**
     * Remove spectator mode from player
     * 
     * @param spectator Spectator player
     */
    private void removeSpectatorMode(Player spectator) {
        // Remove effects
        spectator.removePotionEffect(PotionEffectType.INVISIBILITY);
        
        // Clear inventory
        spectator.getInventory().clear();
        
        // Reset health/food
        spectator.setHealth(spectator.getMaxHealth());
        spectator.setFoodLevel(20);
    }
    
    /**
     * Check if player is spectating
     * 
     * @param playerUUID Player UUID
     * @return true if spectating
     */
    public boolean isSpectating(UUID playerUUID) {
        return spectatorToDuel.containsKey(playerUUID);
    }
    
    /**
     * Get duel being spectated
     * 
     * @param spectatorUUID Spectator UUID
     * @return Duel ID or null
     */
    public UUID getSpectatedDuel(UUID spectatorUUID) {
        return spectatorToDuel.get(spectatorUUID);
    }
    
    /**
     * Get spectator's target player
     * 
     * @param spectatorUUID Spectator UUID
     * @return Target player UUID or null
     */
    public UUID getSpectatorTarget(UUID spectatorUUID) {
        return spectatorToTarget.get(spectatorUUID);
    }
    
    /**
     * Handle duel end (remove all spectators)
     * 
     * @param duelId Duel ID
     */
    public void handleDuelEnd(UUID duelId) {
        // Find all spectators of this duel
        List<UUID> spectators = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : spectatorToDuel.entrySet()) {
            if (entry.getValue().equals(duelId)) {
                spectators.add(entry.getKey());
            }
        }
        
        // Remove all spectators
        for (UUID spectatorUUID : spectators) {
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null && spectator.isOnline()) {
                stopSpectating(spectator);
            } else {
                // Player offline, just cleanup tracking
                spectatorToDuel.remove(spectatorUUID);
                spectatorToTarget.remove(spectatorUUID);
                savedStates.remove(spectatorUUID);
            }
        }
    }
    
    /**
     * Get all spectators of a duel
     * 
     * @param duelId Duel ID
     * @return List of spectator UUIDs
     */
    public List<UUID> getDuelSpectators(UUID duelId) {
        List<UUID> spectators = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : spectatorToDuel.entrySet()) {
            if (entry.getValue().equals(duelId)) {
                spectators.add(entry.getKey());
            }
        }
        return spectators;
    }
    
    /**
     * Get spectator count for duel
     * 
     * @param duelId Duel ID
     * @return Number of spectators
     */
    public int getSpectatorCount(UUID duelId) {
        return (int) spectatorToDuel.values().stream()
                .filter(id -> id.equals(duelId))
                .count();
    }
}
