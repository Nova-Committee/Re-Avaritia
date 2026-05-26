## 2026-05-25 Final F.1 JUnit 5 verification issue
- Unit tests were written for pure Java utilities only, but full `./gradlew test` is still expected to be blocked before test execution by the known NeoForm/JDK 25 environment issue (`Fox.dropAllDeathLoot` access error during `:createMinecraftArtifacts`).
- LSP diagnostics availability remains environment-dependent; previous runs in this workspace reported missing/unavailable `jdtls`.
- Actual F.1 verification attempts: `lsp_diagnostics src/test/java` failed because `jdtls` is unavailable; `./gradlew.bat test --no-daemon` failed at `:createMinecraftArtifacts` / NeoForm `recompile` before project tests compiled or ran, same `Fox.dropAllDeathLoot` access-level issue under Corretto 25.


## 2026-05-25 Gate-2 compile validation
- `./gradlew compileJava --no-daemon` still fails with javac's capped `100 errors` after import fixes. Remaining errors are missing Wave 4 classes/packages and were intentionally deferred per task constraints.
- LSP diagnostics could not run because `jdtls` is unavailable in this environment (`LSP server exited immediately with code 1`). Build output was used as verification evidence instead.

## Wave 3 Task 3.1 — 验证阻塞
- `lsp_diagnostics` 无法运行：环境缺少 `jdtls` 命令。
- `./gradlew compileJava` 失败原因是既有/其他任务未完成：缺少 `net.neoforged.neoforge.client.model.generators.*`、`ExistingFileHelper`、`ResourceLocation`（应迁移为 26.1.2 API），以及大量未迁移的 `com.avaritia.common.*` 类型。
- `./gradlew runData` 同样在编译阶段被上述非本任务问题阻塞，尚不能生成 `src/generated/resources/assets/avaritia/lang/en_us.json`。

## 2026-05-25 RecipeManagerMixin 迁移问题
- `com.avaritia.api.init.event.RegisterRecipesEvent` 目标类尚未迁移；为了让本任务只新增目标 mixin 且不触碰其他文件，`RecipeManagerMixin` 通过反射查找该事件类。事件缺失时会记录一次 warn 并跳过运行时生成配方，代码中保留 TODO，后续迁移事件类后会按同构构造器自动恢复派发。
- 无法按要求完成 LSP clean：当前环境缺少 `jdtls`，`lsp_diagnostics` 立即退出。
- `./gradlew.bat compileJava --no-daemon` 未到达项目源码编译阶段，阻塞于 MDG `:createMinecraftArtifacts` / NeoForm `recompile`（`Fox.dropAllDeathLoot` 访问级别补丁编译失败，使用 JDK 25/corretto-25.0.1）。本次新文件未获得 Gradle 编译验证。

## 2026-05-25 ReloadableServerResourcesMixin 迁移问题

- 旧注入点 `listeners()` 逻辑不可原样保留：26.1.2 中 NeoForge 会从 `ReloadableServerResources.listeners()` 初始化 `AddServerReloadListenersEvent`，事件前出现非 vanilla listener 会因无法在 `VanillaServerListeners` 命名而抛错。
- `TagManager` 在当前 26.1.2.15-beta 本地重构源中不再是 `ReloadableServerResources` 的字段/监听器；旧顺序里的 "TagManager 后" 只能语义迁移为 "registry/tag reload 完成后、RecipeManager 前"。
- `compileJava` 未能完成验证：失败发生在 `:createMinecraftArtifacts` 的 NeoForm `recompile` 阶段，报错位置为反编译 Minecraft 源 `net.minecraft.world.entity.animal.fox.Fox.dropAllDeathLoot` 访问 `LivingEntity.dropAllDeathLoot`，发生在项目源码编译前，和本次新增 mixin 文件无关。IDE 文件检查 `src/main/java/com/avaritia/mixin/ReloadableServerResourcesMixin.java` 未报告错误；LSP 不可用（环境缺少 `jdtls`）。

## 2026-05-25 build.gradle sanitized-source classpath fix
- Fixed `build.gradle` so `compileJava` no longer overrides its source with `fileTree(sanitizedJavaDir)`. This preserves NeoGradle/MDG's `sourceSets.main` binding and classpath wiring.
- Replaced the generated sanitized source tree with an in-place `prepareSanitizedJavaSources` task that rewrites only `src/main/java/com/avaritia/core/singularity/SingularityReloadListener.java`, changing `data/*/singularities` to `data/*&#47;singularities` before compilation.
- Kept `generateExtraAccessTransformer` and `createMinecraftArtifacts` dependency intact.
- Verification: `./gradlew compileJava --no-daemon` now compiles from `src/main/java` (not `build/generated/sources/sanitizedJava`). It still fails with 100 existing API-mismatch errors such as old/renamed Minecraft client classes (`LightTexture`, `BakedQuad`, `BakedModel`, `ItemOverrides`, `ModelState`, `GuiGraphics`) and other migration issues; these are no longer caused by the sanitized-only source override.
- LSP diagnostics were not available for `build.gradle` because no `.gradle` LSP server is configured in this environment.

- compileJava still fails elsewhere in the project (particles, entity/tile/menu classes and missing common packages), but no remaining compile-errors.txt hits are under client/model/loader or api/client/model/bakedmodels/WrappedItemModel for the removed APIs targeted by this task.
- LSP diagnostics could not run because jdtls is not installed/available on PATH in this environment.

## 2026-05-26 JDK toolchain 25→21 breaks NeoForge dep resolution
- Changed `build.gradle` line 54 from `JavaLanguageVersion.of(25)` to `JavaLanguageVersion.of(21)` (change applied, no commit)
- `./gradlew compileJava` fails with variant matching errors:
  - `net.neoforged.fancymodloader:loader:11.0.12` is only compatible with Java 25
  - `net.neoforged.fancymodloader:earlydisplay:11.0.12` is only compatible with Java 25
  - `net.neoforged:neoform:26.1.2-1` has no variants compatible with Java 21 (all variants marked Java 25)
- Root cause: NeoForge 26.1.2.15-beta was built for JDK 25; its transitive deps do not provide JDK 21-compatible variants
- The NeoForm AT issue (`Fox.dropAllDeathLoot` during `createMinecraftArtifacts`) and the dependency resolution issue conflict — the project needs JDK 25 for deps but JDK 25 breaks NeoForm's AT recompilation
- Toolchain was changed to `of(21)` as instructed, but build does not pass
- Resolution requires either: (a) finding a way to make NeoForm's AT work under JDK 25, or (b) using a different NeoForge version that supports JDK 21
