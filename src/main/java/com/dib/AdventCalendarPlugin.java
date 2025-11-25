package com.dib;

import com.dib.commands.ACCommand;
import com.dib.repository.AdventDatabase;
import com.dib.repository.DatabaseMethods;
import com.dib.repository.RewardLoader;
import com.dib.services.EventListener;
import com.dib.services.RewardService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class AdventCalendarPlugin extends JavaPlugin {
    private DatabaseMethods databaseMethods;

    @Override
    public void onEnable() {
        File dbFile = new File(this.getDataFolder().toURI());
        AdventDatabase adventDatabase = new AdventDatabase(this.getLogger(), dbFile, "advent-calendar.db");
        adventDatabase.initializeDatabase();

        this.databaseMethods = new DatabaseMethods(this.getLogger(), adventDatabase);

        RewardService rewardService = new RewardService(this.databaseMethods);
        EventListener listener = new EventListener(rewardService);

        RewardLoader loader = new RewardLoader(this, this.databaseMethods);
        loader.loadRewards();

        Bukkit.getPluginManager().registerEvents(listener, this);
        getLogger().info("Plugin initialized");

        // AC command
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
        // S'assurer que databaseMethods n'est pas null
        if (databaseMethods != null) {
            databaseMethods.close();
        }
        getLogger().info("Plugin stopped");
    }
}