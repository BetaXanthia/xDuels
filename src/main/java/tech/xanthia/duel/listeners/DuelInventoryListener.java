package tech.xanthia.duel.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelManager;
import tech.xanthia.duel.DuelSpectatorManager;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * DuelInventoryListener.java
 * Handles all inventory-related events for duels.
 * 
 * Events:
 * - PlayerDropItemEvent
 * - PlayerPickupItemEvent
 * - InventoryClickEvent
 * 
 * Responsibilities:
 * - Prevent item drops during duel (unless allowed)
 * - Prevent item pickups
 * - Prevent spectator inventory interactions
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelInventoryListener implements Listener {
    
    private final DuelManager duelManager;
    private final DuelSpectatorManager spectatorManager;
    private final Logger logger;
    
    public DuelInventoryListener(DuelManager duelManager, DuelSpectatorManager spectatorManager, Logger logger) {
        this.duelManager = duelManager;
        this.spectatorManager = spectatorManager;
        this.logger = logger;
    }
    
    /**
     * Handle item drop
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Check spectator
        if (spectatorManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        
        // Check duel participant
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel != null) {
            // TODO: Check if drops allowed in settings
            event.setCancelled(true);
            player.sendMessage("§cYou cannot drop items during a duel!");
        }
    }
    
    /**
     * Handle item pickup
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Check spectator
        if (spectatorManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        
        // Check duel participant
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel != null) {
            // TODO: Check if pickups allowed in settings
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle inventory click (prevent armor removal, etc.)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        UUID playerUUID = player.getUniqueId();
        
        // Check spectator
        if (spectatorManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        
        // Check duel participant
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel != null) {
            // TODO: Implement armor slot protection
            // For now, allow inventory management
        }
    }
}
