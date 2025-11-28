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
 * /party promote <player> - Promotes a member to moderator.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PromoteSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public PromoteSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "promote";
    }
    
    @Override
    public String getDescription() {
        return "Promote a member to moderator";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party promote <player>");
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
        
        boolean promoted = partyManager.promotePlayer(party, player.getUniqueId(), 
            target.getUniqueId());
        
        if (promoted) {
            player.sendMessage("§aPromoted §e" + target.getName() + " §ato moderator!");
            party.broadcast("§e" + target.getName() + " §awas promoted to moderator!");
        } else {
            player.sendMessage("§cFailed to promote player! Check your permissions.");
        }
    }
    
    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            Party party = partyManager.getPlayerParty(player.getUniqueId());
            
            if (party == null) {
                return List.of();
            }
            
            List<String> members = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (UUID memberUUID : party.getMemberUUIDs()) {
                PartyMember member = party.getMember(memberUUID);
                if (member != null && member.getName().toLowerCase().startsWith(input)) {
                    members.add(member.getName());
                }
            }
            
            return members;
        }
        
        return List.of();
    }
}
