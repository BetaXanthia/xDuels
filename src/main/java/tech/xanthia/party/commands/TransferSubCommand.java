package tech.xanthia.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyMember;
import tech.xanthia.party.PartyRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /party transfer <player> - Transfers leadership to another member.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransferSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public TransferSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "transfer";
    }
    
    @Override
    public String getDescription() {
        return "Transfer leadership to another member";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party transfer <player>");
            return;
        }
        
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        PartyMember member = party.getMember(player.getUniqueId());
        
        if (member == null || member.getRole() != PartyRole.LEADER) {
            player.sendMessage("§cOnly the party leader can transfer leadership!");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou are already the leader!");
            return;
        }
        
        boolean transferred = partyManager.transferLeadership(party, target.getUniqueId());        if (transferred) {
            player.sendMessage("§aTransferred leadership to §e" + target.getName() + "§a!");
            party.broadcast("§e" + target.getName() + " §ais now the party leader!");
        } else {
            player.sendMessage("§cFailed to transfer leadership!");
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
                if (memberUUID.equals(player.getUniqueId())) {
                    continue;
                }
                
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
