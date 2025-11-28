package tech.xanthia.party.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.xanthia.party.Party;
import tech.xanthia.party.PartyMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles party teleportation operations.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyTeleportHandler {
    
    /**
     * Teleports all party members to a location.
     * 
     * @param party Party
     * @param location Target location
     */
    public static void teleportParty(Party party, Location location) {
        for (UUID memberUUID : party.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            
            if (player != null && player.isOnline()) {
                player.teleport(location);
            }
        }
    }
    
    /**
     * Teleports party to arena with team-based positioning.
     * 
     * @param party Party
     * @param spawn1 Team 1 spawn
     * @param spawn2 Team 2 spawn
     */
    public static void teleportToArena(Party party, Location spawn1, Location spawn2) {
        if (party.isFfaMode()) {
            // FFA positioning handled by PartyFFAManager
            return;
        }
        
        for (UUID memberUUID : party.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            PartyMember member = party.getMember(memberUUID);
            if (member == null) {
                continue;
            }
            
            int team = member.getTeamNumber();
            
            if (team == 0) {
                player.teleport(spawn1);
            } else if (team == 1) {
                player.teleport(spawn2);
            } else {
                // Unassigned, default to spawn1
                player.teleport(spawn1);
            }
        }
    }
    
    /**
     * Teleports party to lobby spawn.
     * 
     * @param party Party
     * @param lobbySpawn Lobby spawn location
     */
    public static void teleportToLobby(Party party, Location lobbySpawn) {
        teleportParty(party, lobbySpawn);
    }
    
    /**
     * Positions party members in FFA circle.
     * 
     * @param party Party
     * @param center Center location
     * @param radius Circle radius
     */
    public static void positionFFACircle(Party party, Location center, double radius) {
        List<UUID> members = new ArrayList<>(party.getMemberUUIDs());
        int count = members.size();
        
        for (int i = 0; i < count; i++) {
            UUID memberUUID = members.get(i);
            Player player = Bukkit.getPlayer(memberUUID);
            
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Calculate position on circle
            double angle = (2 * Math.PI * i) / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location spawnLoc = new Location(center.getWorld(), x, center.getY(), z);
            
            // Face center
            spawnLoc.setDirection(center.toVector().subtract(spawnLoc.toVector()));
            
            player.teleport(spawnLoc);
        }
    }
}
