package com.dib.models;

import org.bukkit.enchantments.Enchantment;

public record RewardEnchantment(
        Enchantment type,
        int level
) {
}
