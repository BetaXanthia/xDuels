package tech.xanthia.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter for the plugin logger with additional formatting and features.
 * 
 * Provides convenient methods for logging at different levels with consistent formatting.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class LoggerAdapter {
    
    private final Logger logger;
    private final String prefix;
    
    /**
     * Creates a new logger adapter.
     * 
     * @param plugin Plugin instance
     */
    public LoggerAdapter(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.prefix = "[" + plugin.getName() + "] ";
    }
    
    /**
     * Logs an info message.
     * 
     * @param message Message to log
     */
    public void info(String message) {
        logger.info(message);
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message Message to log
     */
    public void warn(String message) {
        logger.warning(message);
    }
    
    /**
     * Logs a warning message (alias for warn).
     * 
     * @param message Message to log
     */
    public void warning(String message) {
        logger.warning(message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message Message to log
     */
    public void error(String message) {
        logger.severe(message);
    }
    
    /**
     * Logs a severe error message.
     * 
     * @param message Message to log
     */
    public void severe(String message) {
        logger.severe(message);
    }
    
    /**
     * Logs a debug message (only if debug is enabled).
     * 
     * @param message Message to log
     */
    public void debug(String message) {
        logger.log(Level.FINE, message);
    }
    
    /**
     * Logs a message at a specific level.
     * 
     * @param level Log level
     * @param message Message to log
     */
    public void log(Level level, String message) {
        logger.log(level, message);
    }
    
    /**
     * Logs an exception with a message.
     * 
     * @param message Message to log
     * @param throwable Exception to log
     */
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Gets the underlying Java logger.
     * 
     * @return Java logger
     */
    public Logger getLogger() {
        return logger;
    }
}
