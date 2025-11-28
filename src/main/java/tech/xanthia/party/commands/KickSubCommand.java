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
 * /party kick <player> - Kicks a member from the party.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KickSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public KickSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "kick";
    }
    
    @Override
    public String getDescription() {
        return "Kick a member from the party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party kick <player>");
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
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot kick yourself! Use /party leave instead.");
            return;
        }
        
        boolean kicked = partyManager.kickPlayer(party, player.getUniqueId(), 
            target.getUniqueId());
        
        if (kicked) {
            player.sendMessage("§aKicked §e" + target.getName() + " §afrom the party!");
        } else {
            player.sendMessage("§cFailed to kick player! Check your permissions.");
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
