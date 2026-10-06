# ESP Mod

[![Platform](https://img.shields.io/badge/platform-Minecraft%2026.3-62B47A?style=flat-square)](#)
[![Mod Loader](https://img.shields.io/badge/mod%20loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/license-MIT-d580ff?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/source%20version-2.0.1-00e5ff?style=flat-square)](gradle.properties)

A client-side Fabric mod that shows entity outlines, entity hitboxes, and selected block boxes through walls, with an in-game configuration menu.

## ⚠️ Disclaimer

> This mod is intended for use on servers you own or have explicit permission to use it on. Using ESP mods on public or third-party servers likely violates their rules and may result in a ban. The author is not responsible for any consequences resulting from misuse of this mod. Use it at your own risk.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 26.3 and place it in your mods folder
3. Build v2.0.1 using the instructions below, or download the matching 26.3 jar from [Releases](https://github.com/godblessmerica/ESP-Mod/releases) once published, and place it in your mods folder
4. Launch Minecraft with the Fabric profile

## Requirements

- Minecraft 26.3
- Java 25 or newer
- Fabric Loader 0.19.5+
- Fabric API for 26.3 (built against 0.161.0+26.3)
- Optional: Mod Menu 21.0.0 for 26.3

## Features

### Outline ESP

See glowing outlines through walls for tracked entity types, including invisible entities. Outline colors follow Minecraft's normal behavior.

### Hitbox ESP

See wireframe boxes around tracked entities through walls. Each tracked entity type has its own box color.

### Entity Presets

Add **Players**, **Mobs**, **Vehicles**, **Decorative**, **Projectiles**, **Items & Drops**, **Mods**, or **Other** entities to the tracked list. Presets keep existing entity settings.

### Advanced Entity Settings

Search registered entity types in the Add Entity menu. Toggle, recolor, or remove individual entries from the tracked list.

### Block ESP

Search registered blocks or add the **Ores**, **Containers**, and **Valuables** presets. Toggle, recolor, or remove tracked block types. Loaded chunks are scanned gradually, with nearby chunks prioritized when you change the tracked types. Placed and removed blocks update immediately, even while their chunk awaits scanning. Initial scans and rescans can take seconds to minutes at high render distances.

### Custom Box Colors

Click an entity or block's color swatch to choose a box color with RGB sliders or a `#RRGGBB` hex value. The picker shows a live color preview; select **OK** to apply or **Cancel** to discard.

### Box Range

Limit entity and block boxes to 8–512 blocks, or select **Use Render Distance** to follow the current render distance automatically. This setting does not limit glowing entity outlines.

### Config Menu

Open the config menu with `\`. Bind the ESP toggle in the menu or vanilla Controls. The master toggle switches entity and block ESP together. Menus support native keyboard navigation and narration.

Tracked lists start empty. Open **Entity ESP** or **Block ESP**, then add individual types or select a preset to get started. Entity outlines and hitboxes have separate Show/Hide controls.

## Keybinds

| Key | Action |
|---|---|
| Unbound by default | Toggle ESP on/off |
| `\` | Open config menu |
| `Esc` | Return to the previous menu; cancel color selection or key rebinding |

The ESP toggle and menu bindings are rebindable in **Options → Controls → ESP Mod** or directly in the config menu.

## Notes

- Client-side only - install in your own mods folder, not the server
- Works on any server (vanilla or modded) within your render distance
- Config saves to `.minecraft/config/espmod-entities.json` and `.minecraft/config/blockesp.json`
- Invalid config files are logged and preserved as `.invalid-*` backups; saves replace the file only after writing completes
- Mod Menu is optional - the in-game menu works without it

## Building from Source

With JDK 25 or newer, run `./gradlew build` (`gradlew.bat build` on Windows). The installable jar is `build/libs/esp-mod-2.0.1.jar`.

## License
[MIT](LICENSE)

## Contributing & Feedback

Have a suggestion or found a bug? Feel free to open an [issue](../../issues)!

All feedback is welcome - whether it's a bug report, feature request, or general suggestion - including requests to support a different Minecraft version.
