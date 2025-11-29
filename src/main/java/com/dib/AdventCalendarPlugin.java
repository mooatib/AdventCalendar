package com.dib;

import com.dib.codecs.RewardCodec;
import com.dib.commands.ACCommand;
import com.dib.models.Reward;
import com.dib.repository.AdventDatabase;
import com.dib.repository.DatabaseMethods;
import com.dib.services.EventListener;
import com.dib.services.RewardService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class AdventCalendarPlugin extends JavaPlugin {
    private final DatabaseMethods databaseMethods;
    private final EventListener eventListener;
    private final RewardService rewardService;

    public AdventCalendarPlugin() {
        AdventDatabase adventDatabase = new AdventDatabase(this.getLogger(), new File(this.getDataFolder().toURI()), "advent-calendar.db");
        this.databaseMethods = new DatabaseMethods(this.getLogger(), adventDatabase);
        this.rewardService = new RewardService(this.databaseMethods);
        this.eventListener = new EventListener(rewardService);
        adventDatabase.initializeDatabase();
        initRewards();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        getLogger().info("Plugin initialized");

        this.getServer().getCommandMap().register(
                this.getName().toLowerCase(),
                new ACCommand(
                        "adventcalendar",
                        "Advent calendar command",
                        "/adventcalendar",
                        List.of("ac"),
                        rewardService,
                        this.databaseMethods,
                        this
                )
        );
    }

    @Override
    public void onDisable() {
        if (databaseMethods != null) {
            databaseMethods.close();
        }
        getLogger().info("Plugin stopped");
    }

    private void initRewards() {
        File file = new File(this.getDataFolder(), "rewards.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            this.saveResource("rewards.yml", false);
        }
        List<Reward> rewards = RewardCodec.decodeAll(config);
        databaseMethods.load(rewards);
    }
}