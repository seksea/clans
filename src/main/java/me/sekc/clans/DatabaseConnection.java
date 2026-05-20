package me.sekc.clans;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public List<String> getAllClanNames() {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT name FROM clans"
            );
            ResultSet results = stmt.executeQuery();
            List<String> clanNameList = new ArrayList<>();
            while (results.next()) {
                clanNameList.add(results.getString("name"));
            }
            return clanNameList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<UUID, String>> getPlayersInClan(String clan) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT uuid FROM players where clan=?"
            );
            stmt.setString(1, clan);
            ResultSet results = stmt.executeQuery();
            List<Pair<UUID, String>> playerList = new ArrayList<>();
            while (results.next()) {
                UUID playerUUID = UUID.fromString(results.getString("uuid"));
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                playerList.add(Pair.of(playerUUID, offlinePlayer.getName()));
            }
            return playerList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    public void deleteClan(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            stmt.executeUpdate();

            PreparedStatement stmt2 = connection.prepareStatement(
                    "UPDATE players SET clan=NULL WHERE clan=?"
            );
            stmt2.setString(1, name);
            stmt2.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getClanOwner(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT owner_uuid FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            ResultSet results = stmt.executeQuery();
            return UUID.fromString(results.getString("owner_uuid"));
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
            String description = results.getString("description");
            return description == null ? "" : description;
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
            return clan == null ? "" : clan;
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

    public void playerLeaveClan(UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET clan=? WHERE uuid=?"
            );
            stmt.setString(1, null);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getClanOwnedByPlayer(UUID playerUUID) { // Get the clan this player owns, returns null if does not own any clans
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT clan FROM players WHERE uuid=?"
            );
            stmt.setString(1, playerUUID.toString());
            ResultSet results = stmt.executeQuery();
            String clan = results.getString("clan");

            if (clan == null || clan.isEmpty()) return null;

            UUID owner = getClanOwner(clan);
            return owner.equals(playerUUID) ? clan : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
