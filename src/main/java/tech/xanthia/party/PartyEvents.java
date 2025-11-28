package tech.xanthia.party;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Centralized party event firing system.
 * 
 * All party events are fired through this class.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyEvents {
    
    /**
     * Fires party create event.
     * 
     * @param party Party
     * @param leader Leader UUID
     */
    public static void firePartyCreate(Party party, UUID leader) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
    
    /**
     * Fires party disband event.
     * 
     * @param party Party
     * @param reason Reason
     */
    public static void firePartyDisband(Party party, String reason) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
    
    /**
     * Fires player join event.
     * 
     * @param party Party
     * @param playerUUID Player UUID
     */
    public static void firePlayerJoin(Party party, UUID playerUUID) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
    
    /**
     * Fires player leave event.
     * 
     * @param party Party
     * @param playerUUID Player UUID
     */
    public static void firePlayerLeave(Party party, UUID playerUUID) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
    
    /**
     * Fires player kick event.
     * 
     * @param party Party
     * @param kicker Kicker UUID
     * @param kicked Kicked UUID
     */
    public static void firePlayerKick(Party party, UUID kicker, UUID kicked) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
    
    /**
     * Fires leadership transfer event.
     * 
     * @param party Party
     * @param oldLeader Old leader UUID
     * @param newLeader New leader UUID
     */
    public static void fireLeadershipTransfer(Party party, UUID oldLeader, UUID newLeader) {
        // TODO: Call Bukkit.getPluginManager().callEvent() with custom event
    }
}
