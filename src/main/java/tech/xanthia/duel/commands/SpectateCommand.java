package tech.xanthia.duel.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tech.xanthia.duel.DuelInstance;
import tech.xanthia.duel.DuelManager;
import tech.xanthia.duel.DuelSpectatorManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles /spectate command allowing players to join or leave duels as spectators.
 */
public class SpectateCommand implements CommandExecutor, TabCompleter {

    private final DuelManager duelManager;
    private final DuelSpectatorManager spectatorManager;

    public SpectateCommand(DuelManager duelManager, DuelSpectatorManager spectatorManager) {
        this.duelManager = duelManager;
        this.spectatorManager = spectatorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <player|leave>");
            playError(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("stop")) {
            if (!spectatorManager.isSpectating(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are not currently spectating a duel.");
                playError(player);
                return true;
            }
            spectatorManager.stopSpectating(player);
            player.sendMessage(ChatColor.GREEN + "You have left spectator mode.");
            return true;
        }

        if (spectatorManager.isSpectating(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Use /" + label + " leave before spectating another duel.");
            playError(player);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            playError(player);
            return true;
        }

        DuelInstance duel = duelManager.getPlayerDuel(target.getUniqueId());
        if (duel == null) {
            player.sendMessage(ChatColor.RED + target.getName() + " is not in an active duel.");
            playError(player);
            return true;
        }

        if (!spectatorManager.startSpectating(player, duel.getDuelId())) {
            playError(player);
        }
        return true;
    }

    private void playError(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList()));
            if ("leave".startsWith(partial)) {
                suggestions.add("leave");
            }
            if ("stop".startsWith(partial)) {
                suggestions.add("stop");
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
