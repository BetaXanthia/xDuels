package tech.xanthia.duel.modes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import tech.xanthia.arena.Arena;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelParticipant;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * DuelSpleefController.java
 * Controls Spleef mode duels.
 * 
 * Spleef Mode Rules:
 * - No PvP damage
 * - Players break floor blocks beneath opponents
 * - Broken blocks tracked for regeneration
 * - Falling through floor = elimination
 * - Optional TNT spleef variant
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelSpleefController {
    
    private final DuelInstance duel;
    private final Logger logger;
    private final Arena arena;
    
    // Track broken blocks for regeneration
    private final Set<Block> brokenBlocks;
    
    // Spleef floor Y-level
    private final double floorY;
    private final double eliminationY;
    
    // TNT spleef mode
    private final boolean tntMode;
    
    public DuelSpleefController(DuelInstance duel, Logger logger) {
        this.duel = duel;
        this.logger = logger;
        this.arena = duel.getArena();
        this.brokenBlocks = new HashSet<>();
        
        // Calculate floor Y from arena center
        this.floorY = arena.getCenter().getY();
        this.eliminationY = floorY - 5; // 5 blocks below floor
        
        // Check if TNT mode enabled
        this.tntMode = duel.getKit().getConfigBoolean("spleef-tnt-mode", false);
        
        logger.info("Spleef controller initialized for duel " + duel.getDuelId() + 
                    " | Floor Y: " + floorY + " | TNT Mode: " + tntMode);
    }
    
    /**
     * Handle block break in spleef
     * 
     * @param event Block break event
     * @return true if block break allowed
     */
    public boolean handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        DuelParticipant participant = duel.getParticipant(player.getUniqueId());
        if (participant == null || !participant.isAlive()) {
            event.setCancelled(true);
            return false;
        }
        
        // Check if block is part of spleef floor
        if (!isSpleefFloorBlock(block)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only break spleef floor blocks!");
            return false;
        }
        
        // Track block for regeneration
        brokenBlocks.add(block);
        
        // Play break sound
        player.playSound(block.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 1.0f);
        
        // Drop no items
        event.setDropItems(false);
        
        // Immediate block removal
        block.setType(Material.AIR);
        
        return true;
    }
    
    /**
     * Handle player movement in spleef
     * 
     * @param event Move event
     * @return true if player eliminated
     */
    public boolean handleMovement(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DuelParticipant participant = duel.getParticipant(player.getUniqueId());
        
        if (participant == null || !participant.isAlive()) {
            return false;
        }
        
        Location location = player.getLocation();
        
        // Check if fallen below elimination Y
        if (location.getY() < eliminationY) {
            eliminatePlayer(participant, "fell through the floor");
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if block is part of spleef floor
     * 
     * @param block Block to check
     * @return true if spleef floor block
     */
    private boolean isSpleefFloorBlock(Block block) {
        // Check Y-level (floor is typically at floorY to floorY-2)
        double blockY = block.getY();
        if (blockY < floorY - 2 || blockY > floorY) {
            return false;
        }
        
        // Check if within arena bounds
        if (!duel.isInBounds(block.getLocation())) {
            return false;
        }
        
        // Check material (snow, ice, etc.)
        Material type = block.getType();
        return type == Material.SNOW_BLOCK || 
               type == Material.ICE || 
               type == Material.PACKED_ICE ||
               type == Material.BLUE_ICE ||
               type == Material.WHITE_WOOL ||
               type == Material.WHITE_CONCRETE;
    }
    
    /**
     * Eliminate player from spleef
     * 
     * @param participant Participant to eliminate
     * @param reason Elimination reason
     */
    private void eliminatePlayer(DuelParticipant participant, String reason) {
        if (participant.isEliminated()) {
            return;
        }
        
        participant.recordDeath();
        participant.setEliminated(true);
        
        Player player = participant.getPlayer();
        
        // Announce elimination
        String message = ChatColor.RED + participant.getPlayerName() + 
                        ChatColor.GRAY + " was eliminated: " + reason;
        duel.broadcast(message);
        
        // Play elimination sound
        if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.8f);
            
            // Teleport to spectator location
            Location spectatorLoc = arena.getCenter().clone().add(0, 15, 0);
            player.teleport(spectatorLoc);
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        
        logger.info("Player " + participant.getPlayerName() + " eliminated from spleef duel " + 
                    duel.getDuelId() + ": " + reason);
    }
    
    /**
     * Regenerate broken spleef floor
     */
    public void regenerateFloor() {
        if (!duel.getSettings().isSpleefRegeneration()) {
            return;
        }
        
        logger.info("Regenerating " + brokenBlocks.size() + " spleef blocks for duel " + duel.getDuelId());
        
        for (Block block : brokenBlocks) {
            // Restore original block type
            // TODO: Get original block type from arena snapshot
            block.setType(Material.SNOW_BLOCK);
        }
        
        brokenBlocks.clear();
    }
    
    /**
     * Get broken block count
     * 
     * @return Number of broken blocks
     */
    public int getBrokenBlockCount() {
        return brokenBlocks.size();
    }
    
    /**
     * Get spleef mode summary
     * 
     * @return Formatted summary string
     */
    public String getSpleefSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(ChatColor.GOLD).append("Spleef Status:\n");
        summary.append(ChatColor.GRAY).append("Blocks Broken: ")
               .append(ChatColor.YELLOW).append(brokenBlocks.size()).append("\n");
        
        for (DuelParticipant participant : duel.getParticipants()) {
            String status = participant.isAlive() ? 
                          ChatColor.GREEN + "Alive" : 
                          ChatColor.RED + "Eliminated";
            
            summary.append(ChatColor.WHITE).append(participant.getPlayerName())
                   .append(": ").append(status).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if TNT mode enabled
     * 
     * @return true if TNT mode
     */
    public boolean isTntMode() {
        return tntMode;
    }
}
