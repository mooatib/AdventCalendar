package com.dib.services;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EventListener implements Listener {

    private final RewardService rewardService;
    private final SantaNPCManager santaNPCManager;

    public EventListener(RewardService rewardService, SantaNPCManager santaNPCManager) {
        this.rewardService = rewardService;
        this.santaNPCManager = santaNPCManager;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        int entityId = event.getRightClicked().getEntityId();

        if (santaNPCManager.isSanta(entityId)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.GOLD + "Ho ho ho! Merry Christmas!");
            rewardService.giveRewardPlayer(player);
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Mannequin mannequin) {
                if (!santaNPCManager.tryCaptureSantaEntity(mannequin)) {
                    if (santaNPCManager.isSanta(entity.getEntityId())) {
                        santaNPCManager.reapplySantaProperties(mannequin);
                    }
                }
            }
        }
    }
}
