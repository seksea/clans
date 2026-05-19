package me.sekc.clans;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.clip.placeholderapi.PlaceholderAPI;
import me.sekc.clans.commands.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public final class Clans extends JavaPlugin {
    public DatabaseConnection databaseConnection;
    public YamlConfiguration messagesYml;

    String chatMessageFormat;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            err("Could not find PlaceholderAPI! This plugin is required, disabling this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        log("\n" +
                "   .oooooo.   oooo                                 \n" +
                "  d8P'  `Y8b  `888                                 \n" +
                " 888           888   .oooo.   ooo. .oo.    .oooo.o \n" +
                " 888           888  `P  )88b  `888P\"Y88b  d88(  \"8 \n" +
                " 888           888   .oP\"888   888   888  `\"Y88b.  \n" +
                " `88b    ooo   888  d8(  888   888   888  o.  )88b \n" +
                "  `Y8bood8P'  o888o `Y888\"\"8o o888o o888o 8\"\"888P' \n" +
                "                 The ultimate clan+leveling plugin \n" +
                "==================================================="
        );

        // save yml to disk
        boolean replaceConfigs = true; // turn this on for easy debug of default configs
        saveResource("config.yml", /* replace */ replaceConfigs);
        chatMessageFormat = getConfig().getString("clan-message-format");

        loadMessagesYml(replaceConfigs);

        try {
            databaseConnection = new DatabaseConnection(getConfig().getString("database.filepath"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CommandManager.registerCommands(this);

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        log("The clans plugin has been disabled.");
    }

    public void loadMessagesYml(boolean replaceConfigs) {
        saveResource("messages.yml", /* replace */ replaceConfigs);

        messagesYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));

        final InputStream defConfigStream = getResource("messages.yml");
        if (defConfigStream == null) {
            return;
        }

        messagesYml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public String getMessageWithPlaceholders(UUID playerUUID, String messageYmlPath, Map<String, String> customPlaceholders) {
        OfflinePlayer offlinePlayer = null;
        if (playerUUID != null)
            offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

        String original = messagesYml.getString(messageYmlPath);

        if (original == null) {
            warn("MISSING " + messageYmlPath + " FROM messages.yml");
            return "MISSING " + messageYmlPath + " FROM messages.yml";
        }

        if (customPlaceholders != null) {
            for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
                original = original.replace(entry.getKey(), entry.getValue());
            }
        }


        return PlaceholderAPI.setPlaceholders(offlinePlayer, original);
    }

    public void commandResponseInChat(CommandSourceStack source, String messageYmlPath, Map<String, String> customPlaceholders) {
        source.getSender().sendMessage(
                MiniMessage.miniMessage().deserialize(
                        chatMessageFormat + "<reset>" + getMessageWithPlaceholders(source.getExecutor().getUniqueId(), messageYmlPath, customPlaceholders)
                )
        );
    }

    static public void log(String msg) {
        Bukkit.getLogger().info("[clans] " + msg);
    }

    static public void warn(String msg) {
        Bukkit.getLogger().warning("[clans] " + msg);
    }

    static public void err(String msg) {
        Bukkit.getLogger().severe("[clans] " + msg);
    }
}
