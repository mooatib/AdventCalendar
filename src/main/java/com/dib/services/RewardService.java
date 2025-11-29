package com.dib.services;

import com.dib.models.Reward;
import com.dib.repository.DatabaseMethods;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class RewardService {
    private final DatabaseMethods databaseMethods;

    public RewardService(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    public void giveRewardPlayer(Player player) {
        List<Reward> rewards = databaseMethods.getMissingRewards(player.getUniqueId());

        if (rewards.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "You are up to date in your advent calendar !");
            return;
        }

        for (Reward reward : rewards) {
            ItemStack item = createRewardItem(reward);
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
            String itemName = reward.customName().orElse(formatName(reward.item().name()));

            if (remainingItems.isEmpty()) {
                databaseMethods.insertDayClaimed(player.getUniqueId(), reward.day(), 0);
                player.sendMessage(ChatColor.GOLD + "Day " + reward.day() + ": You received " + reward.amount() + "x " + itemName + "!");
            } else {
                int remainingAmount = remainingItems.values().iterator().next().getAmount();
                databaseMethods.insertDayClaimed(player.getUniqueId(), reward.day(), remainingAmount);
                player.sendMessage(ChatColor.RED + "Your inventory is full! You still have " + remainingAmount + "x " + itemName + " left to receive.");
            }
        }
    }

    private ItemStack createRewardItem(Reward reward) {
        ItemStack item = new ItemStack(reward.item(), reward.amount());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        reward.customName().ifPresent(name -> meta.customName(Component.text(name)));
        reward.enchantment().ifPresent(enchant ->
                meta.addEnchant(enchant.type(), enchant.level(), true)
        );

        item.setItemMeta(meta);
        return item;
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
