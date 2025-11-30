package com.dib.repository;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SantaRepository {
    private final Logger logger;
    private final AdventDatabase adventDatabase;

    public SantaRepository(Logger logger, AdventDatabase adventDatabase) {
        this.logger = logger;
        this.adventDatabase = adventDatabase;
    }

    public void saveSantaLocation(Location location) {
        try (Connection conn = adventDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(Queries.SAVE_SANTA_LOCATION)) {

            ps.setString(1, location.getWorld().getName());
            ps.setDouble(2, location.getX());
            ps.setDouble(3, location.getY());
            ps.setDouble(4, location.getZ());
            ps.setFloat(5, location.getYaw());
            ps.setFloat(6, location.getPitch());

            ps.executeUpdate();
            logger.info("Santa location saved to database");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving Santa location", e);
        }
    }

    public Optional<Location> getSantaLocation() {
        try (Connection conn = adventDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(Queries.GET_SANTA_LOCATION);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    logger.warning("World '" + worldName + "' not found for Santa NPC");
                    return Optional.empty();
                }

                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                return Optional.of(new Location(world, x, y, z, yaw, pitch));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving Santa location", e);
        }

        return Optional.empty();
    }

    public void deleteSantaLocation() {
        try (Connection conn = adventDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(Queries.DELETE_SANTA_LOCATION)) {

            ps.executeUpdate();
            logger.info("Santa location deleted from database");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting Santa location", e);
        }
    }

    public void close() {
        adventDatabase.closeConnection();
    }
}
