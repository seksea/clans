package me.sekc.clans;

import java.sql.*;
import java.util.UUID;

public class DatabaseConnection {
    Connection connection;

    DatabaseConnection(String databasePath) throws Exception {
        Clans.log("Loading SQLite database at \"" + databasePath + "\"...");

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(databasePath);

        if (connection != null) {
            var meta = connection.getMetaData();
            Clans.log("Database driver: " + meta.getDriverName());
        }

        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS clans ("
                        + "   name           VARCHAR(32) NOT NULL PRIMARY KEY,"
                        + "   owner_uuid     VARCHAR(36) NOT NULL,"
                        + "   description    VARCHAR(512) NOT NULL DEFAULT ''"
                        + ")"
        );

        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS players ("
                        + "   uuid           VARCHAR(36) NOT NULL PRIMARY KEY,"
                        + "   experience     INTEGER NOT NULL DEFAULT 0,"
                        + "   clan           VARCHAR(32)" // foreign key to clan table
                        + ")"
        );
    }

    /*-----------------------------------
     *  Clans
     -----------------------------------*/

    public void createNewClan(String name, UUID ownerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO clans (name, owner_uuid) VALUES (?, ?)"
            );
            stmt.setString(1, name);
            stmt.setString(2, ownerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean clanExists(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            ResultSet results = stmt.executeQuery();
            return results.isBeforeFirst(); // false if empty
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getClanDescription(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT description FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            ResultSet results = stmt.executeQuery();
            return results.getString("description");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setClanDescription(String name, String description) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE clans SET description=? WHERE name=?"
            );
            stmt.setString(1, description);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*-----------------------------------
     *  Players
     -----------------------------------*/

    public void createPlayer(UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO players (uuid) VALUES (?)"
            );
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean playerExists(UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM players WHERE uuid=?"
            );
            stmt.setString(1, playerUUID.toString());
            ResultSet results = stmt.executeQuery();
            return results.isBeforeFirst(); // false if empty
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPlayerExperience(UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT experience FROM players WHERE uuid=?"
            );
            stmt.setString(1, playerUUID.toString());
            ResultSet results = stmt.executeQuery();
            return results.getInt("experience");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPlayerExperience(UUID playerUUID, int experience) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET experience=? WHERE uuid=?"
            );
            stmt.setInt(1, experience);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPlayerClan(UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT clan FROM players WHERE uuid=?"
            );
            stmt.setString(1, playerUUID.toString());
            ResultSet results = stmt.executeQuery();
            String clan = results.getString("clan");
            return clan == null ? "" : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPlayerClan(UUID playerUUID, String clan) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET clan=? WHERE uuid=?"
            );
            stmt.setString(1, clan);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
