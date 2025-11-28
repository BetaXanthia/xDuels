package tech.xanthia.party.utils;

import org.bukkit.Location;
import tech.xanthia.party.Party;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages FFA spawn generation and positioning.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyFFAManager {
    
    private static final double MIN_PLAYER_SPACING = 3.0; // blocks
    
    /**
     * Generates FFA spawn positions in a circle.
     * 
     * @param center Center location
     * @param playerCount Number of players
     * @return List of spawn locations
     */
    public static List<Location> generateFFASpawns(Location center, int playerCount) {
        if (playerCount < 2) {
            throw new IllegalArgumentException("FFA requires at least 2 players");
        }
        
        double radius = calculateRadius(playerCount);
        List<Location> spawns = new ArrayList<>();
        
        for (int i = 0; i < playerCount; i++) {
            double angle = getSpawnAngle(i, playerCount);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location spawn = new Location(center.getWorld(), x, center.getY(), z);
            
            // Face center
            spawn.setDirection(center.toVector().subtract(spawn.toVector()));
            
            spawns.add(spawn);
        }
        
        return spawns;
    }
    
    /**
     * Calculates optimal FFA circle radius.
     * 
     * Formula: 2πr = players * spacing
     * r = (players * spacing) / (2π)
     * 
     * @param playerCount Number of players
     * @return Radius in blocks
     */
    public static double calculateRadius(int playerCount) {
        return (playerCount * MIN_PLAYER_SPACING) / (2 * Math.PI);
    }
    
    /**
     * Gets spawn angle for player index.
     * 
     * @param index Player index
     * @param total Total players
     * @return Angle in radians
     */
    public static double getSpawnAngle(int index, int total) {
        return (2 * Math.PI * index) / total;
    }
    
    /**
     * Validates FFA mode for a party.
     * 
     * @param party Party
     * @return True if FFA is valid
     */
    public static boolean validateFFA(Party party) {
        if (!party.isFfaMode()) {
            return false;
        }
        
        // Need at least 2 players for FFA
        return party.getMemberCount() >= 2;
    }
}
