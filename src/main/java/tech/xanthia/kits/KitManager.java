package tech.xanthia.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tech.xanthia.XDuels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all kit operations.
 * 
 * Handles:
 * - Kit loading and caching
 * - CRUD operations
 * - Kit application to players
 * - Validation
 * - Runtime profile generation
 * 
 * All kits are cached in RAM for maximum performance.
 * Kit application is always synchronous.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitManager {
    
    private final XDuels plugin;
    private final KitStorage storage;
    private final KitValidator validator;
    
    // All kits cached in RAM
    private final Map<String, Kit> kits;
    
    /**
     * Creates a new kit manager.
     * 
     * @param plugin Plugin instance
     */
    public KitManager(XDuels plugin) {
        this.plugin = plugin;
        this.storage = new KitStorage(plugin);
        this.validator = new KitValidator(plugin);
        this.kits = new ConcurrentHashMap<>();
    }
    
    /**
     * Initializes the kit manager by loading all kits from storage.
     * 
     * This should be called during plugin startup.
     */
    public void initialize() {
        plugin.getLogger().info("[ KitManager ] Initializing...");
        
        // Load all kits from storage
        Map<String, Kit> loadedKits = storage.loadAllKits();
        
        // Validate each kit
        for (Kit kit : loadedKits.values()) {
            List<String> errors = validator.validate(kit);
            if (!errors.isEmpty()) {
                plugin.getLogger().warning("[ KitManager ] Kit '" + kit.getName() + 
                    "' has validation errors: " + String.join(", ", errors));
                kit.setEnabled(false); // Disable invalid kits
            }
        }
        
        kits.putAll(loadedKits);
        
        plugin.getLogger().info("[ KitManager ] Initialized with " + kits.size() + " kit(s)");
    }
    
    /**
     * Shuts down the kit manager and saves all kits.
     */
    public void shutdown() {
        plugin.getLogger().info("[ KitManager ] Shutting down...");
        
        // Save all kits synchronously
        storage.saveAllKitsSync(kits);
        
        plugin.getLogger().info("[ KitManager ] Shutdown complete");
    }
    
    /**
     * Gets a kit by name.
     * 
     * @param name Kit name
     * @return Kit or null if not found
     */
    public Kit getKit(String name) {
        return kits.get(name);
    }
    
    /**
     * Gets all kits.
     * 
     * @return Unmodifiable collection of all kits
     */
    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }
    
    /**
     * Gets all enabled kits.
     * 
     * @return List of enabled kits
     */
    public List<Kit> getEnabledKits() {
        List<Kit> enabled = new ArrayList<>();
        for (Kit kit : kits.values()) {
            if (kit.isEnabled()) {
                enabled.add(kit);
            }
        }
        return enabled;
    }
    
    /**
     * Gets all kits available for unranked queue.
     * 
     * @return List of unranked-enabled kits
     */
    public List<Kit> getUnrankedKits() {
        List<Kit> unranked = new ArrayList<>();
        for (Kit kit : kits.values()) {
            if (kit.isEnabled() && kit.isAllowInQueueUnranked()) {
                unranked.add(kit);
            }
        }
        return unranked;
    }
    
    /**
     * Gets all kits available for ranked queue.
     * 
     * @return List of ranked-enabled kits
     */
    public List<Kit> getRankedKits() {
        List<Kit> ranked = new ArrayList<>();
        for (Kit kit : kits.values()) {
            if (kit.isEnabled() && kit.isAllowInQueueRanked()) {
                ranked.add(kit);
            }
        }
        return ranked;
    }
    
    /**
     * Checks if a kit exists.
     * 
     * @param name Kit name
     * @return True if kit exists
     */
    public boolean kitExists(String name) {
        return kits.containsKey(name);
    }
    
    /**
     * Creates a new kit.
     * 
     * @param name Kit name (must be unique)
     * @return Created kit or null if name already exists
     */
    public Kit createKit(String name) {
        if (kitExists(name)) {
            return null;
        }
        
        Kit kit = new Kit(name);
        kits.put(name, kit);
        
        // Save asynchronously
        storage.saveKit(kit);
        
        plugin.getLogger().info("[ KitManager ] Created kit: " + name);
        return kit;
    }
    
    /**
     * Deletes a kit.
     * 
     * @param name Kit name
     * @return True if deleted successfully
     */
    public boolean deleteKit(String name) {
        Kit kit = kits.remove(name);
        if (kit == null) {
            return false;
        }
        
        // Delete from storage asynchronously
        storage.deleteKit(name);
        
        plugin.getLogger().info("[ KitManager ] Deleted kit: " + name);
        return true;
    }
    
    /**
     * Saves a kit to storage.
     * 
     * @param kit Kit to save
     */
    public void saveKit(Kit kit) {
        storage.saveKit(kit);
    }
    
    /**
     * Reloads all kits from storage.
     */
    public void reload() {
        plugin.getLogger().info("[ KitManager ] Reloading kits...");
        
        kits.clear();
        initialize();
    }
    
    /**
     * Applies a kit to a player.
     * 
     * This method MUST be called on the main thread.
     * 
     * @param player Target player
     * @param kit Kit to apply
     */
    public void applyKit(Player player, Kit kit) {
        // Clear existing inventory and effects
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        
        // Apply inventory
        ItemStack[] contents = kit.getContents();
        for (int i = 0; i < contents.length && i < 36; i++) {
            if (contents[i] != null) {
                player.getInventory().setItem(i, contents[i].clone());
            }
        }
        
        // Apply armor
        player.getInventory().setArmorContents(kit.getArmorContents());
        
        // Apply attributes
        kit.getAttributeSet().applyTo(player);
        
        // Apply potion effects
        for (KitPotionEffect effect : kit.getPotionEffects()) {
            effect.applyTo(player);
        }
        
        // Set food level based on hunger rule
        if (!kit.getRuleSet().isHunger()) {
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
        
        // Set health to max
        player.setHealth(kit.getAttributeSet().getMaxHealth());
        
        player.updateInventory();
    }
    
    /**
     * Removes kit effects from a player and restores vanilla state.
     * 
     * This method MUST be called on the main thread.
     * 
     * @param player Target player
     * @param kit Kit to remove
     */
    public void removeKit(Player player, Kit kit) {
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // Remove potion effects
        for (KitPotionEffect effect : kit.getPotionEffects()) {
            effect.removeFrom(player);
        }
        
        // Remove attributes
        kit.getAttributeSet().removeFrom(player);
        
        // Reset food level
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        player.updateInventory();
    }
    
    /**
     * Creates a runtime profile for a kit.
     * 
     * This should be called when a duel instance is created,
     * not on every tick.
     * 
     * @param kit Source kit
     * @return Runtime profile
     */
    public KitRuntimeProfile createRuntimeProfile(Kit kit) {
        return new KitRuntimeProfile(kit);
    }
    
    /**
     * Gets the round count for a kit based on match type.
     * 
     * @param kit Kit to check
     * @param ranked Whether the match is ranked
     * @return Round count
     */
    public int getRoundCount(Kit kit, boolean ranked) {
        return ranked ? kit.getRankedRounds() : kit.getUnrankedRounds();
    }
    
    /**
     * Validates a kit and returns any errors.
     * 
     * @param kit Kit to validate
     * @return List of error messages (empty if valid)
     */
    public List<String> validateKit(Kit kit) {
        return validator.validate(kit);
    }
    
    /**
     * Gets the kit storage handler.
     * 
     * @return Kit storage
     */
    public KitStorage getStorage() {
        return storage;
    }
    
    /**
     * Gets the kit validator.
     * 
     * @return Kit validator
     */
    public KitValidator getValidator() {
        return validator;
    }
}
