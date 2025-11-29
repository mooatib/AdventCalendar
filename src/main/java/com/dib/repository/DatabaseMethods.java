package com.dib.repository;

import com.dib.codecs.RewardCodec;
import com.dib.models.Reward;
import com.dib.models.RewardEnchantment;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseMethods {
    private final Logger logger;
    private final AdventDatabase adventDatabase;

    public DatabaseMethods(Logger logger, AdventDatabase adventDatabase) {
        this.logger = logger;
        this.adventDatabase = adventDatabase;
        this.adventDatabase.initializeDatabase();
    }

    public void load(List<Reward> rewards) {
        rewards.forEach(this::insertReward);
        logger.info("All rewards loaded in database !");
    }

    private int getDay() {
        return LocalDate.now().getDayOfMonth();
    }

    public List<Reward> getMissingRewards(UUID playerUUID) {
        List<Reward> missingRewards = new ArrayList<>();

        try (Connection conn = adventDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(Queries.GET_MISSING_REWARDS)) {

            ps.setString(1, playerUUID.toString());
            ps.setInt(2, getDay());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    missingRewards.add(buildRewardFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving missing rewards for player " + playerUUID, e);
        }

        return missingRewards;
    }

    private Reward buildRewardFromResultSet(ResultSet rs) throws SQLException {
        int day = rs.getInt("day_item");
        Material item = Material.valueOf(rs.getString("item"));
        int totalAmount = rs.getInt("amount");
        int amountToGive = rs.getObject("remaining_amount") == null
                ? totalAmount
                : rs.getInt("remaining_amount");

        Optional<String> customName = Optional.ofNullable(rs.getString("custom_name"));
        Optional<RewardEnchantment> enchantment = buildEnchantmentFromResultSet(rs);

        return new Reward(day, item, amountToGive, customName, enchantment);
    }

    private Optional<RewardEnchantment> buildEnchantmentFromResultSet(ResultSet rs) throws SQLException {
        String enchantmentType = rs.getString("enchantment_type");
        if (enchantmentType == null) {
            return Optional.empty();
        }

        int level = rs.getInt("enchantment_level");
        return Optional.of(new RewardEnchantment(RewardCodec.fromString(enchantmentType), level));
    }

    public void insertDayClaimed(UUID playerUUID, int day, int remainingAmount) {
        try {
            Connection conn = adventDatabase.getConnection();
            PreparedStatement ps = conn.prepareStatement(Queries.ADD_DAY_CLAIMED);
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, day);
            ps.setInt(3, remainingAmount);

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error : insertDayClaimed", e);
        }
    }

    public void insertReward(Reward reward) {
        try {
            Connection conn = adventDatabase.getConnection();
            PreparedStatement ps = conn.prepareStatement(Queries.FILL_REWARDS_TABLE);

            ps.setInt(1, reward.day());
            ps.setString(2, reward.item().toString());
            ps.setInt(3, reward.amount());
            ps.setString(4, reward.customName().orElse(null));
            ps.setString(5, reward.enchantment().map(RewardEnchantment::type).map(e -> e.getKey().getKey()).orElse(null));
            ps.setInt(6, reward.enchantment().map(RewardEnchantment::level).orElse(0));
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error : insertReward", e);
        }
    }

    public void resetPlayerRewards(Player player) {
        try (Connection conn = adventDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Queries.RESET_PLAYER_REWARDS)) {

            String playerUUID = player.getUniqueId().toString();
            stmt.setString(1, playerUUID);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.log(Level.INFO, "Advent calendar reset for" + playerUUID);
            } else {
                logger.log(Level.INFO, "No entries to remove for " + playerUUID);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error during reward reset.", e);
        }
    }

    public void close() {
        adventDatabase.closeConnection();
    }
}
