package com.dib.commands;

import com.dib.services.RewardService;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetRewardCommand extends BukkitCommand {
    private final RewardService rewardService;

    public GetRewardCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases, RewardService rewardService) {
        super(name, description, usageMessage, aliases);
        this.rewardService = rewardService;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        return rewardService.giveRewardPlayer((Player) sender);
    }
}
