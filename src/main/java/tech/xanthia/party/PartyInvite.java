package tech.xanthia.party;

import java.util.UUID;

/**
 * Represents a party invitation.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyInvite {
    
    private final UUID partyId;
    private final UUID inviter;
    private final UUID invited;
    private final long timestamp;
    private final long expiresAt;
    
    /**
     * Creates a new party invitation.
     * 
     * @param partyId Party UUID
     * @param inviter Inviter UUID
     * @param invited Invited player UUID
     * @param durationSeconds Invitation duration in seconds
     */
    public PartyInvite(UUID partyId, UUID inviter, UUID invited, int durationSeconds) {
        this.partyId = partyId;
        this.inviter = inviter;
        this.invited = invited;
        this.timestamp = System.currentTimeMillis();
        this.expiresAt = timestamp + (durationSeconds * 1000L);
    }
    
    /**
     * Gets the party UUID.
     * 
     * @return Party UUID
     */
    public UUID getPartyId() {
        return partyId;
    }
    
    /**
     * Gets the inviter UUID.
     * 
     * @return Inviter UUID
     */
    public UUID getInviter() {
        return inviter;
    }
    
    /**
     * Gets the invited player UUID.
     * 
     * @return Invited UUID
     */
    public UUID getInvited() {
        return invited;
    }
    
    /**
     * Gets the invitation timestamp.
     * 
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the expiration timestamp.
     * 
     * @return Expiration in milliseconds
     */
    public long getExpiresAt() {
        return expiresAt;
    }
    
    /**
     * Checks if this invitation has expired.
     * 
     * @return True if expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
    
    /**
     * Gets the remaining time in seconds.
     * 
     * @return Remaining seconds
     */
    public int getRemainingSeconds() {
        long remaining = expiresAt - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }
}
