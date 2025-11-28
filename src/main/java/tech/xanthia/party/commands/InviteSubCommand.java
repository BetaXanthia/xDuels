package tech.xanthia.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * /party invite <player> - Invites a player to the party.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class InviteSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public InviteSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "invite";
    }
    
    @Override
    public String getDescription() {
        return "Invite a player to your party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party invite <player>");
            return;
        }
        
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party! Use /party create first.");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot invite yourself!");
            return;
        }
        
        if (partyManager.isInParty(target.getUniqueId())) {
            player.sendMessage("§cThat player is already in a party!");
            return;
        }
        
        boolean sent = partyManager.getInviteManager().sendInvite(
            party, player.getUniqueId(), target.getUniqueId());
        
        if (sent) {
            player.sendMessage("§aInvited §e" + target.getName() + " §ato the party!");
            party.broadcast("§e" + player.getName() + " §7invited §e" + 
                target.getName() + " §7to the party!");
        } else {
            player.sendMessage("§cFailed to send invite!");
        }
    }
    
    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(input) && 
                    !online.getUniqueId().equals(player.getUniqueId())) {
                    players.add(online.getName());
                }
            }
            
            return players;
        }
        
        return List.of();
    }
}
