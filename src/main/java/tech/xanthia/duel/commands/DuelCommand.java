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
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.KitManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /duel command for quick 1v1 challenges.
 */
public class DuelCommand implements CommandExecutor, TabCompleter {

    private final DuelManager duelManager;
    private final KitManager kitManager;

    public DuelCommand(DuelManager duelManager, KitManager kitManager) {
        this.duelManager = duelManager;
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <kit>");
            playError(player);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            playError(player);
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot duel yourself!");
            playError(player);
            return true;
        }

        if (duelManager.isInDuel(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a duel.");
            playError(player);
            return true;
        }

        if (duelManager.isInDuel(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + target.getName() + " is already in a duel.");
            playError(player);
            return true;
        }

        Kit kit = findKit(args[1]);
        if (kit == null || !kit.isEnabled()) {
            player.sendMessage(ChatColor.RED + "Unknown or disabled kit: " + args[1]);
            playError(player);
            return true;
        }

        DuelInstance duel = duelManager.createDuel(
                Collections.singletonList(player.getUniqueId()),
                Collections.singletonList(target.getUniqueId()),
                kit.getName(),
                false,
                1,
                "standard"
        );

        if (duel == null) {
            player.sendMessage(ChatColor.RED + "Failed to create duel. Check server console for details.");
            playError(player);
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Duel starting against " + target.getName() + " using kit " + kit.getName() + ". Good luck!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + " has challenged you to a duel with kit " + kit.getName() + "!");
        return true;
    }

    private Kit findKit(String input) {
        Kit exact = kitManager.getKit(input);
        if (exact != null) {
            return exact;
        }
        for (Kit kit : kitManager.getAllKits()) {
            if (kit.getName().equalsIgnoreCase(input)) {
                return kit;
            }
        }
        return null;
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
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String partialKit = args[1].toLowerCase();
            return kitManager.getEnabledKits().stream()
                    .map(Kit::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialKit))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
