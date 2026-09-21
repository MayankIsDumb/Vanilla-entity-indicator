# Check all versions - shared world + ModMenu

## Shared world
- Dev world: `run/saves/New World` (git-ignored). All `runClient` launches
  share `run/`, so the same world is used everywhere. For PrismLauncher,
  copy it into each instance's `saves/`.
- Config defaults (`run/config/vanilla-entity-indicator.json`):
  enabled, showInvisible, range 128, entities #00FF00 green,
  players #FF0000 red, lineWidth 1.5, recentColors [].

## Per-version checklist (all launched clean)
- [x] 1.21.11 - WorldRenderEvents renderer, GuiGraphics screens, picker drag
- [x] 26.1 - gizmo renderer, extractor screens, picker drag
- [x] 26.1.1 - same as 26.1
- [x] 26.1.2 - same as 26.1
- [x] 26.2 - same as 26.1 (`setScreenAndShow` only)
- [x] 26.3 - same as 26.2 + KEYBOARD/isKeyDown(int), click button 1 (SDL)

## In-game checks
- `G` toggles indicator, `F3+F6` toggles, `O` opens config.
- Config: color buttons open the Lunar-style picker (square + hue + hex +
  presets + recents), Done applies, Cancel discards.
- Boxes are depth-tested: hidden behind walls, visible in view.
- Mods -> Vanilla entity indicator -> gear icon opens the same screen.
