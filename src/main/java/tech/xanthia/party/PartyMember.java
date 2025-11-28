package tech.xanthia.party;

import java.util.UUID;

/**
 * Represents a party member with role and metadata.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyMember {
    
    private final UUID uuid;
    private final String name;
    private PartyRole role;
    private final long joinedAt;
    private long lastActivity;
    private int teamId;
    
    /**
     * Creates a new party member.
     * 
     * @param uuid Player UUID
     * @param name Player name
     * @param role Initial role
     */
    public PartyMember(UUID uuid, String name, PartyRole role) {
        this.uuid = uuid;
        this.name = name;
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.teamId = 1; // Default team
    }
    
    /**
     * Gets the player UUID.
     * 
     * @return Player UUID
     */
    public UUID getUuid() {
        return uuid;
    }
    
    /**
     * Gets the player name.
     * 
     * @return Player name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the member's role.
     * 
     * @return Role
     */
    public PartyRole getRole() {
        return role;
    }
    
    /**
     * Sets the member's role.
     * 
     * @param role New role
     */
    public void setRole(PartyRole role) {
        this.role = role;
    }
    
    /**
     * Gets the join timestamp.
     * 
     * @return Join time in milliseconds
     */
    public long getJoinedAt() {
        return joinedAt;
    }
    
    /**
     * Gets the last activity timestamp.
     * 
     * @return Last activity in milliseconds
     */
    public long getLastActivity() {
        return lastActivity;
    }
    
    /**
     * Updates the last activity timestamp.
     */
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * Gets the team ID this member is assigned to.
     * 
     * @return Team ID
     */
    public int getTeamId() {
        return teamId;
    }
    
    /**
     * Sets the team ID for this member.
     * 
     * @param teamId Team ID
     */
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    
    /**
     * Checks if this member is online.
     * 
     * @return True if last activity was within 5 minutes
     */
    public boolean isOnline() {
        return System.currentTimeMillis() - lastActivity < 300000; // 5 minutes
    }
    
    /**
     * Gets the display name with role prefix.
     * 
     * @return Formatted display name
     */
    public String getDisplayName() {
        return role.getChatPrefix() + " §f" + name;
    }
    
    /**
     * Gets the team number assigned to this member.
     * 
     * @return Team number
     */
    public int getTeamNumber() {
        return teamId;
    }
    
    /**
     * Gets the Bukkit player object if online.
     * 
     * @return Player object or null
     */
    public org.bukkit.entity.Player getPlayer() {
        return org.bukkit.Bukkit.getPlayer(uuid);
    }
    
    /**
     * Gets the member UUID (alias for getUuid).
     * 
     * @return Member UUID
     */
    public UUID getMemberUUID() {
        return uuid;
    }
}
