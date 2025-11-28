package tech.xanthia.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages party invitations.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyInviteManager {
    
    private final XDuels plugin;
    private final PartyManager partyManager;
    
    // Player UUID -> List of invites
    private final Map<UUID, List<PartyInvite>> pendingInvites;
    
    public PartyInviteManager(XDuels plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.pendingInvites = new ConcurrentHashMap<>();
    }
    
    /**
     * Sends an invitation.
     * 
     * @param party Party
     * @param inviter Inviter UUID
     * @param invited Invited UUID
     * @return True if sent
     */
    public boolean sendInvite(Party party, UUID inviter, UUID invited) {
        // Validation
        if (party == null || !party.getCurrentState().canInvite()) {
            return false;
        }
        
        // Check if already invited
        if (party.getInvite(invited) != null) {
            return false;
        }
        
        // Check if already in party
        if (partyManager.isInParty(invited)) {
            return false;
        }
        
        // Create invite
        PartyInvite invite = new PartyInvite(party.getPartyId(), inviter, invited, 
            partyManager.getInviteExpireSeconds());
        
        party.addInvite(invite);
        pendingInvites.computeIfAbsent(invited, k -> new ArrayList<>()).add(invite);
        
        // Notify
        Player invitedPlayer = Bukkit.getPlayer(invited);
        Player inviterPlayer = Bukkit.getPlayer(inviter);
        
        if (invitedPlayer != null) {
            invitedPlayer.sendMessage("§a" + (inviterPlayer != null ? inviterPlayer.getName() : "Someone") + 
                " §7has invited you to their party!");
            invitedPlayer.sendMessage("§7Type §e/party join " + 
                (inviterPlayer != null ? inviterPlayer.getName() : "") + " §7to accept!");
        }
        
        return true;
    }
    
    /**
     * Accepts an invitation.
     * 
     * @param playerUUID Player UUID
     * @param partyId Party UUID
     * @return True if accepted
     */
    public boolean acceptInvite(UUID playerUUID, UUID partyId) {
        Party party = partyManager.getParty(partyId);
        if (party == null) {
            return false;
        }
        
        PartyInvite invite = party.getInvite(playerUUID);
        if (invite == null || invite.isExpired()) {
            return false;
        }
        
        // Remove invite
        party.removeInvite(invite);
        removePlayerInvites(playerUUID);
        
        // Add to party
        return partyManager.addPlayerToParty(party, playerUUID);
    }
    
    /**
     * Removes all invites for a player.
     * 
     * @param playerUUID Player UUID
     */
    public void removePlayerInvites(UUID playerUUID) {
        pendingInvites.remove(playerUUID);
    }
    
    /**
     * Gets all pending invites for a player.
     * 
     * @param playerUUID Player UUID
     * @return List of invites
     */
    public List<PartyInvite> getPlayerInvites(UUID playerUUID) {
        List<PartyInvite> invites = pendingInvites.get(playerUUID);
        if (invites == null) {
            return Collections.emptyList();
        }
        
        // Remove expired
        invites.removeIf(PartyInvite::isExpired);
        
        return new ArrayList<>(invites);
    }
}
