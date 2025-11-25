package com.dib.commands;

import com.dib.repository.DatabaseMethods;
import com.dib.services.RewardService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ACCommand extends BukkitCommand {

    private final RewardService rewardService;
    private final DatabaseMethods databaseMethods;
    private final JavaPlugin plugin;

    public ACCommand(@NotNull String name,
                     @NotNull String description,
                     @NotNull String usageMessage,
                     @NotNull List<String> aliases,
                     RewardService rewardService,
                     DatabaseMethods databaseMethods,
                     JavaPlugin plugin)
    {
        super(name, description, usageMessage, aliases);
        this.rewardService = rewardService;
        this.databaseMethods = databaseMethods;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage : /" + getLabel() + " <claim|reset>");
            return false;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            return handleReset(sender, args);
        }

        else if (args[0].equalsIgnoreCase("claim")) {
            return handleClaim(sender);
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "--- AC Command ---");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar claim" + ChatColor.GRAY + " : Claim available rewards");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar reset <username>" + ChatColor.GRAY + " : Reset chosen player's rewards");
            return true;
        }
    }

    private boolean handleClaim(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return false;
        }

        rewardService.giveRewardPlayer(player);
        return true;
    }
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("adventcalendar.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permissions to execute this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage : /adventcalendar reset <username>");
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        target.getUniqueId();

        // Gemini
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            databaseMethods.resetPlayerRewards((Player) target);
            sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s advent calendar rewards !");
        });
        return true;
    }
}
