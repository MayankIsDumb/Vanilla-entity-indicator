# Ports - Vanilla entity indicator

Per-version build files and sources. Shared code (`InvisSeeConfig`,
`ModMenuIntegration`, mixins, assets) stays in `src/`.

## Layout

- `1.21.11/{gradle.properties,build.gradle,fabric.mod.json}` - 1.21.11 build files
- `12111-src/...` - 1.21.11-only sources (WorldRenderEvents renderer,
  GuiGraphics screens, `RenderTypes.LINES_NO_DEPTH`-free depth-tested lines)
- `26x-build.gradle` - shared 26.x build file (Loom 1.18, Java 25)
- `26x-src/...` - 26.1-26.2 sources (gizmo renderer via `BEFORE_GIZMOS`,
  extractor-model screens)
- `26.3-InvisSeeClient.java` - 26.3 client (input API differs:
  `Type.KEYBOARD` + single-arg `isKeyDown(int)`, left click is button 1)
- `26.1 - 26.3/{gradle.properties,fabric.mod.json}` - versions, deps, modmenu
  (26.1/26.1.1/26.1.2: modmenu 18.0.1, 26.2: 20.0.2, 26.3: 21.0.0-beta.1)

## Version API notes

- 1.21.11: `WorldRenderEvents.BEFORE_DEBUG_RENDER`
  (`fabric.rendering.v1.world`), `KeyBindingHelper`, `displayClientMessage`,
  `GuiGraphics` screens, `mouseClicked(double,double,int)`, left click = 0.
- 26.1-26.2: `LevelRenderEvents.BEFORE_GIZMOS` + `Gizmos.cuboid`,
  `KeyMappingHelper`, `sendOverlayMessage`, extractor screens,
  `MouseButtonEvent`, left click = 0, `setScreenAndShow` only.
- 26.3: same as 26.2 plus `Type.KEYBOARD`, single-arg `isKeyDown(int)`,
  left click = 1 (SDL backend), no Mojang-published mappings (Loom deobf).

## Build all versions

```powershell
./release-all.ps1
```

Jars land in `releases/<mc>/`, `releases/mod-only/` and `ports/<mc>/`.
The tree is left on 1.21.11 (main) afterwards.
