package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyMember;
import tech.xanthia.party.PartyRole;

/**
 * /party disband - Disbands the party.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class DisbandSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public DisbandSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "disband";
    }
    
    @Override
    public String getDescription() {
        return "Disband your party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        PartyMember member = party.getMember(player.getUniqueId());
        
        if (member == null || member.getRole() != PartyRole.LEADER) {
            player.sendMessage("§cOnly the party leader can disband the party!");
            return;
        }
        
        partyManager.disbandParty(party, "Disbanded by leader");
        player.sendMessage("§cParty disbanded!");
    }
}
