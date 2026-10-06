# ESP Mod

[![Platform](https://img.shields.io/badge/platform-Minecraft%2026.3-62B47A?style=flat-square)](#)
[![Mod Loader](https://img.shields.io/badge/mod%20loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/license-MIT-d580ff?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/source%20version-1.1.0-00e5ff?style=flat-square)](gradle.properties)

A client-side Fabric mod that shows entity outlines, entity hitboxes, and selected block boxes through walls, with an in-game configuration menu.

**The current source is v1.1.0 for Minecraft 26.3. The latest published GitHub release is still [v1.0.0](https://github.com/godblessmerica/ESP-Mod/releases/tag/v1.0.0), which targets Minecraft 26.1.2.** Build from source for 26.3 until a v1.1.0 release is published.

## Changes from v1.0.0 to v1.1.0

This comparison is based on the [v1.0.0 source tag](https://github.com/godblessmerica/ESP-Mod/tree/v1.0.0) and the current code. See the [full source diff](https://github.com/godblessmerica/ESP-Mod/compare/v1.0.0...main).

| Area | v1.0.0 | v1.1.0 |
|---|---|---|
| Minecraft | 26.1.2 | 26.3 |
| Name / mod ID | Spectral ESP / `spectralesp` | ESP Mod / `espmod` |
| ESP targets | Entities | Entities and selected blocks |
| Entity settings | Search the full registry and toggle types | Search to add types; toggle, recolor, or remove tracked entries |
| Entity box colors | Fixed colors for players, mobs, and other entities | Custom color per tracked entity type, with RGB sliders and hex input |
| Entity presets | Players, Mobs, Vehicles, Technical, All Entities bulk toggles | Eight additive categories that preserve existing entity settings |
| Block presets | None | Ores, Containers, Valuables, plus individual block selection |
| Box range | No configurable range | 8–512 blocks or automatic render distance |
| Controls | `G` toggles ESP; `\` opens the menu | Toggle starts unbound; `\` opens the menu; master toggle controls entity and block ESP |
| Config | One `spectral-esp.json` file; master enabled state not saved | Separate entity/block files; enabled states saved; invalid-file backups and completed-write replacement |

Other changes include Minecraft 26.3 input support, native keyboard navigation and narration for list controls, outlines for tracked invisible entities, and updated gizmo submission for entity boxes. Block scanning runs gradually on the client thread, uses one cache per chunk, and updates scanned positions when blocks change. A small assertion-based regression check now runs with the build.

### Upgrading from v1.0.0

- Remove the old Spectral ESP jar before installing ESP Mod. The mod ID changed, so Fabric treats them as different mods.
- Install Minecraft 26.3 and matching Fabric dependencies; the v1.0.0 jar targets 26.1.2.
- Old `spectral-esp.json` settings are not imported. New tracked lists start empty: add the **Players** preset or choose the entity/block types you want.
- Rebind the ESP toggle in the menu or Controls; it no longer defaults to `G`. The renamed key bindings also mean old bindings are not imported.
- Colors apply to entity hitbox boxes and block boxes. Glowing outline colors still follow Minecraft's normal behavior.

### Validation

The v1.1.0 build and regression assertions passed. A Minecraft 26.3 development client also initialized with the updated ESP code without ESP mixin errors. ESP rendering and menu interaction have not yet been visually tested inside a world.

## ⚠️ Disclaimer

> This mod is intended for use on servers you own or have explicit permission to use it on. Using ESP mods on public or third-party servers likely violates their rules and may result in a ban. The author is not responsible for any consequences resulting from misuse of this mod. Use it at your own risk.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 26.3 and place it in your mods folder
3. Build v1.1.0 using the instructions below, or download the matching 26.3 jar from [Releases](https://github.com/godblessmerica/ESP-Mod/releases) once published, and place it in your mods folder
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

With JDK 25 or newer, run `./gradlew build` (`gradlew.bat build` on Windows). The build includes an assertion-based `selfCheck` covering config round trips, malformed config preservation, duplicate IDs, and block cache updates. The installable jar is `build/libs/esp-mod-1.1.0.jar`.

The project follows Fabric's 26.3 template structure: split main/client source sets, Java 25 target, and Gradle 9.7.1 wrapper. Loom is pinned to 1.18.2 for repeatable builds. Minecraft's SDL input migration is described in the [Fabric 26.3 porting notes](https://www.fabricmc.net/2026/09/15/263.html).

## License
[MIT](LICENSE)

## Contributing & Feedback

Have a suggestion or found a bug? Feel free to open an [issue](../../issues)!

All feedback is welcome - whether it's a bug report, feature request, or general suggestion - including requests to support a different Minecraft version.
