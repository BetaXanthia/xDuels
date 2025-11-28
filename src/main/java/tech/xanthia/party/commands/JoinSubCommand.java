package tech.xanthia.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * /party join <player> - Joins a player's party by accepting their invite.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class JoinSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public JoinSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "join";
    }
    
    @Override
    public String getDescription() {
        return "Join a player's party";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /party join <player>");
            return;
        }
        
        if (partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }
        
        Party targetParty = partyManager.getPlayerParty(target.getUniqueId());
        
        if (targetParty == null) {
            player.sendMessage("§cThat player is not in a party!");
            return;
        }
        
        boolean accepted = partyManager.getInviteManager().acceptInvite(
            player.getUniqueId(), targetParty.getPartyId());
        
        if (accepted) {
            player.sendMessage("§aYou joined the party!");
            targetParty.broadcast("§e" + player.getName() + " §ajoined the party!");
        } else {
            player.sendMessage("§cYou don't have an invite to that party!");
        }
    }
    
    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(input) && 
                    partyManager.isInParty(online.getUniqueId())) {
                    players.add(online.getName());
                }
            }
            
            return players;
        }
        
        return List.of();
    }
}
