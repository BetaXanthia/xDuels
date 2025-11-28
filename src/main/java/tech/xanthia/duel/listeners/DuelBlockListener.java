package tech.xanthia.duel.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelManager;
import tech.xanthia.duel.DuelParticipant;
import tech.xanthia.duel.modes.DuelSpleefController;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * DuelBlockListener.java
 * Handles all block-related events for duels.
 * 
 * Events:
 * - BlockBreakEvent
 * - BlockPlaceEvent
 * 
 * Responsibilities:
 * - Enforce build/break permissions
 * - Track broken blocks for regeneration
 * - Spleef floor breaking
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelBlockListener implements Listener {
    
    private final DuelManager duelManager;
    private final Logger logger;
    
    public DuelBlockListener(DuelManager duelManager, Logger logger) {
        this.duelManager = duelManager;
        this.logger = logger;
    }
    
    /**
     * Handle block break
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel == null) {
            return; // Not in duel
        }
        
        // Check if breaking allowed
        if (!duel.getSettings().isBreakAllowed()) {
            // Special case: Spleef mode
            if ("spleef".equals(duel.getDuelMode())) {
                handleSpleefBreak(event, duel);
            } else {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break blocks in this duel!");
            }
        }
    }
    
    /**
     * Handle block place
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel == null) {
            return; // Not in duel
        }
        
        // Check if building allowed
        if (!duel.getSettings().isBuildAllowed()) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks in this duel!");
        }
    }
    
    /**
     * Handle spleef block breaking
     */
    private void handleSpleefBreak(BlockBreakEvent event, DuelInstance duel) {
        DuelSpleefController spleefController = new DuelSpleefController(duel, logger);
        
        boolean allowed = spleefController.handleBlockBreak(event);
        
        if (!allowed) {
            event.setCancelled(true);
        }
    }
}
