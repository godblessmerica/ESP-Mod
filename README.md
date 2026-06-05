# ESP Mod

[![Platform](https://img.shields.io/badge/platform-Minecraft%2026.1.2-62B47A?style=flat-square)](#)
[![Mod Loader](https://img.shields.io/badge/mod%20loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/license-MIT-d580ff?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-00e5ff?style=flat-square)](../../releases/latest)

A client-side Fabric mod that lets you see entity outlines and hitboxes through walls. Supports players, mobs, vehicles, and more - with per-entity control and an in-game config menu.

## ⚠️ Disclaimer

> This mod is intended for use on servers you own or have explicit permission to use it on. Using ESP mods on public or third-party servers likely violates their rules and may result in a ban. The author is not responsible for any consequences resulting from misuse of this mod. Use it at your own risk.

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your mods folder
3. Download the latest jar from [Releases](../../releases/latest) and place it in your mods folder
4. Launch Minecraft with the Fabric profile

## Requirements
- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+

## Features

### Outline ESP
See glowing entity outlines through walls. Works on any entity type you have enabled.

### Hitbox ESP
See colored wireframe boxes rendered around entities (is currently broken and doesnt show through walls.) Players appear in red, mobs in orange, and other entities in yellow.

### Entity Presets
Quick toggles for **Players**, **Mobs**, **Vehicles**, **Technical**, and **All Entities**. Each preset bulk-updates the individual entity settings in the advanced panel.

### Advanced Entity Settings
A full scrollable list of every registered entity type, grouped into categories. Use the search bar to find specific entities and toggle them individually - overriding the global presets.

### Config Menu
Open the config menu in-game at any time with `\`. Rebind both keys directly in the menu without going to vanilla Controls. Press `G` to toggle ESP on/off instantly.

## Keybinds
| Key | Action |
|---|---|
| `G` | Toggle ESP on/off |
| `\` | Open config menu |

Both keys are rebindable in **Options → Controls → ESP Mod** or directly in the config menu.

## Notes
- Client-side only - install in your own mods folder, not the server
- Works on any server (vanilla or modded) within your render distance
- Config saves automatically to `.minecraft/config/espmod.json`
- Mod Menu is optional - the in-game menu works without it

## License
[MIT](LICENSE)

## Contributing & Feedback

Have a suggestion or found a bug? Feel free to open an [issue](../../issues)!

All feedback is welcome - whether it's a bug report, feature request, or general suggestion - including requests to support a different Minecraft version.
