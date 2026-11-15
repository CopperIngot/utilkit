# UtilKit

A small, client-side Fabric utility mod for **Minecraft 1.21.11**, built to run inside
**Lunar Client's Fabric add-on**.

No injection, no launcher patching â€” Lunar loads third-party Fabric mods on 1.16.5+ through
its Fabric add-on, and this is a normal Fabric mod.

## Features

| Feature | What it does |
| --- | --- |
| HUD panel | Coordinates + facing, FPS, ping, biome, held-item durability, free text |
| Keybinds | Any key bound to an action, edge-triggered (fires once per press, never while a screen is open) |
| Chat macros | `chat:/spawn`, `chat:/is go`, any command or message |
| Utilities | Copy coordinates to clipboard, drop held stack / single item, toggle HUD |
| Config | Plain JSON at `config/utilkit.json`, reloadable in game |

## Requirements

- Minecraft **1.21.11** profile in the Lunar Client launcher
- Lunar's **Fabric add-on** enabled for that profile (Lunar only loads external mods on 1.16.5+;
  this will not work on 1.8.9/1.12.2 profiles)
- Fabric API â€” Lunar's add-on ships a bundle, but if the mod fails to appear, add the matching
  `fabric-api` jar to the same mods folder

## Build

On this machine, run `build-utilkit.cmd` from the workspace root (double-click works). It uses the
JDK 21 and Gradle 8.14.3 staged in `tools/` and prints the jar path when it finishes.

Anywhere else, with JDK 21 installed:

```bash
cd utilkit
gradle build          # or ./gradlew build if you generate the wrapper
```

The jar lands in `utilkit/build/libs/utilkit-1.1.0.jar`. Use the one **without** `-sources`.

The first build downloads roughly 400 MB (Minecraft 1.21.11, Yarn mappings, Fabric API), so give it
a few minutes and a working internet connection.

> Note: the DeepSeek Harness shell runs at low integrity, which makes every file report as
> non-writable to the JVM. Java's zip filesystem then refuses to write, and Fabric Loom cannot merge
> the Minecraft jars. That is why the build has to be started from a normal terminal rather than
> from inside the agent shell.

### Different Minecraft version

Edit `gradle.properties` (`minecraft_version`, `yarn_mappings`, `fabric_version`, `loader_version`)
and the `minecraft` range in `fabric.mod.json`. The code uses stable Fabric/vanilla APIs, but
Yarn names shift between minor versions â€” expect to fix a few identifiers.

## Install in Lunar Client

1. Open the launcher, select (or create) a **1.21.11 Fabric** profile.
2. Click the profile's âš™ icon â†’ **Mods** tab.
3. Drag `utilkit-1.1.0.jar` into the window, confirm it is **Enabled**.
4. Launch.

Alternatively, click the folder icon in the Mods tab and copy the jar into that directory.

## Usage

```
/utilkit                         command help
/utilkit hud                     toggle the HUD (also F6 by default)
/utilkit bind <key> <action>     bind a key, e.g. /utilkit bind j chat:/spawn
/utilkit unbind <key>            remove a binding ("all" clears everything)
/utilkit list                    show bindings
/utilkit reload                  re-read config/utilkit.json
```

Key names are Minecraft's own: `j`, `f6`, `space`, `tab`, `key.keyboard.grave`.

Actions:

| Action | Effect |
| --- | --- |
| `chat:<text>` | Sends the text as chat; a leading `/` runs a command |
| `toggle:hud` | Toggles the HUD |
| `copy:coords` | Copies `x y z` to the clipboard |
| `drop:stack` | Drops the held stack |
| `drop:one` | Drops one item from the held stack |

Config example:

```json
{
  "hud": {
    "enabled": true,
    "x": 6,
    "y": 6,
    "background": true,
    "showCoords": true,
    "showFps": true,
    "showPing": true,
    "showBiome": true,
    "showDurability": true,
    "customText": "utilkit"
  },
  "macros": [
    { "keyCode": 74, "action": "chat:/spawn" }
  ]
}
```

## What this deliberately does not do

Server rules come first, and most networks ban **automation** even when the mod itself is
allowed. So this mod does not implement:

- autoclickers, auto-sprint, anti-AFK, or combat/aim assistance
- timed input sequences (press key â†’ click 20 times)
- automatic inventory sorting, which requires simulating dozens of container clicks
- anything that reads game state a normal client cannot see (entity radar, cave finder)

The keybind system is edge-triggered on purpose: one physical press, one action. That keeps it in
the "quality of life / HUD" bucket that Hypixel-style rule sets allow, rather than in the
"macro" bucket they don't. Check the rules of the server you play on anyway.

## Layout

```
src/main/java/dev/bente/utilkit/
  UtilKitClient.java    entrypoint, tick loop, HUD keybind
  UtilKitConfig.java    Gson config load/save
  HudOverlay.java       HUD rendering
  MacroManager.java     keybind polling + action execution
  UtilKitCommands.java  /utilkit command tree
  KeyNames.java         key name <-> GLFW code
  Messages.java         local chat feedback
```

## License

MIT
