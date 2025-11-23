package com.dib;

import com.dib.commands.GetRewardCommand;
import com.dib.repository.DatabaseMethods;
import com.dib.services.EventListener;
import com.dib.services.RewardService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AdventCalendarPlugin extends JavaPlugin {
    private final EventListener listener;
    private final DatabaseMethods databaseMethods;
    private final RewardService rewardService;

    private AdventCalendarPlugin() {
        this.databaseMethods = new DatabaseMethods(this.getLogger(),this.getDataFolder());
        rewardService = new RewardService(databaseMethods);
        listener = new EventListener(databaseMethods, rewardService);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(listener, this);
        getLogger().info("Plugin initialized");

        this.getServer().getCommandMap().register(
                this.getName().toLowerCase(),
                new GetRewardCommand("getreward", "Get today's reward", "/getreward", List.of(),rewardService)
        );
    }

    @Override
    public void onDisable() {
        databaseMethods.close();
        getLogger().info("Plugin stopped");
    }
}
