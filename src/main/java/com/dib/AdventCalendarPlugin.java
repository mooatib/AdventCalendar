package com.dib;

import com.dib.codecs.RewardCodec;
import com.dib.commands.ACCommand;
import com.dib.models.Reward;
import com.dib.repository.AdventDatabase;
import com.dib.repository.RewardRepository;
import com.dib.services.EventListener;
import com.dib.services.RewardService;
import com.dib.services.SantaNPCManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class AdventCalendarPlugin extends JavaPlugin {
    private final AdventDatabase adventDatabase;
    private final RewardRepository rewardRepository;
    private final EventListener eventListener;
    private final RewardService rewardService;
    private final SantaNPCManager santaNPCManager;

    public AdventCalendarPlugin() {
        this.adventDatabase = new AdventDatabase(this.getLogger(), new File(this.getDataFolder().toURI()), "advent-calendar.db");
        this.rewardRepository = new RewardRepository(this.getLogger(), adventDatabase);
        this.rewardService = new RewardService(this.rewardRepository);
        this.santaNPCManager = new SantaNPCManager(this.getLogger());
        this.eventListener = new EventListener(rewardService, santaNPCManager);
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
                        this.rewardRepository,
                        this,
                        santaNPCManager
                )
        );
    }

    @Override
    public void onDisable() {
        adventDatabase.closeConnection();
        getLogger().info("Plugin stopped");
    }

    private void initRewards() {
        File file = new File(this.getDataFolder(), "rewards.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            this.saveResource("rewards.yml", false);
        }
        List<Reward> rewards = RewardCodec.decodeAll(config);
        rewardRepository.load(rewards);
    }
}