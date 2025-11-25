package com.dib.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdventDatabase {
    private final Logger logger;
    private final String databaseUrl;
    private Connection connection;

    public AdventDatabase(Logger logger, File resourcesFolder, String databaseFileName) {
        this.logger = logger;
        this.databaseUrl = "jdbc:sqlite:" + new File(resourcesFolder, databaseFileName).getAbsolutePath();

        if (!resourcesFolder.exists()) {
            boolean res = resourcesFolder.mkdirs();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(databaseUrl);
            logger.info("Database connection established");
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        stmt.execute(Queries.CREATE_TABLE_DAYS_CLAIMED);
        stmt.execute(Queries.CREATE_TABLE_DAY_REWARD);
    }

    public void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {
            createTables(stmt);
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
        }
    }



}
