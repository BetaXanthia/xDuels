package tech.xanthia.kits;

import org.bukkit.Material;
import tech.xanthia.XDuels;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates kits for errors and conflicts.
 * 
 * Ensures:
 * - Display item is valid
 * - Inventories contain valid items
 * - Round counts are sensible
 * - Rules don't conflict
 * - No missing required fields
 * 
 * Invalid kits are automatically disabled.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class KitValidator {
    
    private final XDuels plugin;
    
    /**
     * Creates a new kit validator.
     * 
     * @param plugin Plugin instance
     */
    public KitValidator(XDuels plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Validates a kit and returns a list of errors.
     * 
     * @param kit Kit to validate
     * @return List of error messages (empty if valid)
     */
    public List<String> validate(Kit kit) {
        List<String> errors = new ArrayList<>();
        
        // Validate display item
        if (kit.getDisplayItem() == null || kit.getDisplayItem().getType() == Material.AIR) {
            errors.add("missing display item");
        }
        
        // Validate round counts
        if (kit.getUnrankedRounds() < 1) {
            errors.add("unranked rounds must be >= 1");
        }
        
        if (kit.getRankedRounds() < 1) {
            errors.add("ranked rounds must be >= 1");
        }
        
        // Validate inventories
        if (kit.getContents() == null) {
            errors.add("contents array is null");
        }
        
        if (kit.getArmorContents() == null) {
            errors.add("armor contents array is null");
        }
        
        // Validate rules
        if (!kit.getRuleSet().validate()) {
            errors.add("rule set validation failed");
        }
        
        // Check for conflicting rules
        if (kit.getRuleSet().isOnlyBreakPlayerPlaced() && !kit.getRuleSet().isBreakBlocks()) {
            errors.add("onlyBreakPlayerPlaced requires breakBlocks to be enabled");
        }
        
        // Validate special modes
        int modeCount = 0;
        if (kit.isBoxingMode()) modeCount++;
        if (kit.isSumoMode()) modeCount++;
        if (kit.isSpleefMode()) modeCount++;
        
        if (modeCount > 1) {
            errors.add("kit cannot have multiple special modes enabled");
        }
        
        // Boxing mode validations
        if (kit.isBoxingMode()) {
            if (kit.getRuleSet().isDamageEnabled()) {
                errors.add("boxing mode requires damage to be disabled");
            }
        }
        
        // Sumo mode validations
        if (kit.isSumoMode()) {
            if (kit.getRuleSet().isDamageEnabled()) {
                errors.add("sumo mode requires damage to be disabled");
            }
        }
        
        // Spleef mode validations
        if (kit.isSpleefMode()) {
            if (kit.getRuleSet().isDamageEnabled()) {
                errors.add("spleef mode requires damage to be disabled");
            }
            if (kit.getRuleSet().isRegen()) {
                errors.add("spleef mode should disable regeneration");
            }
        }
        
        // Validate attributes
        if (kit.getAttributeSet().getMaxHealth() < 1.0) {
            errors.add("max health must be >= 1.0");
        }
        
        if (kit.getAttributeSet().getMovementSpeed() < 0.0) {
            errors.add("movement speed cannot be negative");
        }
        
        // Validate potion effects
        if (kit.getPotionEffects() != null) {
            for (KitPotionEffect effect : kit.getPotionEffects()) {
                if (effect.getType() == null) {
                    errors.add("potion effect has null type");
                }
                if (effect.getAmplifier() < 0) {
                    errors.add("potion effect amplifier cannot be negative");
                }
            }
        }
        
        // Log validation results
        if (!errors.isEmpty()) {
            plugin.getLogger().warning("[ KitValidator ] Kit '" + kit.getName() + 
                "' disabled due to validation errors: [" + String.join(", ", errors) + "]");
        }
        
        return errors;
    }
    
    /**
     * Checks if a kit is valid (has no errors).
     * 
     * @param kit Kit to check
     * @return True if valid
     */
    public boolean isValid(Kit kit) {
        return validate(kit).isEmpty();
    }
}
