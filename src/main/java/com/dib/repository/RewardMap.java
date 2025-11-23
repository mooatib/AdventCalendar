package com.dib.repository;

import com.dib.models.Reward;
import org.bukkit.Material;

import java.util.List;

public class RewardMap {

    public static List<Reward> items = List.of(
            new Reward(1, Material.ITEM_FRAME,4),
            new Reward(2,Material.POTATO,34),
            new Reward(3, Material.BEDROCK, 11),
            new Reward(4, Material.BOOK, 32),
            new Reward(5, Material.IRON_AXE, 3),
            new Reward(6, Material.IRON_CHESTPLATE, 4),
            new Reward(22, Material.BOOKSHELF,23),
            new Reward(23, Material.DIAMOND_AXE, 2)
    );
}
