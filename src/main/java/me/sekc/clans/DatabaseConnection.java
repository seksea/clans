package me.sekc.clans;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseConnection {
    public String sanitiseString(String input) {
        // prepared statements don't allow custom table names, so needs to be sanitised manually
        return input.replace("'", "").replace(";", "").replace("\"", "").replace("\\", "");
    }

    String clanMembersTableSchema = " ("
            + "   uuid     VARCHAR(36) NOT NULL PRIMARY KEY" // foreign key to players table
            + ")";

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

    public class ClanPlayerData {
        public OfflinePlayer offlinePlayer;

        ClanPlayerData(OfflinePlayer offlinePlayer) {
            this.offlinePlayer = offlinePlayer;
        }
    }
    public List<ClanPlayerData> getPlayersInClan(String clan) {
        try {
            ResultSet results = connection.createStatement().executeQuery(
                    "SELECT uuid FROM " + sanitiseString("clan_" + clan + "_members")
            );

            List<ClanPlayerData> playerList = new ArrayList<>();
            while (results.next()) {
                UUID playerUUID = UUID.fromString(results.getString("uuid"));
                playerList.add(new ClanPlayerData(Bukkit.getOfflinePlayer(playerUUID)));
            }
            return playerList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ClanPlayerData getPlayerFromClan(String clan, UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT uuid FROM ? WHERE uuid=?"
            );
            stmt.setString(1, "clan_" + clan + "_members");
            stmt.setString(2, playerUUID.toString());
            ResultSet results = stmt.executeQuery();

            return new ClanPlayerData(Bukkit.getOfflinePlayer(playerUUID));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayerToClan(String clan, UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + sanitiseString("clan_" + clan + "_members") + " (uuid) VALUES (?)"
            );
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();

            PreparedStatement updatePlayerClanStmt = connection.prepareStatement(
                    "UPDATE players SET clan=? WHERE uuid=?"
            );
            updatePlayerClanStmt.setString(1, clan);
            updatePlayerClanStmt.setString(2, playerUUID.toString());
            updatePlayerClanStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayerFromClan(String clan, UUID playerUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM ? WHERE uuid=?"
            );
            stmt.setString(1, "clan_" + clan + "_members");
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();

            PreparedStatement removeClanFromPlayerStmt = connection.prepareStatement(
                    "UPDATE players SET clan=? WHERE uuid=?"
            );
            removeClanFromPlayerStmt.setString(1, null);
            removeClanFromPlayerStmt.setString(2, playerUUID.toString());
            removeClanFromPlayerStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNewClan(String name, UUID ownerUUID) {
        try {
            // Create row in clans table
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO clans (name, owner_uuid) VALUES (?, ?)"
            );
            stmt.setString(1, name);
            stmt.setString(2, ownerUUID.toString());
            stmt.executeUpdate();

            // Create table to store members in the clan
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + sanitiseString("clan_" + name + "_members") + clanMembersTableSchema
            );

            addPlayerToClan(name, ownerUUID); // add the owner as a member
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteClan(String name) {
        try {
            // Delete clan from clans table
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            stmt.executeUpdate();

            // Delete clan members table
            connection.createStatement().executeUpdate(
                    "DROP TABLE ?"
            );

            // Remove all players' clan
            PreparedStatement removeClanFromPlayersStmt = connection.prepareStatement(
                    "UPDATE players SET clan=NULL WHERE clan=?"
            );
            removeClanFromPlayersStmt.setString(1, name);
            removeClanFromPlayersStmt.executeUpdate();

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
