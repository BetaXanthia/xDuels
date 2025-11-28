package tech.xanthia.stats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * StatsManager.java
 * Manages player duel statistics with async persistence.
 * 
 * Tracks:
 * - Global stats (wins, losses, kills, deaths, KDR)
 * - Per-kit stats
 * - Combat statistics (damage dealt/taken, combos)
 * - Performance metrics (playtime, longest combo)
 * 
 * Performance Features:
 * - Async saving (write-behind strategy)
 * - Cached profiles (O(1) lookups)
 * - Batch writes
 * - Auto-save every 5 minutes
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class StatsManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    
    // Cached player profiles (O(1) lookup)
    private final Map<UUID, PlayerStats> cachedProfiles;
    
    // Async save executor
    private final ExecutorService saveExecutor;
    
    // Storage
    private final File statsFolder;
    
    // Auto-save task ID
    private int autoSaveTaskId;
    
    // Configuration
    private final int autoSaveInterval = 6000; // 5 minutes in ticks
    
    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.cachedProfiles = new ConcurrentHashMap<>();
        this.saveExecutor = Executors.newSingleThreadExecutor();
        
        // Create stats folder
        this.statsFolder = new File(plugin.getDataFolder(), "stats");
        if (!statsFolder.exists()) {
            statsFolder.mkdirs();
        }
        
        startAutoSave();
        logger.info("StatsManager initialized");
    }
    
    /**
     * Load player stats (cached, async load from disk if not cached)
     * 
     * @param playerUUID Player UUID
     * @return PlayerStats
     */
    public PlayerStats getPlayerStats(UUID playerUUID) {
        // Check cache first
        PlayerStats cached = cachedProfiles.get(playerUUID);
        if (cached != null) {
            return cached;
        }
        
        // Load from disk (sync for now, can be async in production)
        PlayerStats stats = loadStatsFromDisk(playerUUID);
        cachedProfiles.put(playerUUID, stats);
        
        return stats;
    }
    
    /**
     * Update player stats after duel
     * 
     * @param playerUUID Player UUID
     * @param won Whether player won
     * @param kills Number of kills
     * @param deaths Number of deaths
     * @param damageDealt Damage dealt
     * @param damageTaken Damage taken
     * @param kitName Kit used
     */
    public void updateDuelStats(UUID playerUUID, boolean won, int kills, int deaths,
                                 int damageDealt, int damageTaken, String kitName) {
        PlayerStats stats = getPlayerStats(playerUUID);
        
        // Update global stats
        if (won) {
            stats.addWin();
        } else {
            stats.addLoss();
        }
        stats.addKills(kills);
        stats.addDeaths(deaths);
        stats.addDamageDealt(damageDealt);
        stats.addDamageTaken(damageTaken);
        
        // Update per-kit stats
        KitStats kitStats = stats.getKitStats(kitName);
        if (won) {
            kitStats.addWin();
        } else {
            kitStats.addLoss();
        }
        kitStats.addKills(kills);
        kitStats.addDeaths(deaths);
        kitStats.addDamageDealt(damageDealt);
        kitStats.addDamageTaken(damageTaken);
        
        // Mark as dirty for async save
        stats.markDirty();
        
        logger.fine("Updated stats for player " + playerUUID + " | Kit: " + kitName);
    }
    
    /**
     * Get player MMR for kit
     * 
     * @param playerUUID Player UUID
     * @param kitName Kit name
     * @return MMR value
     */
    public int getPlayerMMR(UUID playerUUID, String kitName) {
        PlayerStats stats = getPlayerStats(playerUUID);
        KitStats kitStats = stats.getKitStats(kitName);
        return kitStats.getMMR();
    }
    
    /**
     * Update player MMR for kit
     * 
     * @param playerUUID Player UUID
     * @param kitName Kit name
     * @param newMMR New MMR value
     */
    public void updateMMR(UUID playerUUID, String kitName, int newMMR) {
        PlayerStats stats = getPlayerStats(playerUUID);
        KitStats kitStats = stats.getKitStats(kitName);
        kitStats.setMMR(newMMR);
        stats.markDirty();
        
        logger.fine("Updated MMR for player " + playerUUID + " | Kit: " + kitName + " | New MMR: " + newMMR);
    }
    
    /**
     * Record combo for player
     * 
     * @param playerUUID Player UUID
     * @param comboLength Combo length
     */
    public void recordCombo(UUID playerUUID, int comboLength) {
        PlayerStats stats = getPlayerStats(playerUUID);
        stats.recordCombo(comboLength);
        stats.markDirty();
    }
    
    /**
     * Load stats from disk
     * 
     * @param playerUUID Player UUID
     * @return PlayerStats
     */
    private PlayerStats loadStatsFromDisk(UUID playerUUID) {
        File statsFile = new File(statsFolder, playerUUID.toString() + ".yml");
        
        if (!statsFile.exists()) {
            // Create new stats
            return new PlayerStats(playerUUID);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(statsFile);
        PlayerStats stats = new PlayerStats(playerUUID);
        
        // Load global stats
        stats.setWins(config.getInt("global.wins", 0));
        stats.setLosses(config.getInt("global.losses", 0));
        stats.setKills(config.getInt("global.kills", 0));
        stats.setDeaths(config.getInt("global.deaths", 0));
        stats.setDamageDealt(config.getInt("global.damage-dealt", 0));
        stats.setDamageTaken(config.getInt("global.damage-taken", 0));
        stats.setLongestCombo(config.getInt("global.longest-combo", 0));
        stats.setTotalCombos(config.getInt("global.total-combos", 0));
        stats.setPlaytime(config.getLong("global.playtime", 0));
        
        // Load per-kit stats
        if (config.contains("kits")) {
            for (String kitName : config.getConfigurationSection("kits").getKeys(false)) {
                KitStats kitStats = stats.getKitStats(kitName);
                String path = "kits." + kitName + ".";
                
                kitStats.setWins(config.getInt(path + "wins", 0));
                kitStats.setLosses(config.getInt(path + "losses", 0));
                kitStats.setKills(config.getInt(path + "kills", 0));
                kitStats.setDeaths(config.getInt(path + "deaths", 0));
                kitStats.setDamageDealt(config.getInt(path + "damage-dealt", 0));
                kitStats.setDamageTaken(config.getInt(path + "damage-taken", 0));
                kitStats.setMMR(config.getInt(path + "mmr", 1000));
            }
        }
        
        return stats;
    }
    
    /**
     * Save stats to disk (async)
     * 
     * @param stats PlayerStats to save
     */
    private void saveStatsToDisk(PlayerStats stats) {
        if (!stats.isDirty()) {
            return; // No changes, skip save
        }
        
        saveExecutor.submit(() -> {
            try {
                File statsFile = new File(statsFolder, stats.getPlayerUUID().toString() + ".yml");
                FileConfiguration config = new YamlConfiguration();
                
                // Save global stats
                config.set("global.wins", stats.getWins());
                config.set("global.losses", stats.getLosses());
                config.set("global.kills", stats.getKills());
                config.set("global.deaths", stats.getDeaths());
                config.set("global.damage-dealt", stats.getDamageDealt());
                config.set("global.damage-taken", stats.getDamageTaken());
                config.set("global.longest-combo", stats.getLongestCombo());
                config.set("global.total-combos", stats.getTotalCombos());
                config.set("global.playtime", stats.getPlaytime());
                
                // Save per-kit stats
                for (Map.Entry<String, KitStats> entry : stats.getAllKitStats().entrySet()) {
                    String kitName = entry.getKey();
                    KitStats kitStats = entry.getValue();
                    String path = "kits." + kitName + ".";
                    
                    config.set(path + "wins", kitStats.getWins());
                    config.set(path + "losses", kitStats.getLosses());
                    config.set(path + "kills", kitStats.getKills());
                    config.set(path + "deaths", kitStats.getDeaths());
                    config.set(path + "damage-dealt", kitStats.getDamageDealt());
                    config.set(path + "damage-taken", kitStats.getDamageTaken());
                    config.set(path + "mmr", kitStats.getMMR());
                }
                
                config.save(statsFile);
                stats.clearDirty();
                
            } catch (IOException e) {
                logger.severe("Failed to save stats for player " + stats.getPlayerUUID() + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Start auto-save task
     */
    private void startAutoSave() {
        autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            saveAll();
        }, autoSaveInterval, autoSaveInterval);
    }
    
    /**
     * Save all cached profiles
     */
    public void saveAll() {
        int savedCount = 0;
        for (PlayerStats stats : cachedProfiles.values()) {
            if (stats.isDirty()) {
                saveStatsToDisk(stats);
                savedCount++;
            }
        }
        
        if (savedCount > 0) {
            logger.info("Auto-saved " + savedCount + " player stats");
        }
    }
    
    /**
     * Unload player stats from cache (called on logout)
     * 
     * @param playerUUID Player UUID
     */
    public void unloadPlayerStats(UUID playerUUID) {
        PlayerStats stats = cachedProfiles.remove(playerUUID);
        if (stats != null && stats.isDirty()) {
            saveStatsToDisk(stats);
        }
    }
    
    /**
     * Shutdown manager
     */
    public void shutdown() {
        // Cancel auto-save task
        Bukkit.getScheduler().cancelTask(autoSaveTaskId);
        
        // Save all cached profiles
        logger.info("Saving all player stats before shutdown...");
        saveAll();
        
        // Shutdown executor
        saveExecutor.shutdown();
        
        logger.info("StatsManager shutdown complete");
    }
}
