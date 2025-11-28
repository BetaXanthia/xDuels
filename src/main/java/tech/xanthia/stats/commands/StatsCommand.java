package tech.xanthia.stats.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tech.xanthia.stats.PlayerStats;
import tech.xanthia.stats.StatsManager;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Displays duel statistics for players.
 */
public class StatsCommand implements CommandExecutor, TabCompleter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private final StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [player]");
            if (sender instanceof Player player) {
                playError(player);
            }
            return true;
        }

        OfflinePlayer target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player.");
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getOfflinePlayer(args[0]);
            if ((target == null || (!target.isOnline() && !target.hasPlayedBefore()))) {
                sender.sendMessage(ChatColor.RED + "No statistics found for player: " + args[0]);
                if (sender instanceof Player player) {
                    playError(player);
                }
                return true;
            }
        }

        UUID uuid = target.getUniqueId();
        PlayerStats stats = statsManager.getPlayerStats(uuid);

        sender.sendMessage(ChatColor.GOLD + "———[ " + ChatColor.YELLOW + "xDuels Stats" + ChatColor.GOLD + " ]———");
        sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + target.getName());
        sender.sendMessage(ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + stats.getWins());
        sender.sendMessage(ChatColor.YELLOW + "Losses: " + ChatColor.WHITE + stats.getLosses());
        sender.sendMessage(ChatColor.YELLOW + "W/L Ratio: " + ChatColor.WHITE + DECIMAL_FORMAT.format(stats.getWinRate()) + "%");
        sender.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + stats.getKills());
        sender.sendMessage(ChatColor.YELLOW + "Deaths: " + ChatColor.WHITE + stats.getDeaths());
        sender.sendMessage(ChatColor.YELLOW + "KDR: " + ChatColor.WHITE + DECIMAL_FORMAT.format(stats.getKDR()));
        sender.sendMessage(ChatColor.YELLOW + "Damage Dealt: " + ChatColor.WHITE + stats.getDamageDealt());
        sender.sendMessage(ChatColor.YELLOW + "Damage Taken: " + ChatColor.WHITE + stats.getDamageTaken());
        sender.sendMessage(ChatColor.GOLD + "———————————————");
        return true;
    }

    private void playError(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 1) {
            return Collections.emptyList();
        }
        
        String partial = args.length == 0 ? "" : args[0].toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
