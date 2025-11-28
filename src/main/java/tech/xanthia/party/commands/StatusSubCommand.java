package tech.xanthia.party.commands;

import org.bukkit.entity.Player;
import tech.xanthia.party.*;

import java.util.UUID;

/**
 * /party status - Displays party information.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusSubCommand implements SubCommand {
    
    private final PartyManager partyManager;
    
    public StatusSubCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }
    
    @Override
    public String getName() {
        return "status";
    }
    
    @Override
    public String getDescription() {
        return "View party status";
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Party party = partyManager.getPlayerParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        player.sendMessage("§6§l=== Party Status ===");
        
        // Leader
        UUID leaderUUID = party.getLeader();
        PartyMember leader = party.getMember(leaderUUID);
        player.sendMessage("§eLeader: §f" + (leader != null ? leader.getName() : "Unknown"));
        
        // Members
        player.sendMessage("§eMembers: §f" + party.getMemberCount());
        
        // List members by role
        player.sendMessage("§aModerators:");
        for (UUID modUUID : party.getModerators()) {
            PartyMember mod = party.getMember(modUUID);
            if (mod != null) {
                player.sendMessage("  §7- §f" + mod.getName());
            }
        }
        
        player.sendMessage("§7Members:");
        for (UUID memberUUID : party.getMemberUUIDs()) {
            PartyMember member = party.getMember(memberUUID);
            if (member != null && member.getRole() == PartyRole.MEMBER) {
                player.sendMessage("  §7- §f" + member.getName());
            }
        }
        
        // State
        player.sendMessage("§eState: §f" + party.getCurrentState().getDisplayName());
        
        // Modes
        if (party.isFfaMode()) {
            player.sendMessage("§eMode: §6FFA");
        } else if (party.isSplitMode()) {
            player.sendMessage("§eMode: §bSplit Teams");
        }
        
        // Chat
        player.sendMessage("§eParty Chat: " + (party.isChatEnabled() ? "§aEnabled" : "§cDisabled"));
    }
}
