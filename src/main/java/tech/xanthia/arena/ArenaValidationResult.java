package tech.xanthia.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Result object for arena validation operations.
 * Contains statistics and error details from validation checks.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaValidationResult {
    
    private final List<String> errors;
    private int validCount;
    private int invalidCount;
    private int disabledCount;
    
    /**
     * Creates a new validation result.
     */
    public ArenaValidationResult() {
        this.errors = new ArrayList<>();
        this.validCount = 0;
        this.invalidCount = 0;
        this.disabledCount = 0;
    }
    
    /**
     * Adds an error message.
     * 
     * @param error Error message
     */
    public void addError(String error) {
        errors.add(error);
    }
    
    /**
     * Increments the valid arena count.
     */
    public void incrementValid() {
        validCount++;
    }
    
    /**
     * Increments the invalid arena count.
     */
    public void incrementInvalid() {
        invalidCount++;
    }
    
    /**
     * Increments the disabled arena count.
     */
    public void incrementDisabled() {
        disabledCount++;
    }
    
    /**
     * Checks if any errors were found.
     * 
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Gets all error messages.
     * 
     * @return List of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public int getValidCount() {
        return validCount;
    }
    
    public int getInvalidCount() {
        return invalidCount;
    }
    
    public int getDisabledCount() {
        return disabledCount;
    }
    
    /**
     * Gets the total number of arenas checked.
     * 
     * @return Total arena count
     */
    public int getTotalCount() {
        return validCount + invalidCount + disabledCount;
    }
}
