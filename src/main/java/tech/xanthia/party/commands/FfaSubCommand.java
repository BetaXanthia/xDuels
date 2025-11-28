package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyMember;
import tech.xanthia.party.PartyRole;

/**
 * /party ffa - Toggles FFA mode.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class FfaSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public FfaSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "ffa";
    }
    
    @Override
    public String getDescription() {
        return "Toggle FFA mode";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        PartyMember member = party.getMember(player.getUniqueId());
        
        if (member == null || !member.getRole().canEditTeams()) {
            player.sendMessage("§cYou don't have permission to change FFA mode!");
            return;
        }
        
        if (!party.getCurrentState().canEditTeams()) {
            player.sendMessage("§cCannot change FFA mode in current state!");
            return;
        }
        
        boolean newState = !party.isFfaMode();
        party.setFfaMode(newState);
        
        if (newState) {
            party.setSplitMode(false); // Disable split if enabling FFA
            party.broadcast("§6FFA mode enabled!");
        } else {
            party.broadcast("§cFFA mode disabled!");
        }
    }
}
