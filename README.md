# UtilKit

A client-side Fabric utility mod for Minecraft 1.21.11, designed to run inside **Lunar Client's
Fabric add-on**.

HUD panel, configurable keybinds, chat/command macros, coordinate copying, quick drop.
No injection, no gameplay automation.

Full feature list, commands and Lunar install steps: [`utilkit/README.md`](utilkit/README.md).

## Repo layout

```
utilkit/                  the Gradle project (this is the mod)
.github/workflows/        CI: builds the jar and uploads it as an artifact
build-utilkit.cmd         local Windows build helper (uses tools/)
check-fs.cmd              filesystem diagnostic, not needed for building
```

## Building

Easiest: push this repo, open the **Actions** tab, run *build*, download the `utilkit-jar`
artifact. The jar inside is ready to drop into Lunar Client.

Locally, with JDK 21 installed:

```bash
cd utilkit
gradle build
```

The jar appears at `utilkit/build/libs/utilkit-1.1.0.jar` (take the one without `-sources`).
