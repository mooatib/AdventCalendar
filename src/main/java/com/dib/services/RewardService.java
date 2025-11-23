package com.dib.services;

import com.dib.models.Reward;
import com.dib.repository.DatabaseMethods;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;

public class RewardService {
    private final DatabaseMethods databaseMethods;

    public RewardService(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    // TODO : meilleur affichage (liste de ce qui a été reçu et pas reçu, et non 21 messages)
    //        donner dans l'ordre (jour 1 puis 2...)
    //        meilleur affichage des noms (pas DIAMOND_SWORD mais Diamond Sword)
    //        message : ===ADVENT CALENDAR===
    public boolean giveRewardPlayer(Player player) {
        Set<Reward> rewardSet = databaseMethods.getMissingRewards(player.getUniqueId());

        if (rewardSet.isEmpty()) {
            //player.sendMessage(ChatColor.GREEN + "You are up to date in your advent calendar !");
            return false;
        }

        //Using java Iterator to avoid concurrent errors (removing while in the for-each loop here)
        java.util.Iterator<Reward> iterator = rewardSet.iterator();
        while (iterator.hasNext()) {
            Reward reward = iterator.next();

            ItemStack item = new ItemStack(reward.item(), reward.amount());
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);

            // In order to print the given items in the chat
            String itemName = reward.item().name();

            if (remainingItems.isEmpty()) {
                databaseMethods.insertDayClaimed(player.getUniqueId(), reward.day(), 0);
                iterator.remove();
                player.sendMessage(ChatColor.GREEN + "Day " + reward.day() + ": You received " + reward.amount() + "x " + itemName + "!");
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

                player.sendMessage(ChatColor.RED + "Your inventory is full! You still have " + remainingAmount + "x " + itemName + " left to receive.");
            }
        }
        //Returns true if the reward is given entirely
        return rewardSet.isEmpty();
    }
}
