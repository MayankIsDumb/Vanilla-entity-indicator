# Check all versions - same world + ModMenu

## Shared world
- Dev world: `run/saves/New World` (level.dat 2528B, seed from level.dat) + backup `run/saves/New World Backup Check`
- Config reset to defaults for checking: `run/config/vanilla-entity-indicator.json`
  `{"enabled":true,"showInvisible":true,"onlyPlayers":false,"range":128,"entityColor":-16711936 (green #00FF00),"playerColor":-65536 (red #FF0000),"lineWidth":1.5,"throughWalls":true}`
- All `gradlew runClient` launches share `run/` so same world. For PrismLauncher, copy `run/saves/New World` into each instance's `saves/`.

## 1.21.11 - launched now
- Jar: `ports/1.21.11/vanilla-entity-indicator-1.21.11-1.0.0.jar` (31477B, includes ModMenu entrypoint + see-through fix + compact config)
- ModMenu: `ports/1.21.11/modmenu-17.0.0.jar` (1115809B, v17.0.0 for 1.21.11 - stable, fixes TitleScreen adjustRealmsHeight crash from 13.0.2)
- Launch: `.\gradlew.bat runClient` already started in background (java pids). Check window: Minecraft 1.21.11 + Fabric Loader 0.19.5, 52 mods.
- In-game: `G` toggle, `F3+F6` toggle, `O` opens config (vanilla screen, ModMenu button also works: Mods -> Vanilla entity indicator -> gear icon).
- Config page: Enabled ON, See Through ON (NO_DEPTH_TEST pipeline), Show Invisible ON, Only Players OFF, Other #00FF00 green, Player #FF0000 red, Range 128, Line Width 1.5, Done-Save.

## 26.x - cannot runClient via gradle dev (Mojang published no client_mappings for 26.x - piston-meta downloads only client/server, no client_mappings vs 1.21.11 which has them). Loom fails `Failed to find official mojang mappings for 26.1` even with loom 1.18.2 + Gradle 9.7.
## Use PrismLauncher / Fabric Launcher manually with same world:
- 26.1: `ports/26.1/vanilla-entity-indicator-26.1-1.0.0.jar` + `ports/26.1/modmenu-18.0.1.jar` (859754B, v18.0.1 for 26.1/26.1.1/26.1.2) + Fabric Loader 0.19.5 + Fabric API 0.145.1+26.1 + Java 25 (jdk-25.0.4+7). Copy `run/saves/New World` to instance saves.
- 26.1.1: `ports/26.1.1/vanilla-entity-indicator-26.1.1-1.0.0.jar` + `modmenu-18.0.1.jar` + Fabric API 0.145.4+26.1.1 + Java 25
- 26.1.2: `ports/26.1.2/vanilla-entity-indicator-26.1.2-1.0.0.jar` + `modmenu-18.0.1.jar` + Fabric API 0.155.3+26.1.2 + Java 25
- 26.2: `ports/26.2/vanilla-entity-indicator-26.2-1.0.0.jar` + `ports/26.2/modmenu-20.0.2.jar` (616610B, v20.0.2 for 26.2) + Fabric API 0.160.0+26.2 + Java 25
- 26.3: `ports/26.3/vanilla-entity-indicator-26.3-1.0.0.jar` + `ports/26.3/modmenu-21.0.0-beta.1.jar` (856788B, v21.0.0-beta.1 for 26.3/26.3-rc-1) + Fabric API 0.160.6+26.3 + Java 25

All port jars are intermediary (remapped, version-agnostic) built from same fixed source (RenderTypes LINES_NO_DEPTH NO_DEPTH_TEST + setLineWidth + compact config + ModMenuIntegration), with fabric.mod.json patched inside (`minecraft ~<mc>`, `java >=25` for 26.x).

## What to check in each version
1. Title screen -> Mods button exists (ModMenu loaded)
2. Mods -> Vanilla entity indicator -> config icon opens same O-screen (or press O in game)
3. Singleplayer -> New World (same world) -> G ON, O -> set Other green / Player red, See Through ON
4. Spawn mobs + second account / invisible potion -> green boxes under mobs through walls, red under players, through blocks when ON, depth-tested when OFF
