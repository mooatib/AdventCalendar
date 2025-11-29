package com.dib.models;

import org.bukkit.Material;

import java.util.Optional;

public record Reward(int day,
                     Material item,
                     int amount,
                     Optional<String> customName,
                     Optional<RewardEnchantment> enchantment) {
}
