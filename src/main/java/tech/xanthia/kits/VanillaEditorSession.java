package tech.xanthia.kits;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an active vanilla kit editor session.
 * 
 * When a player edits a kit using the vanilla editor:
 * 1. Teleport to virtual room (void/saved location)
 * 2. Apply kit loadout to inventory
 * 3. Allow free editing
 * 4. On save → capture inventory and update kit
 * 5. On cancel → restore previous state
 * 
 * Prevents:
 * - Item loss
 * - Dropping items
 * - Leaving without saving
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class VanillaEditorSession {
    
    private final UUID playerUUID;
    private final Kit kit;
    
    // Saved state
    private final Location originalLocation;
    private final ItemStack[] originalContents;
    private final ItemStack[] originalArmor;
    private final GameMode originalGameMode;
    private final int originalFoodLevel;
    private final double originalHealth;
    
    // Editor state
    private final Location editorLocation;
    private final long startTime;
    private boolean active;
    
    /**
     * Creates a new vanilla editor session.
     * 
     * @param player Player editing
     * @param kit Kit being edited
     * @param editorLocation Location to teleport to
     */
    public VanillaEditorSession(Player player, Kit kit, Location editorLocation) {
        this.playerUUID = player.getUniqueId();
        this.kit = kit;
        this.editorLocation = editorLocation;
        this.startTime = System.currentTimeMillis();
        this.active = true;
        
        // Save current state
        this.originalLocation = player.getLocation().clone();
        this.originalContents = player.getInventory().getContents().clone();
        this.originalArmor = player.getInventory().getArmorContents().clone();
        this.originalGameMode = player.getGameMode();
        this.originalFoodLevel = player.getFoodLevel();
        this.originalHealth = player.getHealth();
    }
    
    /**
     * Gets the player UUID.
     * 
     * @return Player UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    /**
     * Gets the kit being edited.
     * 
     * @return Kit
     */
    public Kit getKit() {
        return kit;
    }
    
    /**
     * Gets the editor location.
     * 
     * @return Editor location
     */
    public Location getEditorLocation() {
        return editorLocation;
    }
    
    /**
     * Gets the session start time.
     * 
     * @return Start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the session duration.
     * 
     * @return Duration in milliseconds
     */
    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Checks if session is active.
     * 
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Starts the editor session.
     * 
     * Teleports player and applies kit loadout.
     * 
     * @param player Player to start session for
     */
    public void start(Player player) {
        // Teleport to editor location
        player.teleport(editorLocation);
        
        // Set creative mode for easier editing
        player.setGameMode(GameMode.CREATIVE);
        
        // Clear inventory and apply kit
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // Apply kit contents
        ItemStack[] contents = kit.getContents();
        for (int i = 0; i < contents.length && i < 36; i++) {
            if (contents[i] != null) {
                player.getInventory().setItem(i, contents[i].clone());
            }
        }
        
        player.getInventory().setArmorContents(kit.getArmorContents());
        
        // Set full health and food
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        player.updateInventory();
        
        player.sendMessage("§aVanilla Kit Editor started!");
        player.sendMessage("§7Edit your loadout freely.");
        player.sendMessage("§7Type §e/xduels kit save§7 to save or §c/xduels kit cancel§7 to cancel.");
    }
    
    /**
     * Saves the current inventory to the kit.
     * 
     * @param player Player whose inventory to save
     */
    public void save(Player player) {
        if (!active) {
            return;
        }
        
        // Capture current inventory
        ItemStack[] newContents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            newContents[i] = player.getInventory().getItem(i);
        }
        
        ItemStack[] newArmor = player.getInventory().getArmorContents();
        
        // Update kit
        kit.setContents(newContents);
        kit.setArmorContents(newArmor);
        
        // Restore player state
        restore(player);
        
        active = false;
        
        player.sendMessage("§aKit loadout saved successfully!");
    }
    
    /**
     * Cancels the editor session without saving.
     * 
     * @param player Player to restore
     */
    public void cancel(Player player) {
        if (!active) {
            return;
        }
        
        // Restore player state
        restore(player);
        
        active = false;
        
        player.sendMessage("§cKit editor cancelled. No changes saved.");
    }
    
    /**
     * Restores the player's original state.
     * 
     * @param player Player to restore
     */
    private void restore(Player player) {
        // Restore location
        player.teleport(originalLocation);
        
        // Restore inventory
        player.getInventory().setContents(originalContents);
        player.getInventory().setArmorContents(originalArmor);
        
        // Restore game mode
        player.setGameMode(originalGameMode);
        
        // Restore health and food
        player.setHealth(Math.min(originalHealth, player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
        player.setFoodLevel(originalFoodLevel);
        
        player.updateInventory();
    }
    
    /**
     * Forces session cleanup on player disconnect.
     * 
     * Auto-saves if modifications were made.
     * 
     * @param player Player disconnecting
     * @param autoSave Whether to auto-save
     */
    public void cleanup(Player player, boolean autoSave) {
        if (!active) {
            return;
        }
        
        if (autoSave) {
            save(player);
        } else {
            active = false;
        }
    }
}
