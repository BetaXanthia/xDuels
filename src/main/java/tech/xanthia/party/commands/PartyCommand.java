package tech.xanthia.party.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tech.xanthia.party.PartyManager;

import java.util.*;

/**
 * Root party command with subcommand routing.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    
    private final PartyManager partyManager;
    private final Map<String, SubCommand> subCommands;
    
    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
        this.subCommands = new HashMap<>();
        
        // Register subcommands
        registerSubCommand(new CreateSubCommand(partyManager));
        registerSubCommand(new DisbandSubCommand(partyManager));
        registerSubCommand(new LeaveSubCommand(partyManager));
        registerSubCommand(new InviteSubCommand(partyManager));
        registerSubCommand(new JoinSubCommand(partyManager));
        registerSubCommand(new KickSubCommand(partyManager));
        registerSubCommand(new PromoteSubCommand(partyManager));
        registerSubCommand(new DemoteSubCommand(partyManager));
        registerSubCommand(new TransferSubCommand(partyManager));
        registerSubCommand(new StatusSubCommand(partyManager));
        registerSubCommand(new ChatSubCommand(partyManager));
        registerSubCommand(new SplitSubCommand(partyManager));
        registerSubCommand(new FfaSubCommand(partyManager));
    }
    
    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            player.sendMessage("§cUnknown subcommand! Use /party help for a list of commands.");
            return true;
        }
        
        // Remove subcommand name from args
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        subCommand.execute(player, subArgs);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        
        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String subCommandName : new TreeSet<>(subCommands.keySet())) {
                if (input.isEmpty() || subCommandName.startsWith(input)) {
                    completions.add(subCommandName);
                }
            }
            return completions;
        }
        
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.tabComplete(player, subArgs);
        }
        
        return Collections.emptyList();
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Party Commands ===");
        player.sendMessage("§e/party create §7- Create a new party");
        player.sendMessage("§e/party disband §7- Disband your party");
        player.sendMessage("§e/party leave §7- Leave your current party");
        player.sendMessage("§e/party invite <player> §7- Invite a player");
        player.sendMessage("§e/party join <player> §7- Join a player's party");
        player.sendMessage("§e/party kick <player> §7- Kick a member");
        player.sendMessage("§e/party promote <player> §7- Promote a member");
        player.sendMessage("§e/party demote <player> §7- Demote a member");
        player.sendMessage("§e/party transfer <player> §7- Transfer leadership");
        player.sendMessage("§e/party status §7- View party status");
        player.sendMessage("§e/party chat §7- Toggle party chat");
        player.sendMessage("§e/party split §7- Toggle split mode");
        player.sendMessage("§e/party ffa §7- Toggle FFA mode");
    }
}
