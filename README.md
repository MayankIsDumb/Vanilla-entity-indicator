# Vanilla entity indicator

Fabric port of vanilla debug feature `visualize_entity_supporting_block` (added Dec 9 2025) - shows a green outline on the block directly under every entity, only when visible and even on **invisible** players/mobs.

Based on reel analysis: `instagram.com/reel/DbYYyYdJlK1` / `Video-90547.mp4`

## Features
- Green neon box under every living entity & player within 128 blocks - deduped, frustum-culled
- Depth-tested (occluded by walls, only shows in view) and on invisibility potions
- Toggle with **G** or vanilla-like **F3 + F6**
- Client-only, no server needed

## Author
- **mayank** / **AFKZ Studio** - https://afkz.studio
- License: **MIT**

## Setup
Requires: Minecraft `1.21.1`, Fabric Loader `>=0.19.5`, Fabric API `0.116.17+1.21.1`, Java 21

Build:
```
./gradlew build
```
Jar: `build/libs/vanilla-entity-indicator-1.0.0.jar` -> put in `mods/`

## Credits
Inspired by `camman.18` reel showcasing the vanilla debug option.
