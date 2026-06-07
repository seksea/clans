package me.sekc.clans;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ClansPlaceholderExpansion extends PlaceholderExpansion {

	private final Clans plugin;

	public ClansPlaceholderExpansion(Clans plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getAuthor() {
		return String.join(", ", plugin.getPluginMeta().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return plugin.getPluginMeta().getName();
	}

	@Override
	public String getVersion() {
		return plugin.getPluginMeta().getVersion();
	}

	@Override
	public boolean persist() {
		return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
	}

	@Override
	public String onRequest(OfflinePlayer player, String params) {
		String clanName = plugin.databaseConnection.getPlayerClan(player.getUniqueId());
		int clanLevel = 0;
		if (!clanName.isEmpty()) {
			clanLevel = plugin.databaseConnection.calculateLevel(plugin, plugin.databaseConnection.getClanExperience(clanName));
		}

		if (params.equalsIgnoreCase("clan_name")) {
			return clanName;
		}

		if (params.equalsIgnoreCase("clan_level")) {
			return String.valueOf(clanLevel);
		}

		int playerLevel = plugin.databaseConnection.calculateLevel(plugin, plugin.databaseConnection.getPlayerExperience(player.getUniqueId()));

		if (params.equalsIgnoreCase("player_level")) {
			return String.valueOf(playerLevel);
		}

		return null; // Placeholder is unknown by the Expansion
	}
}