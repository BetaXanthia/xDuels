package tech.xanthia.arena;

import java.util.UUID;

/**
 * Represents an active arena editing session for a player.
 * 
 * When a player enters arena editor mode, a session is created to track:
 * - Which arena they're editing
 * - Which location type they're currently setting
 * - Session state and timestamps
 * 
 * Sessions prevent multiple players from editing the same arena simultaneously
 * and provide a clean interface for handling editor interactions.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaEditorSession {
    
    private final UUID playerId;
    private Arena arena;
    private ArenaLocationType activeLocationType;
    private final long startTime;
    private boolean modified;
    
    /**
     * Creates a new editor session.
     * 
     * @param playerId UUID of the player editing
     * @param arena Arena being edited
     */
    public ArenaEditorSession(UUID playerId, Arena arena) {
        this.playerId = playerId;
        this.arena = arena;
        this.activeLocationType = null;
        this.startTime = System.currentTimeMillis();
        this.modified = false;
    }
    
    // ========================================
    // SESSION MANAGEMENT
    // ========================================
    
    /**
     * Marks this session as having made modifications.
     */
    public void markModified() {
        this.modified = true;
    }
    
    /**
     * Checks if any modifications have been made during this session.
     * 
     * @return true if arena was modified
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Gets the session duration in milliseconds.
     * 
     * @return Duration in milliseconds
     */
    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Gets the session duration in a human-readable format.
     * 
     * @return Formatted duration string
     */
    public String getFormattedDuration() {
        long duration = getDuration() / 1000; // Convert to seconds
        
        if (duration < 60) {
            return duration + "s";
        } else if (duration < 3600) {
            long minutes = duration / 60;
            long seconds = duration % 60;
            return minutes + "m " + seconds + "s";
        } else {
            long hours = duration / 3600;
            long minutes = (duration % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    // ========================================
    // LOCATION EDITING
    // ========================================
    
    /**
     * Sets the active location type being edited.
     * When a player clicks a block, this location will be set.
     * 
     * @param type Location type to set as active
     */
    public void setActiveLocationType(ArenaLocationType type) {
        this.activeLocationType = type;
    }
    
    /**
     * Gets the currently active location type being edited.
     * 
     * @return Active location type or null if none selected
     */
    public ArenaLocationType getActiveLocationType() {
        return activeLocationType;
    }
    
    /**
     * Checks if a location type is currently being edited.
     * 
     * @return true if a location type is active
     */
    public boolean hasActiveLocationType() {
        return activeLocationType != null;
    }
    
    /**
     * Clears the active location type selection.
     */
    public void clearActiveLocationType() {
        this.activeLocationType = null;
    }
    
    /**
     * Applies a location to the currently active location type.
     * 
     * @param location Location to apply
     * @return true if successfully applied
     */
    public boolean applyLocation(org.bukkit.Location location) {
        if (activeLocationType == null) {
            return false;
        }
        
        arena.setLocation(activeLocationType, location);
        markModified();
        clearActiveLocationType();
        return true;
    }
    
    // ========================================
    // COMMIT & CANCEL
    // ========================================
    
    /**
     * Commits all changes made during this session.
     * This should trigger a save operation.
     * 
     * @return true if there were changes to commit
     */
    public boolean commit() {
        return modified;
    }
    
    /**
     * Cancels the editing session.
     * Note: This doesn't revert changes - arena reference is already modified.
     * For proper cancel functionality, the arena should be reloaded from storage.
     */
    public void cancel() {
        // Cleanup any temporary state
        clearActiveLocationType();
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    /**
     * Updates the arena being edited.
     * Useful if the arena instance changes during editing.
     * 
     * @param arena New arena instance
     */
    public void setArena(Arena arena) {
        this.arena = arena;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    @Override
    public String toString() {
        return "ArenaEditorSession{" +
                "playerId=" + playerId +
                ", arena=" + (arena != null ? arena.getId() : "null") +
                ", activeLocationType=" + activeLocationType +
                ", modified=" + modified +
                ", duration=" + getFormattedDuration() +
                '}';
    }
}
