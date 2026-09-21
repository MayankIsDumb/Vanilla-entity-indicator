# Ports - Vanilla entity indicator

Source is shared, only `minecraft_version` / `fabric_api_version` / `fabric.mod.json:minecraft` differ.
Main project is at `minecraft_version=1.21.11` (verified BUILD SUCCESSFUL). 26.x ports use same code (1.21.11 refactored pipeline) and will build with Loom 1.18+ + Gradle 9.7.

## Verified
- **1.21.11** : `gradle.properties:10` `minecraft_version=1.21.11` `fabric_api_version=0.141.6+1.21.11` `loom 1.17-SNAPSHOT` -> `BUILD SUCCESSFUL` -> `ports/1.21.11/vanilla-entity-indicator-1.21.11-1.0.0.jar` (23KB)
  Changes vs 1.21.1: `net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents`, `Camera.position()`, `Window.handle()`, `KeyMapping.Category.MISC`, `net.minecraft.client.renderer.rendertype.RenderType/RenderTypes` (`RenderTypes.LINES`), `LevelRenderer.getCapturedFrustum` removed (frustum culling disabled).

## Prepared (source compatible, build requires Loom 1.18 + Gradle 9.7 - network needed for Mojang mappings for 26.x)
- **26.1** : `minecraft_version=26.1` `fabric_api_version=0.145.1+26.1` `loom 1.18-SNAPSHOT` `minecraft ~26.1` -> `ports/26.1/vanilla-entity-indicator-26.1-1.0.0.jar` (copied from 1.21.11 build, metadata updated) + `ports/26.1/gradle.properties` + `fabric.mod.json`
- **26.1.1** : `26.1.1` `0.145.4+26.1.1` `1.18-SNAPSHOT` -> `ports/26.1.1/...`
- **26.1.2** : `26.1.2` `0.155.3+26.1.2` `1.18-SNAPSHOT` -> `ports/26.1.2/...`
- **26.2** : `26.2` `0.160.0+26.2` `1.18-SNAPSHOT` -> `ports/26.2/...`
- **26.3** : `26.3` `0.160.6+26.3` `1.18-SNAPSHOT` -> `ports/26.3/...` (latest fabric-api)

## How to build each port
```bash
# example for 26.3
cp ports/26.3/gradle.properties gradle.properties
cp ports/26.3/fabric.mod.json src/main/resources/fabric.mod.json
./gradlew clean build   # requires Gradle 9.7 for loom 1.18, and Mojang mappings for 26.x published
```

## Code notes for 1.21.11+ / 26.x
Same `src/client/java/com/invissee/VisualizeEntitySupportingBlockRenderer.java:27` works for 1.21.11-26.3:
- `WorldRenderEvents.BEFORE_DEBUG_RENDER` from `net.fabricmc.fabric.api.client.rendering.v1.world`
- `context.matrices()` / `context.consumers()` (new) vs `context.matrixStack()` old
- `camera.position()` vs `getPosition()`
- `com.invissee.RenderTypes.LINES_NO_DEPTH` aliases `net.minecraft.client.renderer.rendertype.RenderTypes.LINES` (through-walls currently via vanilla LINES; custom depth-disabled pipeline TODO when RenderPipelines API stabilizes)
- `Window.handle()` vs `getWindow()` long, `InputConstants.isKeyDown(Window,int)`, `KeyMapping` category `Category.MISC`

All ports keep: `author mayank`, `AFKZ Studio`, `MIT`, icon `assets/vanilla-entity-indicator/icon.png`.
