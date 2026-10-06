# ESP Mod

[![Platform](https://img.shields.io/badge/platform-Minecraft%2026.3-62B47A?style=flat-square)](#)
[![Mod Loader](https://img.shields.io/badge/mod%20loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/license-MIT-d580ff?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/version-2.1.0-00e5ff?style=flat-square)](../../releases/latest)

A client-side Fabric mod that shows entity outlines, entity hitboxes, and selected block boxes through walls, with an in-game configuration menu.

## ⚠️ Disclaimer

> This mod is intended for use on servers you own or have explicit permission to use it on. Using ESP mods on public or third-party servers likely violates their rules and may result in a ban. The author is not responsible for any consequences resulting from misuse of this mod. Use it at your own risk.

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your mods folder
3. Download the latest jar from [Releases](../../releases/latest) and place it in your mods folder
4. Launch Minecraft with the Fabric profile

## Requirements
- Minecraft 26.3
- Java 25 or newer
- Fabric Loader 0.19.5+
- Fabric API for 26.3 (built against 0.161.0+26.3)
- Optional: Mod Menu 21.0.0 for 26.3

## Features

### Outline ESP
See glowing entity outlines through walls. Works on any entity type you have enabled.

### Hitbox ESP
See wireframe boxes rendered around selected entities using Minecraft's always-on-top gizmos. Each tracked entity type has its own box color.

### Entity Presets
Add **Players**, **Mobs**, **Vehicles**, **Decorative**, **Projectiles**, **Items & Drops**, **Mods**, or **Other** entities to the tracked list. Presets keep existing entity settings.

### Advanced Entity Settings
Search registered entity types in the Add Entity menu. Toggle, recolor, or remove individual entries from the tracked list.

### Block ESP
Search registered blocks or add the **Ores**, **Containers**, and **Valuables** presets. Loaded chunks are scanned gradually on the client thread, and block changes update the cache immediately after a chunk has been scanned. Initial scans and rescans can take seconds to minutes at high render distances.

### Box Range
Limit entity and block boxes to 8–512 blocks, or select **Use Render Distance** to follow the current render distance automatically. This setting does not limit glowing entity outlines.

### Config Menu
Open the config menu with `\`. Bind the ESP toggle in the menu or vanilla Controls. The master toggle switches entity and block ESP together. Menus support native keyboard navigation and narration.

## Keybinds
| Key | Action |
|---|---|
| Unbound by default | Toggle ESP on/off |
| `\` | Open config menu |

Both keys are rebindable in **Options → Controls → ESP Mod** or directly in the config menu.

## Notes
- Client-side only - install in your own mods folder, not the server
- Works on any server (vanilla or modded) within your render distance
- Config saves to `.minecraft/config/espmod-entities.json` and `.minecraft/config/blockesp.json`
- Invalid config files are logged and preserved as `.invalid-*` backups; saves replace the file only after writing completes
- Mod Menu is optional - the in-game menu works without it

## Build and Check

With JDK 25 or newer, run `./gradlew build` (`gradlew.bat build` on Windows). The build includes an assertion-based `selfCheck` covering config round trips, malformed config preservation, duplicate IDs, and block cache updates. The installable jar is `build/libs/esp-mod-2.1.0.jar`.

The project follows the included 26.3 template's split main/client source sets, Java 25 target, and Gradle 9.7.1 wrapper. Loom is pinned to 1.18.2 for repeatable builds. Minecraft's SDL input migration is described in the [Fabric 26.3 porting notes](https://www.fabricmc.net/2026/09/15/263.html).

## License
[MIT](LICENSE)

## Contributing & Feedback

Have a suggestion or found a bug? Feel free to open an [issue](../../issues)!

All feedback is welcome - whether it's a bug report, feature request, or general suggestion - including requests to support a different Minecraft version.
