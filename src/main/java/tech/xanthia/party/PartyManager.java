package tech.xanthia.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all party operations.
 * 
 * Handles:
 * - Party creation/disbanding
 * - Member management
 * - Role management
 * - Team management
 * - State machine
 * - Automatic cleanup
 * - Integration with queue/duel systems
 * 
 * Performance: All operations O(1) or O(n) with minimal allocations.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyManager {
    
    private final XDuels plugin;
    
    // All active parties
    private final Map<UUID, Party> parties;
    
    // Player to party lookup
    private final Map<UUID, UUID> playerToParty;
    
    // Player profiles
    private final Map<UUID, PartyProfile> profiles;
    
    // Managers
    private final PartyInviteManager inviteManager;
    private final PartyChatManager chatManager;
    
    // Configuration
    private final int maxPartySize;
    private final int inviteExpireSeconds;
    private final int cleanupIntervalTicks;
    private final int offlineKickMinutes;
    
    /**
     * Creates a new party manager.
     * 
     * @param plugin Plugin instance
     */
    public PartyManager(XDuels plugin) {
        this.plugin = plugin;
        this.parties = new ConcurrentHashMap<>();
        this.playerToParty = new ConcurrentHashMap<>();
        this.profiles = new ConcurrentHashMap<>();
        
        // Initialize managers
        this.inviteManager = new PartyInviteManager(plugin, this);
        this.chatManager = new PartyChatManager(this);
        
        // Load from config
        this.maxPartySize = plugin.getConfig().getInt("party.max-size", 10);
        this.inviteExpireSeconds = plugin.getConfig().getInt("party.invite-expire-seconds", 60);
        this.cleanupIntervalTicks = plugin.getConfig().getInt("party.cleanup-interval-ticks", 600);
        this.offlineKickMinutes = plugin.getConfig().getInt("party.offline-kick-minutes", 5);
    }
    
    /**
     * Initializes the party manager.
     */
    public void initialize() {
        plugin.getLogger().info("[ PartyManager ] Initializing...");
        
        // Start cleanup task
        startCleanupTask();
        
        plugin.getLogger().info("[ PartyManager ] Initialized");
    }
    
    /**
     * Shuts down the party manager.
     */
    public void shutdown() {
        plugin.getLogger().info("[ PartyManager ] Shutting down...");
        
        // Disband all parties
        for (Party party : new ArrayList<>(parties.values())) {
            disbandParty(party, "Server shutdown");
        }
        
        plugin.getLogger().info("[ PartyManager ] Shutdown complete");
    }
    
    /**
     * Creates a new party with a leader.
     * 
     * @param leader Leader UUID
     * @return Created party or null if already in party
     */
    public Party createParty(UUID leader) {
        // Check if already in party
        if (playerToParty.containsKey(leader)) {
            return null;
        }
        
        Party party = new Party(leader);
        
        // Add leader as member
        Player leaderPlayer = Bukkit.getPlayer(leader);
        String leaderName = leaderPlayer != null ? leaderPlayer.getName() : "Unknown";
        PartyMember leaderMember = new PartyMember(leader, leaderName, PartyRole.LEADER);
        party.getMembers().put(leader, leaderMember);
        
        // Store party
        parties.put(party.getPartyId(), party);
        playerToParty.put(leader, party.getPartyId());
        
        // Update profile
        PartyProfile profile = getOrCreateProfile(leader);
        profile.createParty();
        profile.joinParty(party.getPartyId());
        
        plugin.getLogger().info("[ PartyManager ] Party created: " + party.getPartyId() + 
            " (Leader: " + leaderName + ")");
        
        // Fire event
        PartyEvents.firePartyCreate(party, leader);
        
        return party;
    }
    
    /**
     * Disbands a party.
     * 
     * @param party Party to disband
     * @param reason Disband reason
     */
    public void disbandParty(Party party, String reason) {
        if (party == null) {
            return;
        }
        
        plugin.getLogger().info("[ PartyManager ] Disbanding party: " + party.getPartyId() + 
            " (Reason: " + reason + ")");
        
        // Cancel queue if queuing
        if (party.getCurrentState() == PartyState.QUEUING) {
            // TODO: Integration with QueueManager
            plugin.getLogger().info("[ PartyManager ] Cancelled queue for disbanding party");
        }
        
        // Force end duel if fighting
        if (party.getCurrentState() == PartyState.FIGHTING && party.isInDuel()) {
            // TODO: Integration with DuelManager
            plugin.getLogger().warning("[ PartyManager ] Party disbanded during fight - auto-forfeit");
        }
        
        // Remove all members
        for (UUID memberUUID : new ArrayList<>(party.getMemberUUIDs())) {
            removePlayerFromParty(party, memberUUID, false);
        }
        
        // Remove party
        parties.remove(party.getPartyId());
        
        // Fire event
        PartyEvents.firePartyDisband(party, reason);
        
        // Teleport all to lobby
        teleportPartyToLobby(party);
    }
    
    /**
     * Adds a player to a party.
     * 
     * @param party Party to join
     * @param playerUUID Player UUID
     * @return True if added successfully
     */
    public boolean addPlayerToParty(Party party, UUID playerUUID) {
        // Validation
        if (party == null || playerUUID == null) {
            return false;
        }
        
        // Check if already in party
        if (playerToParty.containsKey(playerUUID)) {
            return false;
        }
        
        // Check state
        if (!party.getCurrentState().canJoin()) {
            return false;
        }
        
        // Check size
        if (party.isFull(maxPartySize)) {
            return false;
        }
        
        // Get player
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Add member
        PartyMember member = new PartyMember(playerUUID, player.getName(), PartyRole.MEMBER);
        party.getMembers().put(playerUUID, member);
        playerToParty.put(playerUUID, party.getPartyId());
        
        // Update profile
        PartyProfile profile = getOrCreateProfile(playerUUID);
        profile.joinParty(party.getPartyId());
        
        party.updateActivity();
        
        plugin.getLogger().info("[ PartyManager ] Player " + player.getName() + 
            " joined party " + party.getPartyId());
        
        // Fire event
        PartyEvents.firePlayerJoin(party, playerUUID);
        
        // Notify party
        broadcastToParty(party, "§a" + player.getName() + " §7has joined the party!");
        
        return true;
    }
    
    /**
     * Removes a player from their party.
     * 
     * @param playerUUID Player UUID
     * @return True if removed
     */
    public boolean leaveParty(UUID playerUUID) {
        UUID partyId = playerToParty.get(playerUUID);
        if (partyId == null) {
            return false;
        }
        
        Party party = parties.get(partyId);
        if (party == null) {
            playerToParty.remove(playerUUID);
            return false;
        }
        
        return removePlayerFromParty(party, playerUUID, true);
    }
    
    /**
     * Removes a player from a party.
     * 
     * @param party Party
     * @param playerUUID Player UUID
     * @param checkLeader Whether to handle leader leaving
     * @return True if removed
     */
    public boolean removePlayerFromParty(Party party, UUID playerUUID, boolean checkLeader) {
        PartyMember member = party.getMember(playerUUID);
        if (member == null) {
            return false;
        }
        
        // Handle leader leaving
        if (checkLeader && party.isLeader(playerUUID)) {
            handleLeaderLeaving(party);
            return true;
        }
        
        // Remove member
        party.getMembers().remove(playerUUID);
        playerToParty.remove(playerUUID);
        
        // Update profile
        PartyProfile profile = getOrCreateProfile(playerUUID);
        profile.leaveParty();
        
        party.updateActivity();
        
        Player player = Bukkit.getPlayer(playerUUID);
        String playerName = player != null ? player.getName() : member.getName();
        
        plugin.getLogger().info("[ PartyManager ] Player " + playerName + 
            " left party " + party.getPartyId());
        
        // Fire event
        PartyEvents.firePlayerLeave(party, playerUUID);
        
        // Notify party
        broadcastToParty(party, "§c" + playerName + " §7has left the party!");
        
        // Check if party is empty
        if (party.isEmpty()) {
            disbandParty(party, "Empty party");
        }
        
        return true;
    }
    
    /**
     * Handles leader leaving the party.
     * 
     * @param party Party
     */
    private void handleLeaderLeaving(Party party) {
        UUID oldLeader = party.getLeader();
        
        // Find new leader
        UUID newLeader = findNewLeader(party);
        
        if (newLeader == null) {
            // No one to promote, disband
            disbandParty(party, "Leader left and no members remain");
            return;
        }
        
        // Transfer leadership
        transferLeadership(party, newLeader);
        
        // Remove old leader
        party.getMembers().remove(oldLeader);
        playerToParty.remove(oldLeader);
        
        // Update profile
        PartyProfile profile = getOrCreateProfile(oldLeader);
        profile.leaveParty();
        
        Player oldLeaderPlayer = Bukkit.getPlayer(oldLeader);
        Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
        
        if (oldLeaderPlayer != null) {
            oldLeaderPlayer.sendMessage("§7You have left the party.");
        }
        
        broadcastToParty(party, "§6" + (newLeaderPlayer != null ? newLeaderPlayer.getName() : "Unknown") + 
            " §7is now the party leader!");
    }
    
    /**
     * Finds a new leader for a party.
     * 
     * Priority: oldest moderator → oldest member
     * 
     * @param party Party
     * @return New leader UUID or null
     */
    private UUID findNewLeader(Party party) {
        // Find oldest moderator
        List<PartyMember> moderators = party.getMembersByRole(PartyRole.MODERATOR);
        if (!moderators.isEmpty()) {
            moderators.sort(Comparator.comparingLong(PartyMember::getJoinedAt));
            return moderators.get(0).getUuid();
        }
        
        // Find oldest member
        List<PartyMember> members = party.getMembersByRole(PartyRole.MEMBER);
        if (!members.isEmpty()) {
            members.sort(Comparator.comparingLong(PartyMember::getJoinedAt));
            return members.get(0).getUuid();
        }
        
        return null;
    }
    
    /**
     * Transfers leadership to another player.
     * 
     * @param party Party
     * @param newLeader New leader UUID
     * @return True if transferred
     */
    public boolean transferLeadership(Party party, UUID newLeader) {
        PartyMember member = party.getMember(newLeader);
        if (member == null) {
            return false;
        }
        
        // Demote old leader to member
        PartyMember oldLeaderMember = party.getMember(party.getLeader());
        if (oldLeaderMember != null) {
            oldLeaderMember.setRole(PartyRole.MEMBER);
        }
        
        // Promote new leader
        member.setRole(PartyRole.LEADER);
        
        party.updateActivity();
        
        Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
        broadcastToParty(party, "§6" + (newLeaderPlayer != null ? newLeaderPlayer.getName() : "Unknown") + 
            " §7is now the party leader!");
        
        // Fire event
        PartyEvents.fireLeadershipTransfer(party, party.getLeader(), newLeader);
        
        return true;
    }
    
    /**
     * Kicks a player from the party.
     * 
     * @param party Party
     * @param kicker Kicker UUID
     * @param target Target UUID
     * @return True if kicked
     */
    public boolean kickPlayer(Party party, UUID kicker, UUID target) {
        // Validation
        if (party == null || kicker == null || target == null) {
            return false;
        }
        
        // Check state
        if (!party.getCurrentState().canKick()) {
            return false;
        }
        
        // Check permissions
        PartyRole kickerRole = party.getRole(kicker);
        PartyRole targetRole = party.getRole(target);
        
        if (kickerRole == null || targetRole == null) {
            return false;
        }
        
        if (!kickerRole.canKick(targetRole)) {
            return false;
        }
        
        // Cannot kick self
        if (kicker.equals(target)) {
            return false;
        }
        
        // Remove player
        party.getMembers().remove(target);
        playerToParty.remove(target);
        
        // Update profile
        PartyProfile profile = getOrCreateProfile(target);
        profile.kicked();
        
        Player targetPlayer = Bukkit.getPlayer(target);
        String targetName = targetPlayer != null ? targetPlayer.getName() : "Unknown";
        
        plugin.getLogger().info("[ PartyManager ] Player " + targetName + 
            " kicked from party " + party.getPartyId());
        
        // Fire event
        PartyEvents.firePlayerKick(party, kicker, target);
        
        // Notify
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§cYou have been kicked from the party!");
            // Teleport to lobby
            // TODO: Integration with spawn system
        }
        
        Player kickerPlayer = Bukkit.getPlayer(kicker);
        broadcastToParty(party, "§c" + targetName + " §7was kicked by §6" + 
            (kickerPlayer != null ? kickerPlayer.getName() : "Unknown"));
        
        return true;
    }
    
    /**
     * Promotes a player to moderator or leader.
     * 
     * @param party Party
     * @param promoter Promoter UUID (must be leader)
     * @param target Target UUID
     * @return True if promoted
     */
    public boolean promotePlayer(Party party, UUID promoter, UUID target) {
        // Only leader can promote
        if (!party.isLeader(promoter)) {
            return false;
        }
        
        PartyMember member = party.getMember(target);
        if (member == null) {
            return false;
        }
        
        // Promote based on current role
        if (member.getRole() == PartyRole.MEMBER) {
            member.setRole(PartyRole.MODERATOR);
        } else if (member.getRole() == PartyRole.MODERATOR) {
            // Cannot promote moderator to leader via this method
            return false;
        } else {
            return false;
        }
        
        party.updateActivity();
        
        Player targetPlayer = Bukkit.getPlayer(target);
        broadcastToParty(party, "§6" + (targetPlayer != null ? targetPlayer.getName() : "Unknown") + 
            " §7has been promoted to §eModerator§7!");
        
        return true;
    }
    
    /**
     * Demotes a player.
     * 
     * @param party Party
     * @param demoter Demoter UUID (must be leader)
     * @param target Target UUID
     * @return True if demoted
     */
    public boolean demotePlayer(Party party, UUID demoter, UUID target) {
        // Only leader can demote
        if (!party.isLeader(demoter)) {
            return false;
        }
        
        PartyMember member = party.getMember(target);
        if (member == null) {
            return false;
        }
        
        // Can only demote moderators
        if (member.getRole() != PartyRole.MODERATOR) {
            return false;
        }
        
        member.setRole(PartyRole.MEMBER);
        party.updateActivity();
        
        Player targetPlayer = Bukkit.getPlayer(target);
        broadcastToParty(party, "§6" + (targetPlayer != null ? targetPlayer.getName() : "Unknown") + 
            " §7has been demoted to §7Member§7!");
        
        return true;
    }
    
    /**
     * Gets a party by UUID.
     * 
     * @param partyId Party UUID
     * @return Party or null
     */
    public Party getParty(UUID partyId) {
        return parties.get(partyId);
    }
    
    /**
     * Gets a player's party.
     * 
     * @param playerUUID Player UUID
     * @return Party or null
     */
    public Party getPlayerParty(UUID playerUUID) {
        UUID partyId = playerToParty.get(playerUUID);
        return partyId != null ? parties.get(partyId) : null;
    }
    
    /**
     * Checks if a player is in a party.
     * 
     * @param playerUUID Player UUID
     * @return True if in party
     */
    public boolean isInParty(UUID playerUUID) {
        return playerToParty.containsKey(playerUUID);
    }
    
    /**
     * Gets or creates a player profile.
     * 
     * @param playerUUID Player UUID
     * @return Party profile
     */
    public PartyProfile getOrCreateProfile(UUID playerUUID) {
        return profiles.computeIfAbsent(playerUUID, PartyProfile::new);
    }
    
    /**
     * Broadcasts a message to all party members.
     * 
     * @param party Party
     * @param message Message
     */
    public void broadcastToParty(Party party, String message) {
        for (UUID memberUUID : party.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Teleports entire party to lobby.
     * 
     * @param party Party
     */
    private void teleportPartyToLobby(Party party) {
        // TODO: Integration with spawn system
        for (UUID memberUUID : party.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            if (player != null && player.isOnline()) {
                // player.teleport(lobbySpawn);
            }
        }
    }
    
    /**
     * Starts the automatic cleanup task.
     */
    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            cleanupParties();
        }, cleanupIntervalTicks, cleanupIntervalTicks);
    }
    
    /**
     * Cleans up parties (removes offline players, expired invites, etc.).
     */
    private void cleanupParties() {
        long now = System.currentTimeMillis();
        int offlineKickMillis = offlineKickMinutes * 60 * 1000;
        
        for (Party party : new ArrayList<>(parties.values())) {
            // Remove expired invites
            party.removeExpiredInvites();
            
            // Remove offline players
            for (PartyMember member : new ArrayList<>(party.getMembers().values())) {
                if (now - member.getLastActivity() > offlineKickMillis) {
                    plugin.getLogger().info("[ PartyManager ] Removing offline player: " + 
                        member.getName());
                    removePlayerFromParty(party, member.getUuid(), true);
                }
            }
            
            // Auto-disband empty parties
            if (party.isEmpty()) {
                disbandParty(party, "Empty after cleanup");
            }
        }
    }
    
    /**
     * Gets all active parties.
     * 
     * @return Collection of parties
     */
    public Collection<Party> getAllParties() {
        return Collections.unmodifiableCollection(parties.values());
    }
    
    /**
     * Gets the maximum party size.
     * 
     * @return Max party size
     */
    public int getMaxPartySize() {
        return maxPartySize;
    }
    
    /**
     * Gets the invite expiration time in seconds.
     * 
     * @return Invite expire seconds
     */
    public int getInviteExpireSeconds() {
        return inviteExpireSeconds;
    }
    
    /**
     * Gets the invite manager.
     * 
     * @return Invite manager
     */
    public PartyInviteManager getInviteManager() {
        return inviteManager;
    }
    
    /**
     * Gets the chat manager.
     * 
     * @return Chat manager
     */
    public PartyChatManager getChatManager() {
        return chatManager;
    }
}
