package tech.xanthia.party;

/**
 * Enum representing party state.
 * 
 * State machine:
 * IDLE → QUEUING → FIGHTING → IDLE
 * IDLE ↔ EDITING
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PartyState {
    
    /**
     * Party is idle and ready for actions.
     */
    IDLE,
    
    /**
     * Party is in queue waiting for match.
     */
    QUEUING,
    
    /**
     * Party is currently in a duel.
     */
    FIGHTING,
    
    /**
     * Party is in GUI/editor mode.
     */
    EDITING;
    
    /**
     * Checks if this state allows inviting players.
     * 
     * @return True if invites allowed
     */
    public boolean canInvite() {
        return this == IDLE || this == EDITING;
    }
    
    /**
     * Checks if this state allows joining.
     * 
     * @return True if joins allowed
     */
    public boolean canJoin() {
        return this == IDLE || this == EDITING;
    }
    
    /**
     * Checks if this state allows leaving.
     * 
     * @return True if leaves allowed
     */
    public boolean canLeave() {
        return this == IDLE || this == EDITING;
    }
    
    /**
     * Checks if this state allows kicking.
     * 
     * @return True if kicks allowed
     */
    public boolean canKick() {
        return this == IDLE || this == EDITING;
    }
    
    /**
     * Checks if this state allows team editing.
     * 
     * @return True if team editing allowed
     */
    public boolean canEditTeams() {
        return this == IDLE || this == EDITING;
    }
    
    /**
     * Checks if this state allows queuing.
     * 
     * @return True if queuing allowed
     */
    public boolean canQueue() {
        return this == IDLE;
    }
    
    /**
     * Checks if this state allows disbanding.
     * 
     * @return True if disbanding allowed
     */
    public boolean canDisband() {
        return this != FIGHTING; // Can disband even when queuing
    }
    
    /**
     * Gets the display name for this state.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        switch (this) {
            case IDLE:
                return "§aIdle";
            case QUEUING:
                return "§eQueuing";
            case FIGHTING:
                return "§cFighting";
            case EDITING:
                return "§bEditing";
            default:
                return "§7Unknown";
        }
    }
}
