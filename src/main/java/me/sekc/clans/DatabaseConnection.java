package me.sekc.clans;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.*;
import java.util.*;

public class DatabaseConnection {
    public String sanitiseString(String input) {
        // prepared statements don't allow custom table names, so needs to be sanitised manually
        return input.replace("'", "").replace(";", "").replace("\"", "").replace("\\", "");
    }

    String clanMembersTableSchema = " ("
            + "   uuid     VARCHAR(36) NOT NULL PRIMARY KEY" // foreign key to players table
            + ")";

	String clanStorageSchema = " ("
		+ "   id            INTEGER PRIMARY KEY,"
		+ "   storage_name  VARCHAR(32) NOT NULL DEFAULT 'Unnamed',"
		+ "   color         VARCHAR(32) NOT NULL DEFAULT 'WHITE',"
		+ "   data          TEXT" // Base64 encoded NBT data
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
                        + "   description    VARCHAR(512) NOT NULL DEFAULT '',"
                        + "   experience     INTEGER NOT NULL DEFAULT 0"
                        + ")"
        );

        connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS players ("
                        + "   uuid           VARCHAR(36) NOT NULL PRIMARY KEY,"
                        + "   experience     INTEGER NOT NULL DEFAULT 0,"
                        + "   clan           VARCHAR(32)" // foreign key to clan table
                        + ")"
        );

		connection.createStatement().executeUpdate(
			"CREATE TABLE IF NOT EXISTS invites ("
				+ "   target_uuid    VARCHAR(36) NOT NULL," // foreign key to players table
				+ "   inviter_uuid   VARCHAR(36) NOT NULL," // foreign key to players table
				+ "   clan           VARCHAR(32)," // foreign key to clan table
				+ "   description    VARCHAR(256) NOT NULL DEFAULT ''"
				+ ")"
		);

		connection.createStatement().executeUpdate(
			"CREATE TABLE IF NOT EXISTS furnace_items ("
				+ "   material		VARCHAR(128) NOT NULL," // material for fast searching in database
				+ "   data          TEXT NOT NULL PRIMARY KEY," // Base64 encoded NBT data
				+ "   xp          	INTEGER NOT NULL DEFAULT 1"
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
			try (ResultSet results = stmt.executeQuery()) {
				List<String> clanNameList = new ArrayList<>();
				while (results.next()) {
					clanNameList.add(results.getString("name"));
				}
				return clanNameList;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getClanLeaderboardNames() {
		try {
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT name FROM clans ORDER BY experience DESC"
			);
			try (ResultSet results = stmt.executeQuery()) {
				List<String> clanNameList = new ArrayList<>();
				while (results.next()) {
					clanNameList.add(results.getString("name"));
				}
				return clanNameList;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

    public static class ClanPlayerData {
        public OfflinePlayer offlinePlayer;

        ClanPlayerData(OfflinePlayer offlinePlayer) {
            this.offlinePlayer = offlinePlayer;
        }
    }
    public List<ClanPlayerData> getPlayersInClan(String clan) {
        try {
            try (ResultSet results = connection.createStatement().executeQuery(
                    "SELECT uuid FROM " + sanitiseString("clan_" + clan + "_members")
            )) {

                List<ClanPlayerData> playerList = new ArrayList<>();
                while (results.next()) {
                    UUID playerUUID = UUID.fromString(results.getString("uuid"));
                    playerList.add(new ClanPlayerData(Bukkit.getOfflinePlayer(playerUUID)));
                }
                return playerList;
            }
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
            try (ResultSet results = stmt.executeQuery()) {

            return new ClanPlayerData(Bukkit.getOfflinePlayer(playerUUID));
            }
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
                    "DELETE FROM " + sanitiseString("clan_" + clan + "_members") + " WHERE uuid=?"
            );
            stmt.setString(1, playerUUID.toString());
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

            // Create table to store clan storage
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + sanitiseString("clan_" + name + "_storage") + clanStorageSchema
            );

            addStorageToClan(name); // Add a single storage slot to the clan to start with

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
                    "DROP TABLE " + sanitiseString("clan_" + name + "_members")
            );

            // Delete clan storage table
            connection.createStatement().executeUpdate(
                    "DROP TABLE " + sanitiseString("clan_" + name + "_storage")
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
            try (ResultSet results = stmt.executeQuery()) {
                return UUID.fromString(results.getString("owner_uuid"));
            }
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
            try (ResultSet results = stmt.executeQuery()) {
                return results.isBeforeFirst(); // false if empty
            }
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
            try (ResultSet results = stmt.executeQuery()) {
                String description = results.getString("description");
                return description == null ? "" : description;
            }
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

	public int calculateLevel(Clans clans, int experience) {
		double div = clans.getConfig().getDouble("leveling.div");
		double pow = clans.getConfig().getDouble("leveling.pow");
		if (div != 0 && pow != 0) {
			return (int)Math.floor(Math.pow((double)experience/div, pow));
		}
		throw new RuntimeException("invalid leveling.div or leveling.pow in config.yml");
	}

	public int calculateNumStorageSlotsForClan(Clans clans, int level) {
		double div = clans.getConfig().getDouble("storage-awards.div");
		double pow = clans.getConfig().getDouble("storage-awards.pow");
		if (div != 0 && pow != 0) {
			return (int)Math.floor(Math.pow((double)level/div, pow));
		}
		throw new RuntimeException("invalid storage-awards.div or storage-awards.pow in config.yml");
	}

	// needs to be called for every level gained!! cannot jump levels, beware! see clanExperienceUpdated
	public void handleLevelUp(Clans clans, String name, int newLevel) {
		int oldNumStorage = calculateNumStorageSlotsForClan(clans, newLevel-1);
		int newNumStorage = calculateNumStorageSlotsForClan(clans, newLevel);

		// Send message to all online members of the clan
		for (ClanPlayerData playerData : getPlayersInClan(name)) {
			Player player = playerData.offlinePlayer.getPlayer();
			if (player == null)
				continue;

			clans.messageInChat(player, "level-up", Map.ofEntries(
				Map.entry("%new_level%", String.valueOf(newLevel))
			));
		}

		// Add new storage (this will basically always just add one)
		for (int curNumStorage = oldNumStorage; newNumStorage > curNumStorage; curNumStorage++) {
			addStorageToClan(name);

			// Send message to all online members of the clan
			for (ClanPlayerData playerData : getPlayersInClan(name)) {
				Player player = playerData.offlinePlayer.getPlayer();
				if (player == null)
					continue;

				clans.messageInChat(player, "storage.award-storage", null);
			}
		}
	}

	public void clanExperienceUpdated(Clans clans, String name, int oldValue, int newValue) { // called by setClanExperience
		int oldLevel = calculateLevel(clans, oldValue);
		int newLevel = calculateLevel(clans, newValue);

		for (int curLevel = oldLevel + 1; curLevel <= newLevel; curLevel++) {
			handleLevelUp(clans, name, curLevel);
		}
	}

    public int getClanExperience(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT experience FROM clans WHERE name=?"
            );
            stmt.setString(1, name);
            try (ResultSet results = stmt.executeQuery()) {
                return results.getInt("experience");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setClanExperience(Clans clans, String name, int value) {
        try {
			int oldValue = getClanExperience(name);

            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE clans SET experience=? WHERE name=?"
            );
            stmt.setInt(1, value);
            stmt.setString(2, name);
            stmt.executeUpdate();

			clanExperienceUpdated(clans, name, oldValue, value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class StorageSlot {
        public String title;
        public DyeColor color;
        public List<ItemStack> itemStacks;

        StorageSlot(String title, DyeColor color, List<ItemStack> itemStacks) {
            this.title = title;
            this.color = color;
            this.itemStacks = itemStacks;
        }

        String getItemStackData() {
            byte[] bytes = ItemStack.serializeItemsAsBytes(itemStacks);
            return Base64Coder.encodeLines(bytes);
        }

        static StorageSlot fromItemStackData(String title, String color, String itemStackData) {
            byte[] bytes = Base64Coder.decodeLines(itemStackData);
            return new StorageSlot(title, DyeColor.valueOf(color), new ArrayList<>(Arrays.asList(ItemStack.deserializeItemsFromBytes(bytes))));
        }

        static String createEmptyContainerString(int numRows) {
            List<ItemStack> itemStacks = new ArrayList<>();

            for (int i = 0; i <= numRows*9; i++) {
                itemStacks.add(ItemStack.empty());
            }

            StorageSlot temp = new StorageSlot("", DyeColor.WHITE, itemStacks);
            return temp.getItemStackData();
        }
    }
    public List<StorageSlot> getStorageListFromClan(String clan) {
        try {
            try (ResultSet results = connection.createStatement().executeQuery(
                    "SELECT storage_name, color, data FROM " + sanitiseString("clan_" + clan + "_storage")
            )) {

                List<StorageSlot> storageList = new ArrayList<>();
                while (results.next()) {
                    String data = results.getString("data");
                    if (data == null) {
                        data = StorageSlot.createEmptyContainerString(6);
                    }

                    storageList.add(StorageSlot.fromItemStackData(
                            results.getString("storage_name"),
                            results.getString("color"),
                            data));
                }
                return storageList;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageSlot getStorageFromClan(String clan, int index) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT storage_name, color, data FROM " + sanitiseString("clan_" + clan + "_storage") + " WHERE id=?"
            );
            stmt.setInt(1, index+1); // sql index starts from 1
            try (ResultSet results = stmt.executeQuery()) {
                String data = results.getString("data");
                if (data == null) {
                    data = StorageSlot.createEmptyContainerString(6);
                }
                return StorageSlot.fromItemStackData(
                        results.getString("storage_name"),
                        results.getString("color"),
                        data);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addStorageToClan(String clan) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + sanitiseString("clan_" + clan + "_storage") + " DEFAULT VALUES"
            );
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // use ItemStack.empty() to set air
    public void setItemInClanStorage(String clan, ItemStack itemStack, int storageIndex, int slotID) {
        try {
            StorageSlot storage = getStorageFromClan(clan, storageIndex);

            storage.itemStacks.set(slotID, itemStack);

            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE " + sanitiseString("clan_" + clan + "_storage") + " SET data=? WHERE id=?"
            );
            stmt.setString(1, storage.getItemStackData());
            stmt.setInt(2, storageIndex+1);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void editStorageName(String clan, int index, String newName) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE " + sanitiseString("clan_" + clan + "_storage") + " SET storage_name=? WHERE id=?"
            );
            stmt.setString(1, newName);
            stmt.setInt(2, index+1); // sql index starts from 1
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void editStorageColor(String clan, int index, DyeColor newColor) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE " + sanitiseString("clan_" + clan + "_storage") + " SET color=? WHERE id=?"
            );
            stmt.setString(1, newColor.name());
            stmt.setInt(2, index+1); // sql index starts from 1
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
            try (ResultSet results = stmt.executeQuery()) {
                return results.isBeforeFirst(); // false if empty
            }
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
            try(ResultSet results = stmt.executeQuery()) {
                return results.getInt("experience");
            }
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
            try (ResultSet results = stmt.executeQuery()) {
                String clan = results.getString("clan");
                return clan == null ? "" : clan;
            }
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
            try (ResultSet results = stmt.executeQuery()) {
                String clan = results.getString("clan");

                if (clan == null || clan.isEmpty()) return null;

                UUID owner = getClanOwner(clan);
                return owner.equals(playerUUID) ? clan : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*-----------------------------------
     *  Invites
     -----------------------------------*/

    public void sendClanInvite(String clanName, String description, UUID targetUUID, UUID inviterUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO invites (target_uuid, inviter_uuid, clan, description) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, targetUUID.toString());
            stmt.setString(2, inviterUUID.toString());
            stmt.setString(3, clanName);
            stmt.setString(4, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ClanInviteData {
        public UUID targetUUID;
        public String clanName;
        public UUID inviterUUID;
        public String description;
        private ClanInviteData(UUID targetUUID, String clanName, UUID inviterUUID, String description) {
            this.targetUUID = targetUUID;
            this.clanName = clanName;
            this.inviterUUID = inviterUUID;
            this.description = description;
        }
    }
    public List<ClanInviteData> getClanInvitesForPlayer(UUID targetUUID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT inviter_uuid, clan, description FROM invites WHERE target_uuid=?"
            );
            stmt.setString(1, targetUUID.toString());

            List<ClanInviteData> invites = new ArrayList<>();
            try (ResultSet results = stmt.executeQuery()) {
                while (results.next()) {
                    invites.add(new ClanInviteData(
                            targetUUID,
                            results.getString("clan"),
                            UUID.fromString(results.getString("inviter_uuid")),
                            results.getString("description")
                    ));
                }
                return invites;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteInvitesFromClan(UUID playerUUID, String clan) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM invites WHERE clan=? AND target_uuid=?"
            );
            stmt.setString(1, clan);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*-----------------------------------
     *  Furnace
     -----------------------------------*/

	public static class FurnaceItem {
		public ItemStack item;
		public int xp;
		FurnaceItem(ItemStack item, int xp) {
			this.item = item;
			this.xp = xp;
		}

		String getItemStackData() {
			byte[] bytes = ItemStack.serializeItemsAsBytes(Collections.singleton(item));
			return Base64Coder.encodeLines(bytes);
		}

		static FurnaceItem fromItemStackData(String itemStackData, int xp) {
			byte[] bytes = Base64Coder.decodeLines(itemStackData);
			return new FurnaceItem(ItemStack.deserializeItemsFromBytes(bytes)[0], xp);
		}
	}

	public void addItemToFurnaceItemList(ItemStack itemStack, int xp) {
		try {
			PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO furnace_items (material, data, xp) VALUES (?, ?, ?)"
			);
			FurnaceItem furnaceItem = new FurnaceItem(itemStack, xp);
			stmt.setString(1, itemStack.getType().toString());
			stmt.setString(2, furnaceItem.getItemStackData());
			stmt.setInt(3, xp);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	// returns null if no similar item in list
	public FurnaceItem getSimilarItemFromFurnaceItemList(ItemStack itemStack) {
		try {
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT data, xp FROM furnace_items WHERE material=?"
			);
			stmt.setString(1, itemStack.getType().toString());

			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					FurnaceItem newItem = FurnaceItem.fromItemStackData(results.getString("data"), results.getInt("xp"));

					if (itemStack.isSimilar(newItem.item)) {
						return newItem;
					}
				}
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
