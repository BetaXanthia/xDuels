# xDuels - Quick Start Guide

## Installation (5 Minutes)

### Step 1: Copy JAR File
```bash
# Copy the built JAR to your server's plugins folder
cp target/xDuels-1.0.0-SNAPSHOT.jar /path/to/server/plugins/
```

### Step 2: Start Server
Start your Minecraft Paper server. The plugin will create default config files.

### Step 3: Configure Lobby Spawn
Edit `plugins/xDuels/config.yml`:
```yaml
duel:
  lobby:
    world: world
    x: 0.5
    y: 64.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0
```

### Step 4: Create Your First Arena
```
/arena create nodebuff-1
/arena editor nodebuff-1
# Right-click to set Team 1 spawn
# Left-click to set Team 2 spawn
# Type "done" in chat when finished
```

### Step 5: Create Your First Kit
```
/kit create nodebuff
# Equip yourself with the items you want in the kit
/kit edit nodebuff
# Click "Save Inventory" in the GUI
```

### Step 6: Test a Duel!
```
/duel <player_name> nodebuff
```

## Essential Commands

### Player Commands
| Command | Description | Example |
|---------|-------------|---------|
| `/duel <player> <kit>` | Challenge someone | `/duel Steve nodebuff` |
| `/stats [player]` | View statistics | `/stats` or `/stats Steve` |
| `/spectate <player>` | Watch a duel | `/spectate Steve` |
| `/party create` | Create party | `/party create` |
| `/party invite <player>` | Invite to party | `/party invite Alex` |
| `/queue join <kit>` | Join ranked queue | `/queue join nodebuff` |
| `/queue leave` | Leave queue | `/queue leave` |

### Admin Commands
| Command | Description |
|---------|-------------|
| `/arena create <name>` | Create new arena |
| `/arena editor <name>` | Edit arena spawns |
| `/arena list` | List all arenas |
| `/arena enable <name>` | Enable arena |
| `/arena disable <name>` | Disable arena |
| `/kit create <name>` | Create new kit |
| `/kit edit <name>` | Edit kit contents |
| `/kit delete <name>` | Delete a kit |
| `/kit list` | List all kits |

## Common Use Cases

### 1. Setting Up a 1v1 Duel
```
Player1: /duel Player2 nodebuff
Player2: (accepts via chat)
# Duel starts automatically
```

### 2. Creating a Party for 2v2
```
Leader: /party create
Leader: /party invite Player2
Leader: /party invite Player3  
Leader: /party invite Player4
Leader: /party split (enable 2v2 mode)
Leader: /queue join nodebuff
# Matchmaking finds another 2v2 party
```

### 3. Spectating a Duel
```
/spectate Player1
# You become invisible and can fly around
# Type /spectate again to stop
``` 

### 4. Viewing Statistics
```
/stats
# Shows your wins, losses, KDR, ELO rating, etc.

/stats Player1
# Shows another player's stats
```

## Configuration Quick Reference

### Important Config Options
```yaml
# config.yml

# Max party size
party:
  max-size: 10

# ELO settings
duel:
  elo:
    starting-rating: 1000
    k-factor-beginner: 40

# Queue matchmaking
queue:
  matchmaking-interval-ticks: 40  # 2 seconds
  elo-range-base: 100
  
# Spectator limits
duel:
  spectator:
    max-per-duel: 20
```

## Troubleshooting Quick Fixes

### "No arenas available"
```
/arena list
# Check if arenas exist and are enabled
/arena enable <arena-name>
```

### "Kit not found"
```
/kit list
# Verify kit exists with correct name
# Create it if missing: /kit create <name>
```

### Stats not saving
```
# Check folder permissions
ls -la plugins/xDuels/stats/
# Should show read/write permissions
```

### Plugin not loading
```
# Check Paper version (must be 1.20.2+)
# Check Java version (must be 17+)
# Check console for errors
```

## Performance Tips

1. **Limit concurrent duels** to 10-20 for best performance
2. **Enable arena regeneration** only if needed
3. **Set auto-save interval** to 6000 ticks (5 min) or higher
4. **Use solid arenas** (no complex redstone) for better TPS

## Advanced Features

### Special Game Modes
- **Boxing**: First to 100 hits wins
  ```
  # Create boxing kit with fists only
  /kit create boxing
  # In kit GUI, set mode to "boxing"
  ```

- **Sumo**: Push opponent off platform
  ```
  # Create sumo arena with void/water below
  # Create sumo kit with knockback stick
  /kit create sumo
  ```

- **Spleef**: Break floor beneath opponent
  ```
  # Create spleef arena with breakable floor
  /kit create spleef
  ```

### Party Modes
- **Split Mode**: Divides party into two balanced teams
  ```
  /party split
  ```

- **FFA Mode**: Everyone fights everyone
  ```
  /party ffa
  ```

### ELO Rating Tiers
- **Beginner**: 0-1200 (K-factor: 40)
- **Intermediate**: 1200-1600 (K-factor: 32)
- **Advanced**: 1600-2000 (K-factor: 24)
- **Master**: 2000+ (K-factor: 16)

## File Locations

```
plugins/xDuels/
├── config.yml           # Main configuration
├── arenas/             # Arena data files
├── kits/               # Kit data files
└── stats/              # Player statistics
    └── players/        # Individual player files
```

## Permissions Quick Reference

### For Players
- `xduels.duel` - Use /duel command (default: true)
- `xduels.party` - Use /party commands (default: true)
- `xduels.spectate` - Spectate duels (default: true)
- `xduels.stats` - View stats (default: true)

### For Admins
- `xduels.admin` - All admin commands (default: op)
- `xduels.*` - All permissions (default: op)

## Getting Help

### In-Game
```
/xduels help      # Show help menu
/arena help       # Arena command help
/kit help         # Kit command help
/party help       # Party command help
```

### Common Issues
1. **Can't create arena**: Need `xduels.admin` permission
2. **Can't duel**: Check if arena and kit exist
3. **No matchmaking**: Need more players in queue
4. **Stats reset**: Check if stats folder has backups

---

**Ready to duel? Start with `/arena create` and `/kit create`!** 🎮

For detailed documentation, see `BUILD_COMPLETE.md`
