package com.dib.repository;

import com.dib.models.Reward;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RewardLoader {

    private final JavaPlugin plugin;
    private final DatabaseMethods databaseMethods;

    public RewardLoader(JavaPlugin plugin, DatabaseMethods databaseMethods) {
        this.plugin = plugin;
        this.databaseMethods = databaseMethods;
    }

    public void loadRewards() {
        File file = new File(plugin.getDataFolder(), "rewards.yml");
        if (!file.exists()) {
            plugin.saveResource("rewards.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("rewards")) {
            plugin.getLogger().warning("rewards.yml is empty !");
            return;
        }

        // 4. Boucle sur chaque jour défini dans le fichier
        for (String key : config.getConfigurationSection("rewards").getKeys(false)) {
            try {
                //To avoid crashes if a wrong material is inserted
                Material material = Material.valueOf(config.getString("rewards." + key + ".material"));
                if (material == null) {
                    plugin.getLogger().warning("Invalid material : " + material.name());
                    continue;
                }
                Reward reward = new Reward(
                        Integer.parseInt(key),
                        material,
                        config.getInt("rewards." + key + ".amount")
                );

                databaseMethods.insertReward(reward);
            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Format error for day " + key);
            }
        }
        plugin.getLogger().info("All rewards loaded in database !");
    }


}
