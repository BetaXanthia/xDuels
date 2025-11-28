package tech.xanthia.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /party demote <player> - Demotes a moderator to member.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class DemoteSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public DemoteSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "demote";
    }
    
    @Override
    public String getDescription() {
        return "Demote a moderator to member";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party demote <player>");
            return;
        }
        
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }
        
        boolean demoted = partyManager.demotePlayer(party, player.getUniqueId(), 
            target.getUniqueId());
        
        if (demoted) {
            player.sendMessage("§aDemoted §e" + target.getName() + " §ato member!");
            party.broadcast("§e" + target.getName() + " §cwas demoted to member!");
        } else {
            player.sendMessage("§cFailed to demote player! Check your permissions.");
        }
    }
    
    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            Party party = partyManager.getPlayerParty(player.getUniqueId());
            
            if (party == null) {
                return List.of();
            }
            
            List<String> moderators = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (UUID modUUID : party.getModerators()) {
                PartyMember member = party.getMember(modUUID);
                if (member != null && member.getName().toLowerCase().startsWith(input)) {
                    moderators.add(member.getName());
                }
            }
            
            return moderators;
        }
        
        return List.of();
    }
}
