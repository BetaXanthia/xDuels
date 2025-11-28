package tech.xanthia.arena;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import tech.xanthia.XDuels;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Handles persistent storage for arenas and arena groups.
 * 
 * Manages YAML file I/O operations for:
 * - arenas.yml: Individual arena configurations
 * - groups.yml: Arena group configurations (optional, can be in arenas.yml)
 * 
 * All file I/O operations are performed asynchronously to prevent main thread blocking.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaStorage {
    
    private final XDuels plugin;
    private final File arenasFile;
    private final File groupsFile;
    private YamlConfiguration arenasConfig;
    private YamlConfiguration groupsConfig;
    
    /**
     * Creates a new ArenaStorage instance.
     * 
     * @param plugin Plugin instance
     */
    public ArenaStorage(XDuels plugin) {
        this.plugin = plugin;
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
    }
    
    // ========================================
    // INITIALIZATION
    // ========================================
    
    /**
     * Initializes storage by creating files if they don't exist
     * and loading configurations into memory.
     */
    public void initialize() {
        createFilesIfMissing();
        loadConfigurations();
    }
    
    /**
     * Creates storage files if they don't exist.
     */
    private void createFilesIfMissing() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            if (!arenasFile.exists()) {
                arenasFile.createNewFile();
                plugin.getLogger().info("[ ArenaStorage ] Created arenas.yml");
            }
            
            if (!groupsFile.exists()) {
                groupsFile.createNewFile();
                plugin.getLogger().info("[ ArenaStorage ] Created groups.yml");
            }
            
        } catch (IOException e) {
            plugin.getLogger().severe("[ ArenaStorage ] Failed to create storage files: " + e.getMessage());
        }
    }
    
    /**
     * Loads YAML configurations into memory.
     */
    private void loadConfigurations() {
        this.arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        this.groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
    }
    
    // ========================================
    // ARENA OPERATIONS (SYNC)
    // ========================================
    
    /**
     * Loads all arenas from storage synchronously.
     * Should be called during plugin startup.
     * 
     * @return Map of arena ID to Arena object
     */
    public Map<String, Arena> loadAllArenas() {
        Map<String, Arena> arenas = new HashMap<>();
        
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            plugin.getLogger().info("[ ArenaStorage ] No arenas found in storage");
            return arenas;
        }
        
        for (String arenaId : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaId);
            Arena arena = Arena.deserialize(arenaId, arenaSection);
            
            if (arena != null) {
                arenas.put(arenaId, arena);
            } else {
                plugin.getLogger().warning("[ ArenaStorage ] Failed to load arena: " + arenaId);
            }
        }
        
        plugin.getLogger().info("[ ArenaStorage ] Loaded " + arenas.size() + " arenas");
        return arenas;
    }
    
    /**
     * Saves a single arena synchronously.
     * 
     * @param arena Arena to save
     */
    public void saveArena(Arena arena) {
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            arenasSection = arenasConfig.createSection("arenas");
        }
        
        ConfigurationSection arenaSection = arenasSection.createSection(arena.getId());
        arena.serialize(arenaSection);
        
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[ ArenaStorage ] Failed to save arena " + arena.getId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Saves all arenas synchronously.
     * 
     * @param arenas Map of arenas to save
     */
    public void saveAllArenas(Map<String, Arena> arenas) {
        // Clear existing arenas section
        arenasConfig.set("arenas", null);
        ConfigurationSection arenasSection = arenasConfig.createSection("arenas");
        
        // Serialize all arenas
        for (Arena arena : arenas.values()) {
            ConfigurationSection arenaSection = arenasSection.createSection(arena.getId());
            arena.serialize(arenaSection);
        }
        
        try {
            arenasConfig.save(arenasFile);
            plugin.getLogger().info("[ ArenaStorage ] Saved " + arenas.size() + " arenas");
        } catch (IOException e) {
            plugin.getLogger().severe("[ ArenaStorage ] Failed to save arenas: " + e.getMessage());
        }
    }
    
    /**
     * Deletes an arena from storage synchronously.
     * 
     * @param arenaId Arena ID to delete
     */
    public void deleteArena(String arenaId) {
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection != null) {
            arenasSection.set(arenaId, null);
            
            try {
                arenasConfig.save(arenasFile);
                plugin.getLogger().info("[ ArenaStorage ] Deleted arena: " + arenaId);
            } catch (IOException e) {
                plugin.getLogger().severe("[ ArenaStorage ] Failed to delete arena " + arenaId + ": " + e.getMessage());
            }
        }
    }
    
    // ========================================
    // ARENA OPERATIONS (ASYNC)
    // ========================================
    
    /**
     * Saves a single arena asynchronously.
     * 
     * @param arena Arena to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveArenaAsync(Arena arena) {
        return CompletableFuture.runAsync(() -> saveArena(arena), plugin.getAsyncExecutor());
    }
    
    /**
     * Saves all arenas asynchronously.
     * 
     * @param arenas Map of arenas to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveAllArenasAsync(Map<String, Arena> arenas) {
        return CompletableFuture.runAsync(() -> saveAllArenas(arenas), plugin.getAsyncExecutor());
    }
    
    /**
     * Deletes an arena asynchronously.
     * 
     * @param arenaId Arena ID to delete
     * @return CompletableFuture that completes when deletion is done
     */
    public CompletableFuture<Void> deleteArenaAsync(String arenaId) {
        return CompletableFuture.runAsync(() -> deleteArena(arenaId), plugin.getAsyncExecutor());
    }
    
    // ========================================
    // GROUP OPERATIONS (SYNC)
    // ========================================
    
    /**
     * Loads all arena groups from storage synchronously.
     * 
     * @return Map of group ID to ArenaGroup object
     */
    public Map<String, ArenaGroup> loadAllGroups() {
        Map<String, ArenaGroup> groups = new HashMap<>();
        
        ConfigurationSection groupsSection = groupsConfig.getConfigurationSection("groups");
        if (groupsSection == null) {
            plugin.getLogger().info("[ ArenaStorage ] No arena groups found in storage");
            return groups;
        }
        
        for (String groupId : groupsSection.getKeys(false)) {
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupId);
            ArenaGroup group = ArenaGroup.deserialize(groupId, groupSection);
            
            if (group != null) {
                groups.put(groupId, group);
            } else {
                plugin.getLogger().warning("[ ArenaStorage ] Failed to load group: " + groupId);
            }
        }
        
        plugin.getLogger().info("[ ArenaStorage ] Loaded " + groups.size() + " arena groups");
        return groups;
    }
    
    /**
     * Saves a single group synchronously.
     * 
     * @param group Group to save
     */
    public void saveGroup(ArenaGroup group) {
        ConfigurationSection groupsSection = groupsConfig.getConfigurationSection("groups");
        if (groupsSection == null) {
            groupsSection = groupsConfig.createSection("groups");
        }
        
        ConfigurationSection groupSection = groupsSection.createSection(group.getGroupId());
        group.serialize(groupSection);
        
        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[ ArenaStorage ] Failed to save group " + group.getGroupId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Saves all groups synchronously.
     * 
     * @param groups Map of groups to save
     */
    public void saveAllGroups(Map<String, ArenaGroup> groups) {
        // Clear existing groups section
        groupsConfig.set("groups", null);
        ConfigurationSection groupsSection = groupsConfig.createSection("groups");
        
        // Serialize all groups
        for (ArenaGroup group : groups.values()) {
            ConfigurationSection groupSection = groupsSection.createSection(group.getGroupId());
            group.serialize(groupSection);
        }
        
        try {
            groupsConfig.save(groupsFile);
            plugin.getLogger().info("[ ArenaStorage ] Saved " + groups.size() + " groups");
        } catch (IOException e) {
            plugin.getLogger().severe("[ ArenaStorage ] Failed to save groups: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a group from storage synchronously.
     * 
     * @param groupId Group ID to delete
     */
    public void deleteGroup(String groupId) {
        ConfigurationSection groupsSection = groupsConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            groupsSection.set(groupId, null);
            
            try {
                groupsConfig.save(groupsFile);
                plugin.getLogger().info("[ ArenaStorage ] Deleted group: " + groupId);
            } catch (IOException e) {
                plugin.getLogger().severe("[ ArenaStorage ] Failed to delete group " + groupId + ": " + e.getMessage());
            }
        }
    }
    
    // ========================================
    // GROUP OPERATIONS (ASYNC)
    // ========================================
    
    /**
     * Saves a single group asynchronously.
     * 
     * @param group Group to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveGroupAsync(ArenaGroup group) {
        return CompletableFuture.runAsync(() -> saveGroup(group), plugin.getAsyncExecutor());
    }
    
    /**
     * Saves all groups asynchronously.
     * 
     * @param groups Map of groups to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveAllGroupsAsync(Map<String, ArenaGroup> groups) {
        return CompletableFuture.runAsync(() -> saveAllGroups(groups), plugin.getAsyncExecutor());
    }
    
    /**
     * Deletes a group asynchronously.
     * 
     * @param groupId Group ID to delete
     * @return CompletableFuture that completes when deletion is done
     */
    public CompletableFuture<Void> deleteGroupAsync(String groupId) {
        return CompletableFuture.runAsync(() -> deleteGroup(groupId), plugin.getAsyncExecutor());
    }
    
    // ========================================
    // RELOAD
    // ========================================
    
    /**
     * Reloads all configurations from disk.
     */
    public void reload() {
        loadConfigurations();
        plugin.getLogger().info("[ ArenaStorage ] Configurations reloaded");
    }
}
