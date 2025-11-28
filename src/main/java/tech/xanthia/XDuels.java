package tech.xanthia;

import org.bukkit.plugin.java.JavaPlugin;
import tech.xanthia.arena.ArenaManager;
import tech.xanthia.arena.ArenaRegenerationManager;
import tech.xanthia.duel.DuelManager;
import tech.xanthia.duel.DuelSpectatorManager;
import tech.xanthia.duel.commands.DuelCommand;
import tech.xanthia.duel.commands.SpectateCommand;
import tech.xanthia.duel.listeners.*;
import tech.xanthia.kits.KitManager;
import tech.xanthia.kits.command.KitCommand;
import tech.xanthia.kits.listeners.KitGUIListener;
import tech.xanthia.party.PartyManager;
import tech.xanthia.party.PartyPlayerListener;
import tech.xanthia.party.commands.PartyCommand;
import tech.xanthia.queue.QueueManager;
import tech.xanthia.queue.commands.QueueCommand;
import tech.xanthia.stats.EloManager;
import tech.xanthia.stats.StatsManager;
import tech.xanthia.stats.commands.StatsCommand;
import tech.xanthia.arena.command.ArenaCommand;
import tech.xanthia.utils.LoggerAdapter;
import tech.xanthia.listeners.GuiLockListener;
import tech.xanthia.utils.Utils;
import tech.xanthia.arena.listener.ArenaGUIListener;
import tech.xanthia.arena.listener.ArenaEditorListener;
import tech.xanthia.arena.listener.ArenaEditorQuitListener;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * xDuels - Advanced Dueling Plugin for Minecraft Paper
 * 
 * Streamlined main class focusing on implemented systems:
 * - Complete Duel Engine (Section 5)
 * - Arena System with regeneration
 * - Kit System with modifiers
 * - Party System
 * - Queue System with matchmaking
 * - Stats & ELO System     
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public final class XDuels extends JavaPlugin {
    
    // Singleton instance
    private static XDuels instance;
    
    // Logger adapter
    private LoggerAdapter loggerAdapter;
    
    // Async executor
    private ExecutorService asyncExecutor;
    
    // Core managers
    private ArenaRegenerationManager regenManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private PartyManager partyManager;
    private QueueManager queueManager;
    private DuelManager duelManager;
    private DuelSpectatorManager spectatorManager;
    private StatsManager statsManager;
    private EloManager eloManager;
    
    // Counter for naming async threads in a Java 8+ compatible way
    private static final AtomicInteger ASYNC_THREAD_COUNTER = new AtomicInteger(0);

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize logger adapter
        this.loggerAdapter = new LoggerAdapter(this);
        
        // Initialize async executor
        this.asyncExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r);
            thread.setName("xDuels-Async-" + ASYNC_THREAD_COUNTER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        
        getLogger().info("========================================");
        getLogger().info("  xDuels v" + getPluginMeta().getVersion());
        getLogger().info("  Initializing systems...");
        getLogger().info("========================================");
        

        saveDefaultConfig();
        
        try {
            // Initialize managers in dependency order
            getLogger().info("Initializing Arena System...");
            regenManager = new ArenaRegenerationManager(this);
            arenaManager = new ArenaManager(this, regenManager);
            arenaManager.initialize();
            
            getLogger().info("Initializing Kit System...");
            kitManager = new KitManager(this);
            kitManager.initialize();
            
            getLogger().info("Initializing Party System...");
            partyManager = new PartyManager(this);
            partyManager.initialize();
            
            getLogger().info("Initializing Stats & ELO System...");
            statsManager = new StatsManager(this);
            eloManager = new EloManager(this, statsManager);
            
            getLogger().info("Initializing Duel Engine...");
            duelManager = new DuelManager(this, arenaManager, kitManager, partyManager);
            spectatorManager = new DuelSpectatorManager(this, duelManager);
            duelManager.setSpectatorManager(spectatorManager); // Link spectator manager
            
            getLogger().info("Initializing Queue System...");
            queueManager = new QueueManager(this, partyManager);
            queueManager.initialize();
            
            // Register all event listeners
            registerListeners();
            registerCommands();
            
            getLogger().info("========================================");
            getLogger().info("  ✓ xDuels enabled successfully!");
            getLogger().info("========================================");
            
        } catch (Exception e) {
            getLogger().severe("========================================");
            getLogger().severe("  ✗ FATAL ERROR DURING STARTUP");
            getLogger().severe("========================================");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("========================================");
        getLogger().info("  Shutting down xDuels...");
        getLogger().info("========================================");
        
        try {
            // Shutdown managers in reverse order
            if (queueManager != null) {
                getLogger().info("Shutting down Queue System...");
                queueManager.shutdown();
            }
            
            if (duelManager != null) {
                getLogger().info("Shutting down Duel Engine...");
                duelManager.shutdown();
            }
            
            if (statsManager != null) {
                getLogger().info("Saving player statistics...");
                statsManager.shutdown();
            }
            
            if (partyManager != null) {
                getLogger().info("Shutting down Party System...");
                partyManager.shutdown();
            }
            
            if (kitManager != null) {
                getLogger().info("Shutting down Kit System...");
                kitManager.shutdown();
            }
            
            if (arenaManager != null) {
                getLogger().info("Shutting down Arena System...");
                arenaManager.shutdown();
            }
            
            getLogger().info("========================================");
            getLogger().info("  ✓ xDuels disabled successfully!");
            getLogger().info("========================================");
            
        } catch (Exception e) {
            getLogger().severe("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        } finally {
            instance = null;
        }
    }
    
    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        getLogger().info("Registering event listeners...");
        
        var pm = getServer().getPluginManager();
        
        // Duel Engine listeners
        pm.registerEvents(new DuelCombatListener(duelManager, getLogger()), this);
        pm.registerEvents(new DuelMovementListener(duelManager, getLogger()), this);
        pm.registerEvents(new DuelBlockListener(duelManager, getLogger()), this);
        pm.registerEvents(new DuelInventoryListener(duelManager, spectatorManager, getLogger()), this);
        
        // Arena GUI & editor listeners
        pm.registerEvents(new ArenaGUIListener(this), this);
        pm.registerEvents(new ArenaEditorListener(this), this);
        pm.registerEvents(new ArenaEditorQuitListener(this), this);
        
        // Party system listener
        pm.registerEvents(new PartyPlayerListener(partyManager), this);
        
        // Global GUI lock (only for explicitly locked inventories)
        pm.registerEvents(new GuiLockListener(), this);
        
        // Kit GUI listener
        pm.registerEvents(new KitGUIListener(this), this);
        
        getLogger().info("Registered 10 event listeners");
    }

    /**
     * Registers command executors and tab completers.
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        registerCommand("party", new PartyCommand(partyManager));
        registerCommand("queue", new QueueCommand(queueManager, kitManager));
        registerCommand("kit", new KitCommand(this));
        registerCommand("arena", new ArenaCommand(this));
        registerCommand("duel", new DuelCommand(duelManager, kitManager));
        registerCommand("spectate", new SpectateCommand(duelManager, spectatorManager));
        registerCommand("stats", new StatsCommand(statsManager));
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml; unable to register executor.");
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }
    
    // ========================================
    // GETTERS
    // ========================================
    
    public static XDuels getInstance() {
        return instance;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public ArenaRegenerationManager getRegenManager() {
        return regenManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public PartyManager getPartyManager() {
        return partyManager;
    }
    
    public QueueManager getQueueManager() {
        return queueManager;
    }
    
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    public DuelSpectatorManager getSpectatorManager() {
        return spectatorManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public EloManager getEloManager() {
        return eloManager;
    }
    
    public LoggerAdapter getLoggerAdapter() {
        return loggerAdapter;
    }
    
    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
    
    public org.bukkit.configuration.file.FileConfiguration getMessagesConfig() {
        return getConfig(); // Fallback to main config
    }
    
    /**
     * Gets a colorized message from the config with a default fallback.
     *
     * @param path Config path
     * @param defaultMessage Default value if path missing
     * @return Colorized message string
     */
    public String getMessage(String path, String defaultMessage) {
        String raw = getMessagesConfig().getString(path);
        if (raw == null || raw.isEmpty()) {
            raw = defaultMessage;
        }
        return Utils.colorize(raw);
    }

    public ArenaRegenerationManager getRegenerationManager() {
        return regenManager;
    }
}