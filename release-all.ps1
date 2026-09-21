# Builds release jars for every Minecraft version, one by one.
# Run from the project root: ./release-all.ps1
# Output: releases/<mc>/, releases/mod-only/ and ports/<mc>/ jars.
# Tree is left on 1.21.11 (main) afterwards.
$ErrorActionPreference = 'Stop'

function Check($v) {
  if ($LASTEXITCODE -ne 0) { throw "BUILD FAILED for $v" }
}

function Collect($v) {
  $jar = 'build/libs/vanilla-entity-indicator-1.0.0.jar'
  Copy-Item $jar "releases/$v/vanilla-entity-indicator-$v-1.0.0.jar" -Force
  Copy-Item $jar "releases/mod-only/vanilla-entity-indicator-$v-1.0.0.jar" -Force
  Copy-Item $jar "ports/$v/vanilla-entity-indicator-$v-1.0.0.jar" -Force
  echo "COLLECTED $v"
}

function Use26xBuild {
  Copy-Item build.gradle.26.3.current.bak build.gradle -Force
  Copy-Item ports/26x-src/com/invissee/InvisSeeClient.java src/client/java/com/invissee/InvisSeeClient.java -Force
  Copy-Item ports/26x-src/com/invissee/VisualizeEntitySupportingBlockRenderer.java src/client/java/com/invissee/VisualizeEntitySupportingBlockRenderer.java -Force
  Copy-Item ports/26x-src/com/invissee/config/InvisSeeConfigScreen.java src/client/java/com/invissee/config/InvisSeeConfigScreen.java -Force
  Copy-Item ports/26x-src/com/invissee/config/ColorPickerScreen.java src/client/java/com/invissee/config/ColorPickerScreen.java -Force
  if (Test-Path src/client/java/com/invissee/RenderTypes.java) {
    Move-Item src/client/java/com/invissee/RenderTypes.java RenderTypes.java.1.21.11.hold -Force
  }
}

function Use12111 {
  Copy-Item gradle.properties.1.21.11.bak gradle.properties -Force
  Copy-Item build.gradle.1.21.11.working.bak build.gradle -Force
  Copy-Item fabric.mod.json.1.21.11.bak src/main/resources/fabric.mod.json -Force
  Copy-Item src-1.21.11-bak/InvisSeeClient.java src/client/java/com/invissee/InvisSeeClient.java -Force
  Copy-Item src-1.21.11-bak/RenderTypes.java src/client/java/com/invissee/RenderTypes.java -Force
  Copy-Item src-1.21.11-bak/VisualizeEntitySupportingBlockRenderer.java src/client/java/com/invissee/VisualizeEntitySupportingBlockRenderer.java -Force
  Copy-Item src-1.21.11-bak/config/InvisSeeConfigScreen.java src/client/java/com/invissee/config/InvisSeeConfigScreen.java -Force
  Copy-Item src-1.21.11-bak/config/ColorPickerScreen.java src/client/java/com/invissee/config/ColorPickerScreen.java -Force
  if (Test-Path RenderTypes.java.1.21.11.hold) { Remove-Item RenderTypes.java.1.21.11.hold -Force }
}

function Build26($v) {
  echo "=== BUILDING $v ==="
  Copy-Item "ports/$v/gradle.properties" gradle.properties -Force
  Copy-Item "ports/$v/fabric.mod.json" src/main/resources/fabric.mod.json -Force
  Use26xBuild
  if ($v -eq '26.3') {
    Copy-Item ports/26.3-InvisSeeClient.java src/client/java/com/invissee/InvisSeeClient.java -Force
  }
  ./gradlew clean build
  Check $v
  Collect $v
}

Build26 '26.1'
Build26 '26.1.1'
Build26 '26.1.2'
Build26 '26.2'
Build26 '26.3'

echo '=== BUILDING 1.21.11 (final, tree stays here) ==='
Use12111
./gradlew clean build
Check '1.21.11'
Collect '1.21.11'

echo 'ALL RELEASE JARS DONE'
