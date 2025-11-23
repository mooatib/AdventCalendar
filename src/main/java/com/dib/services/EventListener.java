package com.dib.services;

import com.dib.repository.DatabaseMethods;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

    private final RewardService rewardService;

    public EventListener(DatabaseMethods databaseMethods, RewardService rewardService) {
        this.rewardService = rewardService;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        rewardService.giveRewardPlayer(player);
    }

}
