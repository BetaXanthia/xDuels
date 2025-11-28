package tech.xanthia.party;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's party profile with statistics.
 * 
 * Tracks:
 * - Party join/leave history
 * - Leadership history
 * - Win/loss records
 * - Last party
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyProfile {
    
    private final UUID playerUUID;
    
    // Current party
    private UUID currentPartyId;
    
    // Statistics
    private int partiesJoined;
    private int partiesLeft;
    private int partiesCreated;
    private int kickedCount;
    private int partyWins;
    private int partyLosses;
    
    // Session data
    private long lastPartyJoinTime;
    private long totalPartyTime;
    
    // Settings
    private boolean autoAcceptInvites;
    private boolean receiveInvites;
    
    /**
     * Creates a new party profile.
     * 
     * @param playerUUID Player UUID
     */
    public PartyProfile(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.currentPartyId = null;
        this.partiesJoined = 0;
        this.partiesLeft = 0;
        this.partiesCreated = 0;
        this.kickedCount = 0;
        this.partyWins = 0;
        this.partyLosses = 0;
        this.lastPartyJoinTime = 0;
        this.totalPartyTime = 0;
        this.autoAcceptInvites = false;
        this.receiveInvites = true;
    }
    
    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public UUID getCurrentPartyId() { return currentPartyId; }
    public int getPartiesJoined() { return partiesJoined; }
    public int getPartiesLeft() { return partiesLeft; }
    public int getPartiesCreated() { return partiesCreated; }
    public int getKickedCount() { return kickedCount; }
    public int getPartyWins() { return partyWins; }
    public int getPartyLosses() { return partyLosses; }
    public long getLastPartyJoinTime() { return lastPartyJoinTime; }
    public long getTotalPartyTime() { return totalPartyTime; }
    public boolean isAutoAcceptInvites() { return autoAcceptInvites; }
    public boolean isReceiveInvites() { return receiveInvites; }
    
    // Setters
    public void setCurrentPartyId(UUID currentPartyId) { this.currentPartyId = currentPartyId; }
    public void setAutoAcceptInvites(boolean autoAcceptInvites) { 
        this.autoAcceptInvites = autoAcceptInvites; 
    }
    public void setReceiveInvites(boolean receiveInvites) { 
        this.receiveInvites = receiveInvites; 
    }
    
    /**
     * Checks if player is currently in a party.
     * 
     * @return True if in party
     */
    public boolean isInParty() {
        return currentPartyId != null;
    }
    
    /**
     * Records joining a party.
     * 
     * @param partyId Party UUID
     */
    public void joinParty(UUID partyId) {
        this.currentPartyId = partyId;
        this.partiesJoined++;
        this.lastPartyJoinTime = System.currentTimeMillis();
    }
    
    /**
     * Records leaving a party.
     */
    public void leaveParty() {
        if (currentPartyId != null && lastPartyJoinTime > 0) {
            long sessionTime = System.currentTimeMillis() - lastPartyJoinTime;
            this.totalPartyTime += sessionTime;
        }
        this.currentPartyId = null;
        this.partiesLeft++;
    }
    
    /**
     * Records creating a party.
     */
    public void createParty() {
        this.partiesCreated++;
    }
    
    /**
     * Records being kicked from a party.
     */
    public void kicked() {
        leaveParty();
        this.kickedCount++;
    }
    
    /**
     * Records a party win.
     */
    public void recordWin() {
        this.partyWins++;
    }
    
    /**
     * Records a party loss.
     */
    public void recordLoss() {
        this.partyLosses++;
    }
    
    /**
     * Gets the party win/loss ratio.
     * 
     * @return W/L ratio
     */
    public double getWLRatio() {
        if (partyLosses == 0) {
            return partyWins;
        }
        return (double) partyWins / partyLosses;
    }
    
    /**
     * Serializes this profile to a map.
     * 
     * @return Map representation
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", playerUUID.toString());
        data.put("partiesJoined", partiesJoined);
        data.put("partiesLeft", partiesLeft);
        data.put("partiesCreated", partiesCreated);
        data.put("kickedCount", kickedCount);
        data.put("partyWins", partyWins);
        data.put("partyLosses", partyLosses);
        data.put("totalPartyTime", totalPartyTime);
        data.put("autoAcceptInvites", autoAcceptInvites);
        data.put("receiveInvites", receiveInvites);
        return data;
    }
    
    /**
     * Deserializes a profile from a map.
     * 
     * @param data Map data
     * @return Party profile
     */
    public static PartyProfile deserialize(Map<String, Object> data) {
        UUID uuid = UUID.fromString((String) data.get("uuid"));
        PartyProfile profile = new PartyProfile(uuid);
        
        profile.partiesJoined = (int) data.getOrDefault("partiesJoined", 0);
        profile.partiesLeft = (int) data.getOrDefault("partiesLeft", 0);
        profile.partiesCreated = (int) data.getOrDefault("partiesCreated", 0);
        profile.kickedCount = (int) data.getOrDefault("kickedCount", 0);
        profile.partyWins = (int) data.getOrDefault("partyWins", 0);
        profile.partyLosses = (int) data.getOrDefault("partyLosses", 0);
        profile.totalPartyTime = ((Number) data.getOrDefault("totalPartyTime", 0L)).longValue();
        profile.autoAcceptInvites = (boolean) data.getOrDefault("autoAcceptInvites", false);
        profile.receiveInvites = (boolean) data.getOrDefault("receiveInvites", true);
        
        return profile;
    }
}
