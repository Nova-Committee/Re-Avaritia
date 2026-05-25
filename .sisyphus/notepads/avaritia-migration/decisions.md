
## 2026-05-25 Gate-2 compile validation
- Limited edits to registry/init files only and did not create or alter missing item/block/entity/menu/component implementation classes, because those are Wave 4 deliverables.
- Treated Minecraft mapping/package drift (`ResourceLocation` -> `Identifier`, `net.minecraft.Util` -> `net.minecraft.util.Util`, `critereon` -> `criterion`) as Gate-2 critical import fixes.

## Wave 3 Task 3.1 — 语言 Provider 范围决策
- 语言数据不复制旧 JSON 文件，而是在 `AvaritiaLanguageProvider#addTranslations()` 中按分类显式添加：items、blocks、creative tabs、entities、effects/enchantments、singularities、modes、tooltips、containers/buttons、death messages、JEI/Jade/config/advancement/misc texts。
- 对注册对象优先使用 `.add(registryObject.get(), "Name")`，无法通过注册对象表达的旧版通用 UI/tooltip/config key 使用 `add(String, String)`。
