package com.dib.commands;

import com.dib.repository.RewardRepository;
import com.dib.services.RewardService;
import com.dib.services.SantaNPCManager;
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
    private final RewardRepository rewardRepository;
    private final JavaPlugin plugin;
    private final SantaNPCManager santaNPCManager;

    public ACCommand(@NotNull String name,
                     @NotNull String description,
                     @NotNull String usageMessage,
                     @NotNull List<String> aliases,
                     RewardService rewardService,
                     RewardRepository rewardRepository,
                     JavaPlugin plugin,
                     SantaNPCManager santaNPCManager)
    {
        super(name, description, usageMessage, aliases);
        this.rewardService = rewardService;
        this.rewardRepository = rewardRepository;
        this.plugin = plugin;
        this.santaNPCManager = santaNPCManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage : /" + getLabel() + " <claim|reset|spawnsanta|removesanta>");
            return false;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            return handleReset(sender, args);
        }

        else if (args[0].equalsIgnoreCase("claim")) {
            return handleClaim(sender);
        }

        else if (args[0].equalsIgnoreCase("spawnsanta")) {
            return handleSpawnSanta(sender);
        }

        else if (args[0].equalsIgnoreCase("removesanta")) {
            return handleRemoveSanta(sender);
        }

        else {
            sender.sendMessage(ChatColor.YELLOW + "--- AC Command ---");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar claim" + ChatColor.GRAY + " : Claim available rewards");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar reset <username>" + ChatColor.GRAY + " : Reset chosen player's rewards");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar spawnsanta" + ChatColor.GRAY + " : Spawn Santa NPC at your location");
            sender.sendMessage(ChatColor.YELLOW + "/adventcalendar removesanta" + ChatColor.GRAY + " : Remove Santa NPC");
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            rewardRepository.resetPlayerRewards((Player) target);
            sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s advent calendar rewards !");
        });
        return true;
    }

    private boolean handleSpawnSanta(CommandSender sender) {
        if (!sender.hasPermission("adventcalendar.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permissions to execute this command.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return false;
        }

        boolean success = santaNPCManager.spawnSanta(player.getLocation());

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Santa NPC spawned successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "Santa NPC already exists! Use /ac removesanta first.");
        }

        return true;
    }

    private boolean handleRemoveSanta(CommandSender sender) {
        if (!sender.hasPermission("adventcalendar.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permissions to execute this command.");
            return true;
        }

        boolean success = santaNPCManager.removeSanta();

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Santa NPC removed successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "No Santa NPC found!");
        }

        return true;
    }
}
