package tech.xanthia.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Manages party chat functionality.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyChatManager {
    
    private final PartyManager partyManager;
    
    public PartyChatManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    /**
     * Sends a party chat message.
     * 
     * @param sender Sender UUID
     * @param message Message
     * @return True if sent
     */
    public boolean sendMessage(UUID sender, String message) {
        Party party = partyManager.getPlayerParty(sender);
        if (party == null) {
            return false;
        }
        
        PartyMember member = party.getMember(sender);
        if (member == null) {
            return false;
        }
        
        // Format message
        String formatted = "§9[Party] " + member.getRole().getChatPrefix() + " §f" + 
            member.getName() + "§7: §f" + message;
        
        // Broadcast to party
        for (UUID memberUUID : party.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage(formatted);
            }
        }
        
        return true;
    }
    
    /**
     * Toggles party chat for a player.
     * 
     * @param playerUUID Player UUID
     * @return New chat state
     */
    public boolean toggleChat(UUID playerUUID) {
        Party party = partyManager.getPlayerParty(playerUUID);
        if (party == null) {
            return false;
        }
        
        boolean newState = !party.isChatEnabled();
        party.setChatEnabled(newState);
        
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(newState ? 
                "§aParty chat enabled!" : 
                "§cParty chat disabled!");
        }
        
        return newState;
    }
}
