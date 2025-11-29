package com.dib.codecs;

import com.dib.models.Reward;
import com.dib.models.RewardEnchantment;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardCodec {

    public static List<Reward> decodeAll(FileConfiguration config) {
        ConfigurationSection rewardsSection = getRewardsSection(config);

        List<Reward> rewards = new ArrayList<>();
        for (String dayKey : rewardsSection.getKeys(false)) {
            int day = parseDayKey(dayKey);
            Reward reward = decodeOne(rewardsSection, day);
            rewards.add(reward);
        }

        return rewards;
    }

    private static Reward decodeOne(ConfigurationSection rewardsSection, int day) {
        ConfigurationSection daySection = getDaySection(rewardsSection, day);

        Material material = parseMaterial(daySection, day);
        int amount = daySection.getInt("amount", 1);
        Optional<String> customName = parseCustomName(daySection);
        Optional<RewardEnchantment> enchantment = parseEnchantment(daySection);

        return new Reward(day, material, amount, customName, enchantment);
    }

    private static ConfigurationSection getSection(ConfigurationSection parent, String key, String errorMessage) {
        if (!parent.contains(key)) {
            throw new IllegalArgumentException(errorMessage);
        }

        ConfigurationSection section = parent.getConfigurationSection(key);
        if (section == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return section;
    }

    private static ConfigurationSection getRewardsSection(FileConfiguration config) {
        return getSection(config, "rewards", "Missing rewards section in config");
    }

    private static ConfigurationSection getDaySection(ConfigurationSection rewardsSection, int day) {
        return getSection(rewardsSection, String.valueOf(day), "Day " + day + " not found in rewards config");
    }

    private static Material parseMaterial(ConfigurationSection daySection, int day) {
        String materialName = daySection.getString("material");
        if (materialName == null) {
            throw new IllegalArgumentException("Material not specified for day " + day);
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialName + " for day " + day);
        }

        return material;
    }

    private static Optional<String> parseCustomName(ConfigurationSection daySection) {
        ConfigurationSection metadata = daySection.getConfigurationSection("metadata");
        if (metadata == null || !metadata.contains("custom_name")) {
            return Optional.empty();
        }

        String name = metadata.getString("custom_name");
        return (name != null && !name.isEmpty()) ? Optional.of(name) : Optional.empty();
    }

    private static Optional<RewardEnchantment> parseEnchantment(ConfigurationSection daySection) {
        ConfigurationSection metadata = daySection.getConfigurationSection("metadata");
        if (metadata == null) {
            return Optional.empty();
        }

        ConfigurationSection enchantmentSection = metadata.getConfigurationSection("enchantment");
        if (enchantmentSection == null) {
            return Optional.empty();
        }

        String enchantmentName = enchantmentSection.getString("name");
        if (enchantmentName == null || enchantmentName.isEmpty()) {
            return Optional.empty();
        }

        Enchantment enchantment = fromString(enchantmentName);

        if (enchantment == null) {
            return Optional.empty();
        }

        int enchantmentLevel = enchantmentSection.getInt("level", 1);
        return Optional.of(new RewardEnchantment(enchantment, enchantmentLevel));
    }

    public static Enchantment fromString(String type) {
        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        NamespacedKey key = NamespacedKey.minecraft(type.toLowerCase());
        return enchantmentRegistry.get(key);
    }

    private static int parseDayKey(String dayKey) {
        try {
            return Integer.parseInt(dayKey);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid day key: " + dayKey + " (must be a number)");
        }
    }
}
