package tech.xanthia.party;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a complete party with all state and members.
 * 
 * Supports:
 * - 1v1, 2v2, 3v3, 4v4, 5v5+ team configurations
 * - Party FFA mode
 * - Split teams mode
 * - Ranked/unranked queues
 * - Full state machine
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class Party {
    
    // Core identification
    private final UUID partyId;
    private final UUID leader;
    
    // Members with roles
    private final Map<UUID, PartyMember> members;
    
    // State
    private PartyState currentState;
    
    // Visibility
    private boolean publicParty;
    
    // Team structure
    private final Map<UUID, Integer> teamAssignments;
    
    // FFA mode
    private boolean ffaEnabled;
    
    // Split mode
    private boolean splitModeEnabled;
    
    // Invitations
    private final List<PartyInvite> activeInvites;
    
    // Time metadata
    private final long createdAt;
    private long lastActivity;
    
    // Duel integration
    private UUID duelId;
    private boolean inDuel;
    
    // Party chat
    private boolean chatEnabled;
    
    /**
     * Creates a new party.
     * 
     * @param leader Leader UUID
     */
    public Party(UUID leader) {
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.members = new ConcurrentHashMap<>();
        this.currentState = PartyState.IDLE;
        this.publicParty = false;
        this.teamAssignments = new ConcurrentHashMap<>();
        this.ffaEnabled = false;
        this.splitModeEnabled = false;
        this.activeInvites = Collections.synchronizedList(new ArrayList<>());
        this.createdAt = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.duelId = null;
        this.inDuel = false;
        this.chatEnabled = false;
    }
    
    // Core getters
    public UUID getPartyId() { return partyId; }
    public UUID getLeader() { return leader; }
    public Map<UUID, PartyMember> getMembers() { return members; }
    public PartyState getCurrentState() { return currentState; }
    public boolean isPublicParty() { return publicParty; }
    public Map<UUID, Integer> getTeamAssignments() { return teamAssignments; }
    public boolean isFfaEnabled() { return ffaEnabled; }
    public boolean isSplitModeEnabled() { return splitModeEnabled; }
    public List<PartyInvite> getActiveInvites() { return activeInvites; }
    public long getCreatedAt() { return createdAt; }
    public long getLastActivity() { return lastActivity; }
    public UUID getDuelId() { return duelId; }
    public boolean isInDuel() { return inDuel; }
    public boolean isChatEnabled() { return chatEnabled; }
    
    // Core setters
    public void setCurrentState(PartyState currentState) { 
        this.currentState = currentState;
        updateActivity();
    }
    public void setPublicParty(boolean publicParty) { this.publicParty = publicParty; }
    public void setFfaEnabled(boolean ffaEnabled) { this.ffaEnabled = ffaEnabled; }
    public void setSplitModeEnabled(boolean splitModeEnabled) { 
        this.splitModeEnabled = splitModeEnabled; 
    }
    public void setDuelId(UUID duelId) { this.duelId = duelId; }
    public void setInDuel(boolean inDuel) { this.inDuel = inDuel; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    
    /**
     * Updates the last activity timestamp.
     */
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * Gets the party size.
     * 
     * @return Number of members
     */
    public int getSize() {
        return members.size();
    }
    
    /**
     * Checks if a player is in this party.
     * 
     * @param uuid Player UUID
     * @return True if in party
     */
    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }
    
    /**
     * Gets a member by UUID.
     * 
     * @param uuid Player UUID
     * @return Party member or null
     */
    public PartyMember getMember(UUID uuid) {
        return members.get(uuid);
    }
    
    /**
     * Gets the role of a player.
     * 
     * @param uuid Player UUID
     * @return Role or null if not in party
     */
    public PartyRole getRole(UUID uuid) {
        PartyMember member = members.get(uuid);
        return member != null ? member.getRole() : null;
    }
    
    /**
     * Checks if a player is the leader.
     * 
     * @param uuid Player UUID
     * @return True if leader
     */
    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }
    
    /**
     * Checks if a player is a moderator.
     * 
     * @param uuid Player UUID
     * @return True if moderator
     */
    public boolean isModerator(UUID uuid) {
        PartyRole role = getRole(uuid);
        return role == PartyRole.MODERATOR;
    }
    
    /**
     * Gets all members with a specific role.
     * 
     * @param role Role to filter by
     * @return List of members with role
     */
    public List<PartyMember> getMembersByRole(PartyRole role) {
        List<PartyMember> result = new ArrayList<>();
        for (PartyMember member : members.values()) {
            if (member.getRole() == role) {
                result.add(member);
            }
        }
        return result;
    }
    
    /**
     * Gets all online members.
     * 
     * @return List of online members
     */
    public List<PartyMember> getOnlineMembers() {
        List<PartyMember> result = new ArrayList<>();
        for (PartyMember member : members.values()) {
            if (member.isOnline()) {
                result.add(member);
            }
        }
        return result;
    }
    
    /**
     * Gets all member UUIDs.
     * 
     * @return Set of UUIDs
     */
    public Set<UUID> getMemberUUIDs() {
        return new HashSet<>(members.keySet());
    }
    
    /**
     * Gets members assigned to a specific team.
     * 
     * @param teamId Team ID
     * @return List of members on team
     */
    public List<PartyMember> getTeamMembers(int teamId) {
        List<PartyMember> result = new ArrayList<>();
        for (PartyMember member : members.values()) {
            if (member.getTeamId() == teamId) {
                result.add(member);
            }
        }
        return result;
    }
    
    /**
     * Gets the number of unique teams.
     * 
     * @return Team count
     */
    public int getTeamCount() {
        Set<Integer> teams = new HashSet<>();
        for (PartyMember member : members.values()) {
            teams.add(member.getTeamId());
        }
        return teams.size();
    }
    
    /**
     * Checks if party is full.
     * 
     * @param maxSize Maximum party size
     * @return True if at max size
     */
    public boolean isFull(int maxSize) {
        return getSize() >= maxSize;
    }
    
    /**
     * Checks if party is empty.
     * 
     * @return True if no members
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }
    
    /**
     * Adds an invitation.
     * 
     * @param invite Invitation to add
     */
    public void addInvite(PartyInvite invite) {
        activeInvites.add(invite);
        updateActivity();
    }
    
    /**
     * Removes an invitation.
     * 
     * @param invite Invitation to remove
     */
    public void removeInvite(PartyInvite invite) {
        activeInvites.remove(invite);
    }
    
    /**
     * Removes all expired invitations.
     * 
     * @return Number of invites removed
     */
    public int removeExpiredInvites() {
        int removed = 0;
        synchronized (activeInvites) {
            Iterator<PartyInvite> iterator = activeInvites.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isExpired()) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
    
    /**
     * Finds an invitation for a specific player.
     * 
     * @param uuid Player UUID
     * @return Invite or null
     */
    public PartyInvite getInvite(UUID uuid) {
        synchronized (activeInvites) {
            for (PartyInvite invite : activeInvites) {
                if (invite.getInvited().equals(uuid) && !invite.isExpired()) {
                    return invite;
                }
            }
        }
        return null;
    }
    
    /**
     * Validates the current state transition.
     * 
     * @param newState New state to transition to
     * @return True if transition is valid
     */
    public boolean canTransitionTo(PartyState newState) {
        switch (currentState) {
            case IDLE:
                return newState == PartyState.QUEUING || newState == PartyState.EDITING;
            case QUEUING:
                return newState == PartyState.FIGHTING || newState == PartyState.IDLE;
            case FIGHTING:
                return newState == PartyState.IDLE;
            case EDITING:
                return newState == PartyState.IDLE || newState == PartyState.QUEUING;
            default:
                return false;
        }
    }
    
    /**
     * Broadcasts a message to all online party members.
     * 
     * @param message Message to broadcast
     */
    public void broadcast(String message) {
        for (PartyMember member : members.values()) {
            if (member.isOnline() && member.getPlayer() != null) {
                member.getPlayer().sendMessage(message);
            }
        }
    }
    
    /**
     * Gets the total member count.
     * 
     * @return Number of members
     */
    public int getMemberCount() {
        return members.size();
    }
    
    /**
     * Gets all moderator UUIDs.
     * 
     * @return Set of moderator UUIDs
     */
    public Set<UUID> getModerators() {
        Set<UUID> mods = new HashSet<>();
        for (Map.Entry<UUID, PartyMember> entry : members.entrySet()) {
            if (entry.getValue().getRole() == PartyRole.MODERATOR) {
                mods.add(entry.getKey());
            }
        }
        return mods;
    }
    
    /**
     * Checks if party is in FFA mode.
     * 
     * @return True if FFA enabled
     */
    public boolean isFfaMode() {
        return ffaEnabled;
    }
    
    /**
     * Checks if party is in split mode.
     * 
     * @return True if split mode enabled
     */
    public boolean isSplitMode() {
        return splitModeEnabled;
    }
    
    /**
     * Sets the FFA mode state.
     * 
     * @param enabled True to enable FFA
     */
    public void setFfaMode(boolean enabled) {
        this.ffaEnabled = enabled;
        updateActivity();
    }
    
    /**
     * Sets the split mode state.
     * 
     * @param enabled True to enable split mode
     */
    public void setSplitMode(boolean enabled) {
        this.splitModeEnabled = enabled;
        updateActivity();
    }
    
    /**
     * Assigns a player to a team.
     * 
     * @param playerUUID Player UUID
     * @param teamId Team ID
     */
    public void assignTeam(UUID playerUUID, int teamId) {
        teamAssignments.put(playerUUID, teamId);
        PartyMember member = members.get(playerUUID);
        if (member != null) {
            member.setTeamId(teamId);
        }
        updateActivity();
    }
    
    /**
     * Sets the party's current state.
     * 
     * @param newState The new state to set
     */
    public void setState(PartyState newState) {
        this.currentState = newState;
        updateActivity();
    }
}
