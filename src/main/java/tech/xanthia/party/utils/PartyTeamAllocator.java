package tech.xanthia.party.utils;

import tech.xanthia.party.Party;
import tech.xanthia.party.PartyMember;
import tech.xanthia.party.PartyRole;

import java.util.*;

/**
 * Handles automatic team allocation and balancing.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyTeamAllocator {
    
    /**
     * Allocates party members to teams evenly.
     * 
     * @param party Party
     * @param teamCount Number of teams
     */
    public static void allocateTeams(Party party, int teamCount) {
        if (teamCount < 2) {
            throw new IllegalArgumentException("Team count must be at least 2");
        }
        
        List<UUID> members = new ArrayList<>(party.getMemberUUIDs());
        
        // Leader always goes to team 0
        UUID leader = party.getLeader();
        party.assignTeam(leader, 0);
        members.remove(leader);
        
        // Distribute remaining members
        int currentTeam = 1;
        for (UUID memberUUID : members) {
            party.assignTeam(memberUUID, currentTeam % teamCount);
            currentTeam++;
        }
    }
    
    /**
     * Balances teams to have equal (or near-equal) sizes.
     * 
     * @param party Party
     */
    public static void balanceTeams(Party party) {
        // Get all assigned teams
        Map<Integer, List<UUID>> teams = new HashMap<>();
        
        for (UUID memberUUID : party.getMemberUUIDs()) {
            PartyMember member = party.getMember(memberUUID);
            if (member == null) {
                continue;
            }
            
            int team = member.getTeamNumber();
            if (team >= 0) {
                teams.computeIfAbsent(team, k -> new ArrayList<>()).add(memberUUID);
            }
        }
        
        if (teams.size() < 2) {
            return; // Nothing to balance
        }
        
        // Calculate target size
        int totalPlayers = party.getMemberCount();
        int teamCount = teams.size();
        int targetSize = totalPlayers / teamCount;
        
        // Balance teams using greedy algorithm
        List<Map.Entry<Integer, List<UUID>>> sortedTeams = new ArrayList<>(teams.entrySet());
        sortedTeams.sort(Comparator.comparingInt(e -> e.getValue().size()));
        
        for (int i = sortedTeams.size() - 1; i >= 0; i--) {
            Map.Entry<Integer, List<UUID>> largeTeam = sortedTeams.get(i);
            
            while (largeTeam.getValue().size() > targetSize + 1) {
                // Find smallest team
                Map.Entry<Integer, List<UUID>> smallTeam = sortedTeams.get(0);
                
                if (smallTeam.getValue().size() >= targetSize) {
                    break;
                }
                
                // Move player from large to small team
                UUID playerToMove = largeTeam.getValue().remove(largeTeam.getValue().size() - 1);
                
                // Don't move leader
                PartyMember member = party.getMember(playerToMove);
                if (member != null && member.getRole() == PartyRole.LEADER) {
                    continue;
                }
                
                party.assignTeam(playerToMove, smallTeam.getKey());
                smallTeam.getValue().add(playerToMove);
                
                // Re-sort
                sortedTeams.sort(Comparator.comparingInt(e -> e.getValue().size()));
            }
        }
    }
    
    /**
     * Validates that all party members are assigned to teams.
     * 
     * @param party Party
     * @return True if valid
     */
    public static boolean validateTeams(Party party) {
        for (UUID memberUUID : party.getMemberUUIDs()) {
            PartyMember member = party.getMember(memberUUID);
            
            if (member == null || member.getTeamNumber() < 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets team sizes.
     * 
     * @param party Party
     * @return Map of team number to size
     */
    public static Map<Integer, Integer> getTeamSizes(Party party) {
        Map<Integer, Integer> sizes = new HashMap<>();
        
        for (UUID memberUUID : party.getMemberUUIDs()) {
            PartyMember member = party.getMember(memberUUID);
            
            if (member != null && member.getTeamNumber() >= 0) {
                int team = member.getTeamNumber();
                sizes.put(team, sizes.getOrDefault(team, 0) + 1);
            }
        }
        
        return sizes;
    }
}
