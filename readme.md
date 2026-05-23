<img src="docs/logo.png" alt="Alt text" width="100%" style="image-rendering: pixelated;">

# The ultimate clan plugin

---

Let players group up to form clans and fight for the top of the leaderboard.

## Features
 - Clans can level up by burning items of value in the "furnace" GUI
 - Progress stored in SQLite database
 - Highly configurable via yaml
   - Edit GUI layouts [example](src/main/resources/gui/mainmenu.yml)
   - Configurable via config options in [config.yml](src/main/resources/config.yml)
   - Edit chat messages with [messages.yml](src/main/resources/messages.yml)
 - GUI (including optional resource pack if you want cute UI, it is free and you have my permission to merge the assets in [resourcepack/](resourcepack/) into your server's resource pack, or modify them to your liking)
 - Shared clan storage (more unlocked as your clan levels up)
 - Admin commands allowing you to manage other clans & reload the yaml files
 - Clan leaderboard
 - Command system with brigadier

## Screenshots

![img.png](docs/mainmenu.png)

## Dependencies 
 - PlaceholderAPI (Not required, but the plugin does support placeholderAPI placeholders in the messages.yml & gui yml files if installed)

## Setup

1. Drag the plugin .jar file to your servers' `plugins/` directory
2. (Optional) If you would like to have the custom GUI background pngs, then you must merge the `resourcepack` folder into your servers resource pack)