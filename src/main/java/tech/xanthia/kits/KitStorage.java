package tech.xanthia.kits;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tech.xanthia.XDuels;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Handles YAML persistence for kits.
 * 
 * All kits are stored in kits.yml with Base64-encoded inventories.
 * Load operations are synchronous, save operations are asynchronous.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitStorage {
    
    private final XDuels plugin;
    private final File kitsFile;
    private FileConfiguration kitsConfig;
    
    /**
     * Creates a new kit storage handler.
     * 
     * @param plugin Plugin instance
     */
    public KitStorage(XDuels plugin) {
        this.plugin = plugin;
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        
        // Create file if it doesn't exist
        if (!kitsFile.exists()) {
            try {
                kitsFile.getParentFile().mkdirs();
                kitsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[ KitStorage ] Failed to create kits.yml" + " - Error: " + e.getMessage());
            }
        }
        
        this.kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }
    
    /**
     * Loads all kits from kits.yml.
     * 
     * This operation is synchronous and should be called during plugin startup.
     * 
     * @return Map of kit name to Kit object
     */
    public Map<String, Kit> loadAllKits() {
        Map<String, Kit> kits = new HashMap<>();
        
        // Reload config from disk
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        
        if (!kitsConfig.contains("kits")) {
            plugin.getLogger().info("[ KitStorage ] No kits found in kits.yml");
            return kits;
        }
        
        for (String kitName : kitsConfig.getConfigurationSection("kits").getKeys(false)) {
            try {
                Kit kit = Kit.deserialize(kitName, kitsConfig.getConfigurationSection("kits." + kitName));
                kits.put(kitName, kit);
                plugin.getLogger().info("[ KitStorage ] Loaded kit: " + kitName);
            } catch (Exception e) {
                plugin.getLogger().severe("[ KitStorage ] Failed to load kit: " + kitName + " - Error: " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("[ KitStorage ] Loaded " + kits.size() + " kit(s)");
        return kits;
    }
    
    /**
     * Saves a single kit to kits.yml asynchronously.
     * 
     * @param kit Kit to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveKit(Kit kit) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Reload config to avoid overwriting other changes
                kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
                
                // Serialize kit
                kit.serialize(kitsConfig.createSection("kits." + kit.getName()));
                
                // Save to disk
                kitsConfig.save(kitsFile);
                
                plugin.getLogger().info("[ KitStorage ] Saved kit: " + kit.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("[ KitStorage ] Failed to save kit: " + kit.getName() + " - Error: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }
    
    /**
     * Saves a single kit synchronously.
     * 
     * Use this only when necessary (e.g., during plugin shutdown).
     * 
     * @param kit Kit to save
     */
    public void saveKitSync(Kit kit) {
        try {
            // Reload config to avoid overwriting other changes
            kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
            
            // Serialize kit
            kit.serialize(kitsConfig.createSection("kits." + kit.getName()));
            
            // Save to disk
            kitsConfig.save(kitsFile);
            
            plugin.getLogger().info("[ KitStorage ] Saved kit (sync): " + kit.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("[ KitStorage ] Failed to save kit (sync): " + kit.getName() + " - Error: " + e.getMessage());
        }
    }
    
    /**
     * Saves all kits to kits.yml asynchronously.
     * 
     * @param kits Map of kits to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> saveAllKits(Map<String, Kit> kits) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Create fresh config
                kitsConfig = new YamlConfiguration();
                
                // Serialize all kits
                for (Kit kit : kits.values()) {
                    kit.serialize(kitsConfig.createSection("kits." + kit.getName()));
                }
                
                // Save to disk
                kitsConfig.save(kitsFile);
                
                plugin.getLogger().info("[ KitStorage ] Saved " + kits.size() + " kit(s)");
            } catch (Exception e) {
                plugin.getLogger().severe("[ KitStorage ] Failed to save all kits" + " - Error: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }
    
    /**
     * Saves all kits synchronously.
     * 
     * Use this during plugin shutdown to ensure all data is persisted.
     * 
     * @param kits Map of kits to save
     */
    public void saveAllKitsSync(Map<String, Kit> kits) {
        try {
            // Create fresh config
            kitsConfig = new YamlConfiguration();
            
            // Serialize all kits
            for (Kit kit : kits.values()) {
                kit.serialize(kitsConfig.createSection("kits." + kit.getName()));
            }
            
            // Save to disk
            kitsConfig.save(kitsFile);
            
            plugin.getLogger().info("[ KitStorage ] Saved " + kits.size() + " kit(s) (sync)");
        } catch (Exception e) {
            plugin.getLogger().severe("[ KitStorage ] Failed to save all kits (sync)" + " - Error: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a kit from kits.yml asynchronously.
     * 
     * @param kitName Kit name to delete
     * @return CompletableFuture that completes when delete is done
     */
    public CompletableFuture<Void> deleteKit(String kitName) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Reload config
                kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
                
                // Remove kit section
                kitsConfig.set("kits." + kitName, null);
                
                // Save to disk
                kitsConfig.save(kitsFile);
                
                plugin.getLogger().info("[ KitStorage ] Deleted kit: " + kitName);
            } catch (Exception e) {
                plugin.getLogger().severe("[ KitStorage ] Failed to delete kit: " + kitName + " - Error: " + e.getMessage());
            }
        }, plugin.getAsyncExecutor());
    }
    
    /**
     * Checks if a kit exists in kits.yml.
     * 
     * @param kitName Kit name to check
     * @return True if kit exists
     */
    public boolean kitExists(String kitName) {
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        return kitsConfig.contains("kits." + kitName);
    }
}
