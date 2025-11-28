package tech.xanthia.party;

/**
 * Enum representing party roles and their permissions.
 * 
 * Hierarchy:
 * LEADER > MODERATOR > MEMBER
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PartyRole {
    
    /**
     * Leader role - full permissions.
     * 
     * Can:
     * - Promote/demote members
     * - Transfer ownership
     * - Disband party
     * - Change public/private status
     * - Split teams / toggle FFA
     * - Queue party for duels
     * - Kick anyone
     * - Manage all settings
     */
    LEADER(3),
    
    /**
     * Moderator role - management permissions.
     * 
     * Can:
     * - Kick MEMBERS only
     * - Invite players
     * - Use party chat
     * - Cannot kick other moderators or leader
     */
    MODERATOR(2),
    
    /**
     * Member role - no management permissions.
     * 
     * Can:
     * - Use party chat
     * - Leave party
     * - View party info
     */
    MEMBER(1);
    
    private final int level;
    
    PartyRole(int level) {
        this.level = level;
    }
    
    /**
     * Gets the permission level of this role.
     * 
     * @return Permission level (higher = more permissions)
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Checks if this role can kick another role.
     * 
     * @param targetRole Target role to kick
     * @return True if can kick
     */
    public boolean canKick(PartyRole targetRole) {
        if (this == LEADER) {
            return targetRole != LEADER;
        }
        if (this == MODERATOR) {
            return targetRole == MEMBER;
        }
        return false;
    }
    
    /**
     * Checks if this role can promote/demote.
     * 
     * @return True if can promote/demote
     */
    public boolean canPromote() {
        return this == LEADER;
    }
    
    /**
     * Checks if this role can transfer ownership.
     * 
     * @return True if can transfer
     */
    public boolean canTransfer() {
        return this == LEADER;
    }
    
    /**
     * Checks if this role can disband the party.
     * 
     * @return True if can disband
     */
    public boolean canDisband() {
        return this == LEADER;
    }
    
    /**
     * Checks if this role can change party settings.
     * 
     * @return True if can change settings
     */
    public boolean canChangeSettings() {
        return this == LEADER;
    }
    
    /**
     * Checks if this role can queue the party.
     * 
     * @return True if can queue
     */
    public boolean canQueue() {
        return this == LEADER;
    }
    
    /**
     * Checks if this role can invite players.
     * 
     * @return True if can invite
     */
    public boolean canInvite() {
        return this == LEADER || this == MODERATOR;
    }
    
    /**
     * Gets the display name for this role.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        switch (this) {
            case LEADER:
                return "§6Leader";
            case MODERATOR:
                return "§eModerator";
            case MEMBER:
                return "§7Member";
            default:
                return "§7Unknown";
        }
    }
    
    /**
     * Gets the chat prefix for this role.
     * 
     * @return Chat prefix
     */
    public String getChatPrefix() {
        switch (this) {
            case LEADER:
                return "§6★";
            case MODERATOR:
                return "§e●";
            case MEMBER:
                return "§7○";
            default:
                return "";
        }
    }
    
    /**
     * Checks if this role can edit team assignments.
     * 
     * @return True if can edit teams
     */
    public boolean canEditTeams() {
        return this == LEADER;
    }
}
