<img src="docs/logo.png" alt="Clans Logo" width="100%" style="image-rendering: pixelated;">

# The ultimate clans plugin
[\[preview video\]](https://youtu.be/AA58mq7kdhI)

[spigot page (download jar here)](https://www.spigotmc.org/resources/clans.135746/)

[support discord](https://discord.gg/QySnx7Vpuh)

---

Let players group up to form clans and fight for the top of the leaderboard.

### You can see this plugin in action on my server [clans.bounceme.net](https://modrinth.com/server/clans_server)

## Features
 - Clans can level up by burning items of value in the "furnace"
 - The furnace burns items slowly in the background (even when no-one is online!)
 - Progress stored in SQLite database
 - Highly configurable via yaml
   - Edit GUI layouts [example](src/main/resources/gui/mainmenu.yml)
   - Configurable via config options in [config.yml](src/main/resources/config.yml)
   - Edit chat messages with [messages.yml](src/main/resources/messages.yml)
 - GUI (including custom gui glyphs using my [packman]() plugin)
 - Shared clan storage (more unlocked as your clan levels up)
 - Admin commands allowing you to manage other clans & reload the yaml files
 - Clan leaderboard
 - Command system with brigadier

## Screenshots

![img.png](docs/mainmenu.png)

## Dependencies
- [Packman](https://www.spigotmc.org/resources/packman.136204/) (not required, but used for custom chest GUI glyphs, so definitely recommended)
- PlaceholderAPI (Not required, but the plugin does support placeholderAPI placeholders in the messages.yml & gui yml files if installed, and also will add %clans_clan_name%, %clans_clan_level%, and %clans_player_level% placeholders)

## Setup

1. Drag the plugin .jar file to your servers' `plugins/` directory
2. (Optional) If you would like to have the custom GUI pngs, then you must install the [Packman](https://www.spigotmc.org/resources/packman.136204/) plugin, takes 2 mins, highly recommended for the full experience