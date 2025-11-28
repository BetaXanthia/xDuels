package tech.xanthia.duel.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tech.xanthia.duel.DuelInstance;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * DuelTimer.java
 * Handles countdown and timer functionality for duels.
 * 
 * Features:
 * - Countdown sequences (3-2-1-GO)
 * - Round timers
 * - Sound effects
 * - Title/actionbar displays
 * - Callback support
 * 
 * @author xDuels
 * @since 1.0.0
 */
public class DuelTimer {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final DuelInstance duel;
    
    private BukkitTask timerTask;
    private int remaining;
    
    public DuelTimer(JavaPlugin plugin, DuelInstance duel) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.duel = duel;
    }
    
    /**
     * Start countdown sequence
     * 
     * @param seconds Countdown duration
     * @param onComplete Callback when countdown completes
     */
    public void startCountdown(int seconds, Runnable onComplete) {
        this.remaining = seconds;
        
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remaining > 0) {
                // Display countdown
                displayCountdown(remaining);
                
                // Play sound
                playCountdownSound(remaining);
                
                remaining--;
            } else {
                // Countdown complete
                displayGo();
                playGoSound();
                
                // Cancel task
                cancel();
                
                // Execute callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }, 0L, 20L); // Run every second
    }
    
    /**
     * Start round timer
     * 
     * @param seconds Timer duration
     * @param onExpire Callback when timer expires
     */
    public void startRoundTimer(int seconds, Runnable onExpire) {
        this.remaining = seconds;
        
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            remaining--;
            
            // Display warnings at key intervals
            if (remaining == 60 || remaining == 30 || remaining == 10 || remaining <= 5 && remaining > 0) {
                displayTimeWarning(remaining);
            }
            
            if (remaining <= 0) {
                // Timer expired
                cancel();
                
                if (onExpire != null) {
                    onExpire.run();
                }
            }
        }, 20L, 20L); // Start after 1 second, run every second
    }
    
    /**
     * Display countdown number
     * 
     * @param number Countdown number
     */
    private void displayCountdown(int number) {
        NamedTextColor color = switch (number) {
            case 3 -> NamedTextColor.RED;
            case 2 -> NamedTextColor.YELLOW;
            case 1 -> NamedTextColor.GREEN;
            default -> NamedTextColor.WHITE;
        };
        
        Component title = Component.text(String.valueOf(number), color);
        
        Title countdown = Title.title(
                title,
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(0)
                )
        );
        
        for (Player player : duel.getOnlinePlayers()) {
            player.showTitle(countdown);
        }
    }
    
    /**
     * Display GO! message
     */
    private void displayGo() {
        Component title = Component.text("GO!", NamedTextColor.GREEN);
        Component subtitle = Component.text("Fight!", NamedTextColor.GRAY);
        
        Title goTitle = Title.title(
                title,
                subtitle,
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(500)
                )
        );
        
        for (Player player : duel.getOnlinePlayers()) {
            player.showTitle(goTitle);
        }
    }
    
    /**
     * Display time warning
     * 
     * @param seconds Remaining seconds
     */
    private void displayTimeWarning(int seconds) {
        String message = "§e" + seconds + " seconds remaining!";
        
        for (Player player : duel.getOnlinePlayers()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        }
    }
    
    /**
     * Play countdown sound
     * 
     * @param number Countdown number
     */
    private void playCountdownSound(int number) {
        Sound sound = Sound.BLOCK_NOTE_BLOCK_HAT;
        float pitch = switch (number) {
            case 3 -> 0.8f;
            case 2 -> 1.0f;
            case 1 -> 1.2f;
            default -> 1.0f;
        };
        
        for (Player player : duel.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1.0f, pitch);
        }
    }
    
    /**
     * Play GO sound
     */
    private void playGoSound() {
        for (Player player : duel.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        }
    }
    
    /**
     * Cancel timer
     */
    public void cancel() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
    
    /**
     * Get remaining time
     * 
     * @return Remaining seconds
     */
    public int getRemaining() {
        return remaining;
    }
    
    /**
     * Check if timer is running
     * 
     * @return true if active
     */
    public boolean isActive() {
        return timerTask != null;
    }
}
