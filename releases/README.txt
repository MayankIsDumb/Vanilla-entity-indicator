Vanilla entity indicator 1.0.0 - by mayank / AFKZ Studio (MIT)
=============================================================
Shows a colored outline on the block under every entity (works on
invisible players too). Port of vanilla's Dec 9 2025 debug feature
"visualize_entity_supporting_block".

WHAT'S IN EACH VERSION FOLDER
- vanilla-entity-indicator-<mc>-1.0.0.jar ... the mod (put in mods/)
- fabric-api-<ver>.jar .................... required dependency (put in mods/)
- modmenu-<ver>.jar ....................... optional, for Mods-list + config button (put in mods/)
- icon.png (in releases root) ............. the mod icon (128x128)

REQUIREMENTS PER VERSION
- 1.21.11 : Fabric Loader >= 0.15.0 (tested 0.19.5), Java 21+
- 26.1    : Fabric Loader >= 0.15.0 (tested 0.19.5), Java 25+
- 26.1.1  : same as 26.1
- 26.1.2  : same as 26.1
- 26.2    : same as 26.1
- 26.3    : same as 26.1

INSTALL (PrismLauncher / Fabric launcher)
1. Create an instance for the matching Minecraft version with Fabric Loader.
2. Copy the THREE jars from that version's folder into the instance's mods/ folder.
   (ModMenu is optional - the mod works without it. Open the config with O.)
3. Launch. Singleplayer world from dev testing is NOT included - use your own world.

KEYS (changeable in Controls)
- G ......... toggle indicator on/off
- O ......... open config screen
- F3+F6 ..... toggle (same as vanilla debug)

CONFIG SCREEN (O key, or Mods -> Vanilla entity indicator -> gear icon)
- Enabled / Show Invisible / Only Players
- Other Entities color + Player color buttons open a Lunar-style color
  picker (HSB square + hue bar + hex field + preset/recent swatches)
- Range (16-256), Line Width (0.5-5.0)
- Done-Save / Reset Defaults

SETTINGS ARE SAVED automatically (on Done, on screen close, on every toggle)
to config/vanilla-entity-indicator.json and reloaded on every launch.
Proof: change Range to 256 and a color, close the game, reopen - values persist.

DEFAULTS: mobs green #00FF00, players red #FF0000. Boxes are depth-tested
(occluded by walls, only visible in view).

1.21.11 uses the classic outline renderer; 26.x uses vanilla's own gizmo
system (same path as vanilla's support-block visualizer) so colors match.
