
## 2026-05-25 Gate-2 compile validation
- Limited edits to registry/init files only and did not create or alter missing item/block/entity/menu/component implementation classes, because those are Wave 4 deliverables.
- Treated Minecraft mapping/package drift (`ResourceLocation` -> `Identifier`, `net.minecraft.Util` -> `net.minecraft.util.Util`, `critereon` -> `criterion`) as Gate-2 critical import fixes.

## Wave 3 Task 3.1 — 语言 Provider 范围决策
- 语言数据不复制旧 JSON 文件，而是在 `AvaritiaLanguageProvider#addTranslations()` 中按分类显式添加：items、blocks、creative tabs、entities、effects/enchantments、singularities、modes、tooltips、containers/buttons、death messages、JEI/Jade/config/advancement/misc texts。
- 对注册对象优先使用 `.add(registryObject.get(), "Name")`，无法通过注册对象表达的旧版通用 UI/tooltip/config key 使用 `add(String, String)`。

## 2026-05-25 ReloadableServerResourcesMixin 迁移决策

- 不再 shadow `RecipeManager` / `TagManager` / `ServerFunctionLibrary` / `ServerAdvancementManager`，也不再覆写 `listeners()` 的完整返回列表，避免和 NeoForge 26.1.2 的 reload listener 排序事件冲突。
- 选择在 `EventHooks.onResourceReload(...)` 返回后使用 MixinExtras `@ModifyExpressionValue` 插入 `SingularityReloadListener.INSTANCE`。这样保留 NeoForge/其他模组通过 `AddServerReloadListenersEvent` 注册的监听器，同时避免事件构造阶段把 Avaritia listener 当作非法 mixin listener。
- 插入位置选择在排序后列表中的 `RecipeManager` 前；若未来版本找不到 `RecipeManager`，代码带 TODO 并退化为追加到列表末尾。
- 每次重载路径都会重新创建 `SingularityReloadListener.INSTANCE`，保留旧构造器注入“重置 listener 实例”的行为。
## 2026-05-26 - Migration guidance source of truth
- For remaining compile-error triage, use the transformed MC sources under ~/.gradle/caches/neoformruntime/intermediate_results/transformSources_*_output.zip as the authoritative view of the effective 26.1.2 classpath, not generic upstream memory. This source set reflects the actual mapping/package layout seen by the project (GuiGraphicsExtractor, moved projectile packages, render-state generics, removed legacy renderer classes).
