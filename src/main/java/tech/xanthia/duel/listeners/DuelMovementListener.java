package tech.xanthia.duel.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelManager;
import tech.xanthia.duel.DuelParticipant;
import tech.xanthia.duel.DuelState;
import tech.xanthia.duel.modes.DuelSpleefController;
import tech.xanthia.duel.modes.DuelSumoController;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * DuelMovementListener.java
 * Handles all movement-related events for duels.
 * 
 * Events:
 * - PlayerMoveEvent (freeze, bounds, mode-specific)
 * - PlayerTeleportEvent (prevent teleport during duel)
 * - PlayerQuitEvent (handle disconnect)
 * 
 * Responsibilities:
 * - Freeze players during countdown
 * - Check arena boundary exits
 * - Sumo fall detection
 * - Spleef fall detection
 * - Prevent teleportation exploits
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelMovementListener implements Listener {
    
    private final DuelManager duelManager;
    private final Logger logger;
    
    public DuelMovementListener(DuelManager duelManager, Logger logger) {
        this.duelManager = duelManager;
        this.logger = logger;
    }
    
    /**
     * Handle player movement
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Optimization: Only check if location changed (not just head movement)
        if (!hasMovedLocation(event)) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel == null) {
            return; // Not in duel
        }
        
        DuelParticipant participant = duel.getParticipant(playerUUID);
        if (participant == null) {
            return;
        }
        
        // Check if frozen (countdown)
        if (participant.isFrozen()) {
            event.setCancelled(true);
            return;
        }
        
        // Check arena bounds
        if (!duel.isInBounds(event.getTo())) {
            // Player left arena
            handleBoundaryExit(duel, participant, player);
            return;
        }
        
        // Special mode checks
        String duelMode = duel.getDuelMode();
        
        if ("sumo".equals(duelMode)) {
            handleSumoMovement(event, duel, participant);
        } else if ("spleef".equals(duelMode)) {
            handleSpleefMovement(event, duel, participant);
        }
    }
    
    /**
     * Handle player teleportation
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel == null) {
            return; // Not in duel
        }
        
        // Allow system teleports (duel start, end)
        if (duelManager.isTeleporting(playerUUID)) {
            return;
        }
        
        // Block player-initiated teleports during duel
        if (duel.getState().isActiveCombat()) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot teleport during a duel!");
        }
    }
    
    /**
     * Handle player disconnect
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        DuelInstance duel = duelManager.getPlayerDuel(playerUUID);
        if (duel == null) {
            return; // Not in duel
        }
        
        DuelParticipant participant = duel.getParticipant(playerUUID);
        if (participant != null) {
            participant.recordDeath();
            participant.setEliminated(true);
        }
        
        // Check if team eliminated
        // TODO: Trigger win condition check via round controller
        
        logger.info("Player " + player.getName() + " disconnected from duel " + duel.getDuelId());
    }
    
    /**
     * Handle boundary exit
     */
    private void handleBoundaryExit(DuelInstance duel, DuelParticipant participant, Player player) {
        if (participant.isEliminated()) {
            return;
        }
        
        participant.recordDeath();
        participant.setEliminated(true);
        
        duel.broadcast("§c" + player.getName() + " §7left the arena bounds!");
        
        // TODO: Check win conditions via round controller
        
        logger.info("Player " + player.getName() + " left arena bounds in duel " + duel.getDuelId());
    }
    
    /**
     * Handle sumo mode movement
     */
    private void handleSumoMovement(PlayerMoveEvent event, DuelInstance duel, DuelParticipant participant) {
        if (participant.isEliminated()) {
            return;
        }
        
        // Create sumo controller
        DuelSumoController sumoController = new DuelSumoController(duel, logger);
        
        boolean eliminated = sumoController.handleMovement(event);
        
        if (eliminated) {
            // TODO: Check win conditions via round controller
            logger.info("Player eliminated in sumo duel " + duel.getDuelId());
        }
    }
    
    /**
     * Handle spleef mode movement
     */
    private void handleSpleefMovement(PlayerMoveEvent event, DuelInstance duel, DuelParticipant participant) {
        if (participant.isEliminated()) {
            return;
        }
        
        // Create spleef controller
        DuelSpleefController spleefController = new DuelSpleefController(duel, logger);
        
        boolean eliminated = spleefController.handleMovement(event);
        
        if (eliminated) {
            // TODO: Check win conditions via round controller
            logger.info("Player eliminated in spleef duel " + duel.getDuelId());
        }
    }
    
    /**
     * Check if player actually moved location (not just head movement)
     */
    private boolean hasMovedLocation(PlayerMoveEvent event) {
        if (event.getFrom() == null || event.getTo() == null) {
            return false;
        }
        
        return event.getFrom().getX() != event.getTo().getX() ||
               event.getFrom().getY() != event.getTo().getY() ||
               event.getFrom().getZ() != event.getTo().getZ();
    }
}
