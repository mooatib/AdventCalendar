package com.dib.repository;

import com.dib.models.Reward;
import org.bukkit.Material;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseMethods {
    private final Logger logger;
    private final AdventDatabase adventDatabase;

    public DatabaseMethods(Logger logger, File resourcesFolder) {
        this.logger = logger;
        this.adventDatabase = new AdventDatabase(logger, resourcesFolder, "advent-calendar.db");
        this.adventDatabase.initializeDatabase();
        this.adventDatabase.fillDayRewards();
    }

    private int getDay() {
        return LocalDate.now().getDayOfMonth();
    }

    public Set<Reward> getMissingRewards(UUID playerUUID) {
        Set<Reward> missingRewards = new HashSet<>();

        try {
            Connection conn = adventDatabase.getConnection();
            PreparedStatement ps = conn.prepareStatement(Queries.GET_MISSING_REWARDS);
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, getDay());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int day = rs.getInt("day_item");
                String itemString = rs.getString("item");
                int totalAmount = rs.getInt("amount");

                int amountToGive;

                //Test if the reward has been claimed before
                if (rs.getObject("remaining_amount") == null) {
                    amountToGive = totalAmount;
                } else {
                    // Reward has already been claimed
                    amountToGive = rs.getInt("remaining_amount");
                }
                missingRewards.add(new Reward(
                        day,
                        Material.valueOf(itemString),
                        amountToGive));
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error : getMissingRewards", e);
        }
        return missingRewards;
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

    public void close() {
        adventDatabase.closeConnection();
    }
}
