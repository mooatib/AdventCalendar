package com.dib.services;

import com.dib.models.Reward;
import com.dib.repository.DatabaseMethods;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class RewardService {
    private final DatabaseMethods databaseMethods;

    public RewardService(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    public void giveRewardPlayer(Player player) {
        List<Reward> rewardList = databaseMethods.getMissingRewards(player.getUniqueId());

        if (rewardList.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "You are up to date in your advent calendar !");
        }

        //Using java Iterator to avoid concurrent errors (removing while in the for-each loop here)
        java.util.Iterator<Reward> iterator = rewardList.iterator();
        while (iterator.hasNext()) {
            Reward reward = iterator.next();

            ItemStack item = new ItemStack(reward.item(), reward.amount());
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);

            // In order to print the given items in the chat
            String itemName = reward.item().name();

            if (remainingItems.isEmpty()) {
                databaseMethods.insertDayClaimed(player.getUniqueId(), reward.day(), 0);
                iterator.remove();
                player.sendMessage(ChatColor.GOLD + "Day " + reward.day() + ": You received " + reward.amount() + "x " + formatName(itemName) + "!");
            }
            // Reward not given entirely :
            else {
                ItemStack itemRemaining = remainingItems.values().iterator().next();
                int remainingAmount = itemRemaining.getAmount();

                databaseMethods.insertDayClaimed(
                        player.getUniqueId(),
                        reward.day(),
                        remainingAmount
                );

                player.sendMessage(ChatColor.RED + "Your inventory is full! You still have " + remainingAmount + "x " + formatName(itemName) + " left to receive.");
            }
        }
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();

        String[] words = name.split("_");

        for (String word : words) {
            if (word.isEmpty()) continue;
            String capitalized = word.substring(0, 1).toUpperCase()
                    + word.substring(1).toLowerCase();
            formatted.append(capitalized).append(" ");
        }
        return formatted.toString().trim();
    }
}
