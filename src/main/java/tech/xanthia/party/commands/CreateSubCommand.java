package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;

/**
 * /party create - Creates a new party.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public CreateSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "create";
    }
    
    @Override
    public String getDescription() {
        return "Create a new party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }
        
        Party party = partyManager.createParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cFailed to create party!");
            return;
        }
        
        player.sendMessage("§aParty created! You are now the leader.");
        player.sendMessage("§7Use §e/party invite <player> §7to invite members.");
    }
}
