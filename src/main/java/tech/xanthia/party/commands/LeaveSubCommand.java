package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;

/**
 * /party leave - Leaves the current party.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class LeaveSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public LeaveSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "leave";
    }
    
    @Override
    public String getDescription() {
        return "Leave your current party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        boolean removed = partyManager.removePlayerFromParty(party, player.getUniqueId(), false);
        
        if (removed) {
            player.sendMessage("§aYou left the party!");
        } else {
            player.sendMessage("§cFailed to leave party!");
        }
    }
}
