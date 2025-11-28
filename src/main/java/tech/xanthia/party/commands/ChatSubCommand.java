package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;

/**
 * /party chat - Toggles party chat mode.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChatSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public ChatSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "chat";
    }
    
    @Override
    public String getDescription() {
        return "Toggle party chat";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        partyManager.getChatManager().toggleChat(player.getUniqueId());
    }
}
