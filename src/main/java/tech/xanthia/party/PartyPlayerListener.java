package tech.xanthia.party;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles party-related player events.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyPlayerListener implements Listener {
    
    private final PartyManager partyManager;
    
    public PartyPlayerListener(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Party party = partyManager.getPlayerParty(event.getPlayer().getUniqueId());
        
        if (party == null) {
            return;
        }
        
        PartyMember member = party.getMember(event.getPlayer().getUniqueId());
        
        if (member != null) {
            // Update last activity
            member.updateActivity();
            
            // PartyManager cleanup task will handle offline players
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Party party = partyManager.getPlayerParty(event.getPlayer().getUniqueId());
        
        if (party == null) {
            return;
        }
        
        PartyMember member = party.getMember(event.getPlayer().getUniqueId());
        
        if (member != null) {
            // Update activity on rejoin
            member.updateActivity();
            
            // Notify party
            party.broadcast("§e" + event.getPlayer().getName() + " §7came back online!");
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Party party = partyManager.getPlayerParty(event.getPlayer().getUniqueId());
        
        if (party == null || !party.isChatEnabled()) {
            return;
        }
        
        // If party chat is enabled, redirect to party chat
        event.setCancelled(true);
        
        partyManager.getChatManager().sendMessage(
            event.getPlayer().getUniqueId(), event.getMessage());
    }
}
