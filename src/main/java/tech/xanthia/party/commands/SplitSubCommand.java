package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyMember;
import tech.xanthia.party.PartyRole;

/**
 * /party split - Toggles split team mode.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class SplitSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public SplitSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "split";
    }
    
    @Override
    public String getDescription() {
        return "Toggle split team mode";
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
            player.sendMessage("§cYou don't have permission to change team mode!");
            return;
        }
        
        if (!party.getCurrentState().canEditTeams()) {
            player.sendMessage("§cCannot change team mode in current state!");
            return;
        }
        
        boolean newState = !party.isSplitMode();
        party.setSplitMode(newState);
        
        if (newState) {
            party.setFfaMode(false); // Disable FFA if enabling split
            party.broadcast("§aSplit team mode enabled!");
        } else {
            party.broadcast("§cSplit team mode disabled!");
        }
    }
}
