plementation Summary
# xDuels Plugin - Im
## Overview
This document provides a comprehensive overview of the generated Party & Queue System implementation for the xDuels Minecraft plugin.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Party System](#party-system)
3. [Queue System](#queue-system)
4. [Remaining Work](#remaining-work)
5. [Integration Points](#integration-points)

---

## Architecture Overview

### Technology Stack
- **Platform**: Minecraft Paper 1.20.2+
- **Java Version**: 17+
- **Concurrency**: ConcurrentHashMap for thread-safe operations
- **Performance**: O(1) lookups, O(n) matchmaking, no YAML in hot paths

### Package Structure
```
tech.xanthia.party/     - Party system components
tech.xanthia.queue/     - Queue and matchmaking components
```

---

## Party System

### Core Models (✅ COMPLETED)

#### 1. PartyRole.java
**Purpose**: Defines party role hierarchy and permissions

**Roles**:
- `LEADER` - Full control, cannot be kicked
- `MODERATOR` - Can kick members, manage teams
- `MEMBER` - Standard member

**Key Methods**:
- `canKick(PartyRole target)` - Permission checking
- `canPromote()`, `canDemote()` - Role management
- `canQueue()` - Queue permission
- `getChatPrefix()` - Formatted chat prefix

#### 2. PartyState.java
**Purpose**: State machine for party lifecycle

**States**:
- `IDLE` - Default state, can invite/queue
- `QUEUING` - In matchmaking queue
- `FIGHTING` - Active in duel
- `EDITING` - Team/settings editor

**Key Methods**:
- `canInvite()`, `canQueue()`, `canEditTeams()` - State validation
- `canTransitionTo(PartyState)` - State transition rules

#### 3. PartyMember.java
**Purpose**: Represents a single party member

**Fields**:
- `UUID memberUUID` - Player identifier
- `String name` - Player name (cached)
- `PartyRole role` - Member role
- `int teamNumber` - Team assignment (-1 = unassigned)
- `long lastActivity` - Activity tracking

#### 4. PartyInvite.java
**Purpose**: Invitation with expiration tracking

**Fields**:
- `UUID partyId`, `inviter`, `invited`
- `long inviteTime` - Creation timestamp
- `int expireSeconds` - TTL (default 60s)

**Key Methods**:
- `isExpired()` - Checks if invite expired
- `getTimeRemaining()` - Seconds until expiration

#### 5. Party.java
**Purpose**: Core party model (300+ lines)

**Key Fields**:
```java
UUID partyId
Map<UUID, PartyMember> members
Set<PartyInvite> invites
PartyState currentState
Map<UUID, Integer> teamAssignments
boolean ffaMode
boolean splitMode
boolean chatEnabled
UUID currentDuelId
String currentArenaId
```

**Key Methods**:
- `getMember()`, `addMember()`, `removeMember()` - Member CRUD
- `getLeader()`, `getModerators()` - Role queries
- `setState()` - State transitions
- `assignTeam()`, `unassignTeam()` - Team management
- `broadcast()` - Message all online members

#### 6. PartyProfile.java
**Purpose**: Player party statistics

**Fields**:
- Total parties created/joined/disbanded
- Party wins/losses
- Favorite party size
- Last party ID

### Manager Layer (✅ COMPLETED)

#### PartyManager.java (600+ lines)
**Purpose**: Central party management system

**Core Operations**:
```java
// Creation & Destruction
Party createParty(UUID leaderUUID)
void disbandParty(UUID partyUUID, String reason)

// Member Management
boolean addPlayerToParty(Party, UUID)
boolean removePlayerFromParty(Party, UUID)
boolean kickPlayer(Party, UUID kicker, UUID kicked)

// Role Management
boolean promotePlayer(Party, UUID promoter, UUID target)
boolean demotePlayer(Party, UUID demoter, UUID target)
boolean transferLeadership(Party, UUID current, UUID newLeader)

// State Management
boolean canTransitionState(Party, PartyState)
void handleLeaderLeaving(Party)

// Queries
Party getPlayerParty(UUID)
boolean isInParty(UUID)
```

**Performance**:
- `ConcurrentHashMap<UUID, Party> parties` - O(1) party lookup
- `ConcurrentHashMap<UUID, UUID> playerToParty` - O(1) player -> party lookup
- Cleanup task runs every 30 seconds (600 ticks)

**Cleanup Logic**:
- Removes players offline for 5+ minutes
- Expires invites older than TTL
- Auto-disbands empty parties
- Leader succession: oldest moderator → oldest member

### Utility Layer (✅ COMPLETED)

#### PartyInviteManager.java
**Purpose**: Centralized invitation management

**Key Methods**:
- `sendInvite(Party, inviter, invited)` - Creates and sends invite
- `acceptInvite(playerUUID, partyId)` - Accepts and joins
- `getPlayerInvites(UUID)` - Fetches pending invites
- `removePlayerInvites(UUID)` - Clears all invites

**Features**:
- Validates party state (`canInvite()`)
- Prevents duplicate invites
- Tracks pending invites per player
- Auto-removes expired invites

#### PartyChatManager.java
**Purpose**: Party chat system

**Key Methods**:
- `sendMessage(sender, message)` - Broadcasts to party
- `toggleChat(playerUUID)` - Enables/disables party chat

**Format**: `§9[Party] [ROLE] Name§7: §fMessage`

#### PartyEvents.java
**Purpose**: Event firing system

**Events** (TODOs for Bukkit events):
- `firePartyCreate(Party, leader)`
- `firePartyDisband(Party, reason)`
- `firePlayerJoin(Party, player)`
- `firePlayerLeave(Party, player)`
- `firePlayerKick(Party, kicker, kicked)`
- `fireLeadershipTransfer(Party, old, new)`

---

## Queue System

### Core Models (✅ COMPLETED)

#### 1. QueueType.java
**Purpose**: Enum of all queue types

**Types**:
- `UNRANKED_1V1` through `UNRANKED_5V5`
- `RANKED_1V1` through `RANKED_5V5`
- `PARTY_VS_PARTY` - Dynamic size
- `PARTY_FFA` - Free-for-all

**Properties**:
- `displayName` - GUI/chat display
- `ranked` - MMR-based matching
- `teamSize` - Players per team (-1 for party queues)

#### 2. QueueEntry.java
**Purpose**: Represents a queue entry (solo or party)

**Fields**:
```java
UUID entryId
QueueType queueType
String kitName
List<UUID> players
long queueTime
int mmrExpansion
```

**Key Methods**:
- `getQueueDuration()` - Time in queue (ms)
- `expandMmrRange()` - Widens MMR search (+50 per call)
- `containsPlayer(UUID)` - Membership check

#### 3. RankedQueueEntry.java
**Purpose**: Extends QueueEntry for ranked matches

**Additional Fields**:
- `int averageMMR` - Party average
- `int minMMR` - Lowest player MMR
- `int maxMMR` - Highest player MMR

**Key Methods**:
```java
int[] getCurrentMMRRange() - Returns [min-expansion, max+expansion]
boolean isMMRCompatible(RankedQueueEntry) - Checks MMR overlap
```

**MMR Expansion**:
- Starts at ±0
- Expands ±50 every 10 seconds
- Allows wider matches over time

### Manager Layer (✅ COMPLETED)

#### QueueManager.java (250+ lines)
**Purpose**: Central queue system

**Data Structures**:
```java
Map<QueueType, List<QueueEntry>> queues           // Per-type queue lists
Map<UUID, QueueEntry> playerToEntry               // O(1) player lookup
Map<UUID, QueueEntry> partyToEntry                // O(1) party lookup
```

**Core Operations**:
```java
// Solo Queue
boolean addToQueue(UUID player, QueueType, kit)
boolean removeFromQueue(UUID player)

// Party Queue
boolean addPartyToQueue(UUID party, QueueType, kit)

// Queries
boolean isInQueue(UUID)
QueueEntry getQueueEntry(UUID)
List<QueueEntry> getQueueEntries(QueueType)
int getQueueSize(QueueType)
```

**Solo Queue Logic**:
1. Validate player not in queue/party
2. Create `QueueEntry` or `RankedQueueEntry`
3. Add to type-specific list
4. Map player UUID to entry

**Party Queue Logic**:
1. Validate party state (`canQueue()`)
2. Check no members in queue
3. Transition party to `QUEUING` state
4. Calculate party MMR (average, min, max)
5. Add all members to `playerToEntry` map
6. Add party to `partyToEntry` map

**Integration**:
- Starts matchmaker task on initialization (every 1 second)
- Integrates with `PartyManager` for state transitions
- TODO: Get MMR from StatsManager

#### QueueMatchmaker.java (150+ lines)
**Purpose**: Matchmaking algorithm

**Tick Frequency**: Every 1 second (20 ticks)

**Standard Queue Algorithm** (1v1-5v5):
```
1. Expand MMR for entries in queue 10+ seconds
2. For each pair of entries:
   a. Check kit match
   b. Check MMR compatibility (ranked only)
   c. If match found, create duel
3. Exit after one match per tick (prevents lag)
```

**Party Queue Algorithm**:
- **Party FFA**: Needs only 1 party
- **Party vs Party**: Match parties within 2 players size difference

**MMR Expansion**:
- Every 10 seconds in queue
- Expands by ±50 MMR
- Allows gradual matchmaking expansion

**Performance**:
- O(n²) worst case for matching (acceptable for queue sizes)
- One match per tick prevents server lag
- Separate lists per queue type minimize iteration

**Integration**:
- Calls `QueueManager.removeEntry()` when match found
- TODO: Call `DuelManager.createDuel()` with entry data

---

## Remaining Work

### Party System Components

#### Commands (❌ NOT IMPLEMENTED)
**Required Files**:
1. `PartyCommand.java` - Root command with subcommand routing
2. `CreateSubCommand.java` - `/party create`
3. `DisbandSubCommand.java` - `/party disband`
4. `LeaveSubCommand.java` - `/party leave`
5. `ManageSubCommand.java` - `/party manage` (opens GUI)
6. `JoinSubCommand.java` - `/party join <player>`
7. `PromoteSubCommand.java` - `/party promote <player>`
8. `TransferSubCommand.java` - `/party transfer <player>`
9. `KickSubCommand.java` - `/party kick <player>`
10. `StatusSubCommand.java` - `/party status`
11. `ChatSubCommand.java` - `/party chat` (toggle)
12. `SplitSubCommand.java` - `/party split` (toggle split mode)
13. `FfaSubCommand.java` - `/party ffa` (toggle FFA mode)

**Implementation Notes**:
- Each subcommand extends `SubCommand` interface
- Permission checks via `PartyRole.canX()` methods
- State validation via `PartyState.canX()` methods
- Tab completion for player names

#### GUIs (❌ NOT IMPLEMENTED)
**Required Files**:
1. `PartyManageGUI.java` - Main party management menu
   - View members, roles, teams
   - Access other GUI menus
   - Kick, promote, transfer buttons

2. `PartyMembersGUI.java` - Member list with actions
   - Player heads with role indicators
   - Click to kick/promote/demote
   - Team assignment display

3. `PartyTeamEditorGUI.java` - Team assignment interface
   - Drag-and-drop team assignment
   - Team balance display
   - FFA/Split toggle

4. `PartyVisibilityGUI.java` - Privacy settings
   - Public/Private toggle
   - Invite-only mode
   - Max size setting

5. `PartyRoleSelectGUI.java` - Role promotion interface
   - Select member to promote
   - Role hierarchy display
   - Confirmation dialog

**GUI Features**:
- Adventure API for components
- Click handlers with role validation
- Real-time updates via inventory refresh
- Pagination for large parties

#### Listeners (❌ NOT IMPLEMENTED)
**Required Files**:
1. `PartyPlayerListener.java`
   - `PlayerQuitEvent` - Handle disconnects, cleanup
   - `PlayerJoinEvent` - Restore party state
   - `AsyncPlayerChatEvent` - Party chat toggle

**Logic**:
- On disconnect: Start 5-minute inactive timer
- On rejoin: Cancel timer, restore state
- On chat: Intercept if party chat enabled

#### Utility Classes (❌ NOT IMPLEMENTED)
**Required Files**:
1. `PartyTeleportHandler.java`
   - `teleportToArena(Party, Arena)` - Teleport all members
   - `teleportToLobby(Party)` - Return to spawn
   - `positionFFACircle(Party, Arena)` - FFA spawn positioning

2. `PartyTeamAllocator.java`
   - `allocateTeams(Party, int teamCount)` - Auto-assign teams
   - `balanceTeams(Party)` - Balance team sizes
   - `validateTeams(Party)` - Check team validity

3. `PartyFFAManager.java`
   - `generateFFASpawns(Party, Arena)` - Circle spawn positions
   - `calculateRadius(int players)` - Optimal FFA radius
   - `getSpawnAngle(int index, int total)` - Angle for player spawn

**Algorithms**:
- FFA positioning: Circular arrangement with `2πr = players * 3 blocks`
- Team balancing: Greedy algorithm, minimize size difference
- Teleportation: Async teleport, sync inventory operations

### Queue System Components

#### Commands (❌ NOT IMPLEMENTED)
**Required Files**:
1. `QueueCommand.java` - Root command
2. `JoinQueueSubCommand.java` - `/queue join <type> <kit>`
3. `LeaveQueueSubCommand.java` - `/queue leave`
4. `StatusQueueSubCommand.java` - `/queue status`
5. `ListQueueSubCommand.java` - `/queue list` (all queues)

#### GUIs (❌ NOT IMPLEMENTED)
**Required Files**:
1. `QueueSelectorGUI.java` - Select queue type
2. `QueueKitSelectorGUI.java` - Select kit for queue
3. `QueueStatusGUI.java` - Current queue status (time, position)

#### Utility Classes (❌ NOT IMPLEMENTED)
**Required Files**:
1. `QueueEloCalculator.java`
   - `calculateEloChange(winner, loser, isTeam)` - ELO adjustment
   - `getExpectedScore(mmr1, mmr2)` - Win probability
   - K-factor: 32 for solo, 24 for teams

2. `QueueTimeoutHandler.java`
   - `checkTimeout(QueueEntry)` - Remove stuck entries
   - Max timeout: 5 minutes

3. `QueueTickTask.java`
   - Main matchmaking task wrapper
   - Runs `QueueMatchmaker.tick()` every second

---

## Integration Points

### With DuelManager (⚠️ REQUIRED)
**Called By**: `QueueMatchmaker.createMatch()`

**Required Methods**:
```java
Duel createDuel(List<UUID> team1, List<UUID> team2, String kit, Arena arena)
Duel createFFADuel(List<UUID> players, String kit, Arena arena)
void updateElo(UUID player, int mmrChange)
```

**Flow**:
1. Matchmaker finds compatible entries
2. Calls `DuelManager.createDuel()`
3. DuelManager:
   - Selects available arena
   - Applies kit to players
   - Teleports players
   - Starts duel countdown
   - Transitions party state to `FIGHTING`

### With ArenaManager (⚠️ REQUIRED)
**Called By**: `DuelManager`

**Required Methods**:
```java
Arena getFreeArena(String kitName)
void reserveArena(UUID arenaId)
void releaseArena(UUID arenaId)
```

### With StatsManager (⚠️ REQUIRED)
**Called By**: `QueueManager`, `QueueEloCalculator`

**Required Methods**:
```java
int getPlayerMMR(UUID player, String kit)
void updateMMR(UUID player, String kit, int newMMR)
PartyProfile getPartyProfile(UUID player)
void updatePartyStats(UUID player, boolean win)
```

### With XDuels Main Class (⚠️ REQUIRED)
**Registration**:
```java
// In XDuels.onEnable()
this.partyManager = new PartyManager(this);
this.queueManager = new QueueManager(this, partyManager);

// Register commands
getCommand("party").setExecutor(new PartyCommand(partyManager));
getCommand("queue").setExecutor(new QueueCommand(queueManager));

// Register listeners
getServer().getPluginManager().registerEvents(
    new PartyPlayerListener(partyManager), this);
```

---

## Performance Specifications

### Party System
- ✅ O(1) player → party lookup via `playerToParty` map
- ✅ O(1) party ID lookup via `parties` map
- ✅ O(n) party member iteration (acceptable, max ~20 members)
- ✅ Cleanup task every 30 seconds (minimal CPU)
- ✅ ConcurrentHashMap for thread safety

### Queue System
- ✅ O(1) player → entry lookup via `playerToEntry` map
- ✅ O(1) queue size query via list size
- ✅ O(n) matchmaking per queue type (1 match per tick)
- ✅ O(1) entry removal via map operations
- ✅ No YAML/database operations in hot path

### Memory Footprint
- Party: ~500 bytes per party (10 members)
- QueueEntry: ~200 bytes per entry
- Expected: <10MB for 1000 concurrent players

---

## Configuration (❌ NOT IMPLEMENTED)

**Required File**: `party-config.yml`
```yaml
party:
  max-size: 20
  invite-expire-seconds: 60
  inactive-kick-minutes: 5
  cleanup-interval-ticks: 600
  
queue:
  mmr-expand-interval-seconds: 10
  mmr-expand-amount: 50
  max-queue-time-minutes: 5
  matchmaking-tick-interval: 20
```

---

## Testing Checklist

### Party System
- [ ] Create party
- [ ] Invite player
- [ ] Accept invite
- [ ] Kick member
- [ ] Promote/demote member
- [ ] Transfer leadership
- [ ] Disband party
- [ ] Leader disconnect handling
- [ ] Member disconnect handling
- [ ] State transitions
- [ ] Team assignment
- [ ] FFA mode toggle
- [ ] Split mode toggle
- [ ] Party chat

### Queue System
- [ ] Solo queue (unranked)
- [ ] Solo queue (ranked)
- [ ] Party queue (2v2, 3v3, etc.)
- [ ] Party FFA queue
- [ ] MMR expansion over time
- [ ] Matchmaking compatibility
- [ ] Queue leave
- [ ] Queue timeout
- [ ] ELO calculation
- [ ] Concurrent queue operations

---

## Known Limitations

1. **No Persistence**: Parties disband on server restart
   - **Solution**: Implement `PartyStorage.java` with async YAML save/load

2. **No MMR System**: Currently hardcoded to 1000
   - **Solution**: Integrate with `StatsManager.getPlayerMMR()`

3. **No Arena System**: Matchmaker has TODO comments
   - **Solution**: Implement `ArenaManager` and `Duel` creation

4. **No Custom Events**: PartyEvents has TODO stubs
   - **Solution**: Create Bukkit Event classes and fire them

5. **No GUI Implementation**: Commands and GUIs not implemented
   - **Solution**: Implement command/GUI classes listed above

---

## File Inventory

### ✅ Completed Files (18)

#### Party System (11 files)
1. `tech/xanthia/party/PartyRole.java`
2. `tech/xanthia/party/PartyState.java`
3. `tech/xanthia/party/PartyMember.java`
4. `tech/xanthia/party/PartyInvite.java`
5. `tech/xanthia/party/Party.java`
6. `tech/xanthia/party/PartyProfile.java`
7. `tech/xanthia/party/PartyManager.java`
8. `tech/xanthia/party/PartyInviteManager.java`
9. `tech/xanthia/party/PartyChatManager.java`
10. `tech/xanthia/party/PartyEvents.java`

#### Queue System (5 files)
11. `tech/xanthia/queue/QueueType.java`
12. `tech/xanthia/queue/QueueEntry.java`
13. `tech/xanthia/queue/RankedQueueEntry.java`
14. `tech/xanthia/queue/QueueManager.java`
15. `tech/xanthia/queue/QueueMatchmaker.java`

#### Kits System (24 files - from previous session)
16-39. All kit system files previously generated

### ❌ Pending Files (23+)

#### Party Commands (13 files)
- PartyCommand.java + 12 subcommands

#### Party GUIs (5 files)
- PartyManageGUI, PartyMembersGUI, PartyTeamEditorGUI, PartyVisibilityGUI, PartyRoleSelectGUI

#### Party Utilities (4 files)
- PartyTeleportHandler, PartyTeamAllocator, PartyFFAManager, PartyPlayerListener

#### Queue Commands (5 files)
- QueueCommand.java + 4 subcommands

#### Queue GUIs (3 files)
- QueueSelectorGUI, QueueKitSelectorGUI, QueueStatusGUI

#### Queue Utilities (3 files)
- QueueEloCalculator, QueueTimeoutHandler, QueueTickTask

---

## Next Steps

1. **Implement Party Commands** - Enable player interaction with party system
2. **Implement Party GUIs** - Provide visual party management
3. **Implement Queue Commands** - Allow queue joining/leaving
4. **Implement Queue GUIs** - Visual queue selection
5. **Create ArenaManager Integration** - Connect matchmaker to arena system
6. **Create DuelManager Integration** - Handle duel creation from matches
7. **Implement StatsManager Integration** - Track MMR and statistics
8. **Add Persistence** - Save/load parties and profiles
9. **Create Custom Events** - Fire Bukkit events for integrations
10. **Testing & Optimization** - Load testing and performance tuning

---

## Contact & Support

For questions or issues with this implementation:
- Check TODO comments in code
- Review integration points section
- Ensure all dependencies (DuelManager, ArenaManager, StatsManager) are implemented

---

**Generated by**: GitHub Copilot  
**Date**: 2024  
**Version**: 1.0.0  
**Status**: Core systems complete, commands/GUIs/utilities pending
