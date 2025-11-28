package tech.xanthia.duel;

/**
 * DuelState.java
 * Represents all possible states a duel can be in.
 * 
 * State machine enforces valid transitions and prevents invalid operations.
 * 
 * State Transition Flow:
 * CREATED → TELEPORTING → COUNTDOWN → COMBAT → ROUND_END → FINISHED → CLEANUP
 * 
 * Special transitions:
 * - Any state → CANCELLED (emergency abort)
 * - ROUND_END → COUNTDOWN (next round)
 * 
 * @author xDuels
 * @since 1.0.0
 */
public enum DuelState {
    
    /**
     * CREATED - Duel instance created, awaiting teleportation
     * - Arena reserved
     * - Teams assigned
     * - No players teleported yet
     */
    CREATED("Created", true, false, false, false),
    
    /**
     * TELEPORTING - Players being teleported to arena
     * - Chunks loading
     * - Players frozen
     * - Inventories clearing
     */
    TELEPORTING("Teleporting", false, false, false, false),
    
    /**
     * COUNTDOWN - Pre-combat countdown active
     * - Players frozen at spawns
     * - Countdown timer running
     * - No damage allowed
     */
    COUNTDOWN("Countdown", false, false, false, false),
    
    /**
     * COMBAT - Active combat phase
     * - Players can move and fight
     * - Damage enabled (unless mode disables it)
     * - Round win conditions checked
     */
    COMBAT("Combat", false, true, true, false),
    
    /**
     * ROUND_END - Round completed, awaiting next round or duel end
     * - Winner determined
     * - Stats recorded
     * - Arena regenerating
     */
    ROUND_END("Round End", false, false, false, false),
    
    /**
     * FINISHED - Duel completed successfully
     * - All rounds complete
     * - Final stats updated
     * - ELO calculated
     * - Players teleporting to lobby
     */
    FINISHED("Finished", false, false, false, true),
    
    /**
     * CLEANUP - Final cleanup phase
     * - Arena freed
     * - Memory cleared
     * - Spectators removed
     */
    CLEANUP("Cleanup", false, false, false, true),
    
    /**
     * CANCELLED - Duel cancelled/aborted
     * - Emergency state
     * - No stats recorded
     * - Immediate cleanup
     */
    CANCELLED("Cancelled", false, false, false, true);
    
    private final String displayName;
    private final boolean canInvite;
    private final boolean canDamage;
    private final boolean canMove;
    private final boolean isTerminal;
    
    DuelState(String displayName, boolean canInvite, boolean canDamage, boolean canMove, boolean isTerminal) {
        this.displayName = displayName;
        this.canInvite = canInvite;
        this.canDamage = canDamage;
        this.canMove = canMove;
        this.isTerminal = isTerminal;
    }
    
    /**
     * Get display name for UI/messages
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if players can be invited to spectate
     */
    public boolean canInviteSpectators() {
        return canInvite;
    }
    
    /**
     * Check if damage is allowed in this state
     */
    public boolean canDamage() {
        return canDamage;
    }
    
    /**
     * Check if players can move freely
     */
    public boolean canMove() {
        return canMove;
    }
    
    /**
     * Check if this is a terminal state (duel ending/ended)
     */
    public boolean isTerminal() {
        return isTerminal;
    }
    
    /**
     * Check if transition to target state is valid
     * 
     * @param target Target state to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(DuelState target) {
        // Emergency cancellation always allowed
        if (target == CANCELLED) {
            return true;
        }
        
        // Terminal states cannot transition (except to CLEANUP)
        if (this.isTerminal && target != CLEANUP) {
            return false;
        }
        
        // Define valid transitions
        return switch (this) {
            case CREATED -> target == TELEPORTING;
            case TELEPORTING -> target == COUNTDOWN;
            case COUNTDOWN -> target == COMBAT;
            case COMBAT -> target == ROUND_END;
            case ROUND_END -> target == COUNTDOWN || target == FINISHED; // Next round or end
            case FINISHED -> target == CLEANUP;
            case CLEANUP -> false; // No transitions from cleanup
            case CANCELLED -> target == CLEANUP;
        };
    }
    
    /**
     * Get next expected state in normal flow
     * 
     * @return Next state, or null if terminal
     */
    public DuelState getNextState() {
        return switch (this) {
            case CREATED -> TELEPORTING;
            case TELEPORTING -> COUNTDOWN;
            case COUNTDOWN -> COMBAT;
            case COMBAT -> ROUND_END;
            case ROUND_END -> FINISHED; // Default, may go to COUNTDOWN for next round
            case FINISHED -> CLEANUP;
            default -> null;
        };
    }
    
    /**
     * Check if state is active combat
     */
    public boolean isActiveCombat() {
        return this == COMBAT;
    }
    
    /**
     * Check if state allows spectators to join
     */
    public boolean allowsSpectators() {
        return this == COUNTDOWN || this == COMBAT || this == ROUND_END;
    }
    
    /**
     * Check if state is pre-combat
     */
    public boolean isPreCombat() {
        return this == CREATED || this == TELEPORTING || this == COUNTDOWN;
    }
    
    /**
     * Check if state is post-combat
     */
    public boolean isPostCombat() {
        return this == FINISHED || this == CLEANUP || this == CANCELLED;
    }
}
