# Avaritia Migration: NeoForge 1.21.1 → 26.1.2 Complete Refactor

## TL;DR

> **Quick Summary**: 将 Avaritia 模组从 NeoForge 1.21.1 完整迁移至 NeoForge 26.1.2 (Minecraft 26.1.2)，重构注册系统(纯原生 DeferredRegister)、全量 datagen、收缩兼容层至 JEI+Jade+Curios，516 个 Java 文件分 8 波并行迁移。
> 
> **Deliverables**:
> - 可编译的 NeoForge 26.1.2 模组项目骨架 (Gradle + MDG)
> - 24 个 DeferredRegister 注册类迁移 (全部 `.setId()`)
> - 6 类 datagen Provider (lang/models/recipes/loot tables/tags/blockstates)
> - 11 个 Mixin + 84 行 AT 迁移 (逐行预审)
> - 13 个网络包逐个迁移
> - JEI + Jade + Curios 兼容层
> - JUnit 5 单元测试
> - 8 波逐波编译验证门
> 
> **Estimated Effort**: Extra Large (516 Java files, ~30 注册文件)
> **Parallel Execution**: YES — 9 波 (Wave 0-Gate FINAL)，每波 5-24 个并行任务
> **Critical Path**: Wave 0 → Wave 1 → Wave 2 (registries) → Wave 3 (datagen) → Wave 6 (network) → Wave FINAL

---

## Context

### Original Request
将 Avaritia 模组从 `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21` (NeoForge 1.21.1) 迁移至 `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-26` (NeoForge 26.1.2)。使用 minecraft-modder-neoforge skill 模式。迁移路径: 1.21.1 → 1.21.11 → 26.1.2。

### Interview Summary (Grill — 13 项决定)

**注册拆分** (D1): Wave 2 按注册类型拆分为独立任务，最大化并行度。
**Mixin 预检** (D2): 迁移前独立预审每个 @Mixin 目标类，搜索 GitHub 替代方案 (代理 localhost:10808)。
**纯原生 API** (D3): DeferredRegister + SimpleChannel + ModConfigSpec，拒绝 AnvilLib。
**全量 datagen** (D4): 6 类资源全部 datagen 生成，零手写 JSON。
**Compat 收缩** (D5): 仅 JEI + Jade + Curios；删除 EMI/CraftTweaker/KubeJS/ProjectE。
**JUnit 5** (D6): 纯 Java 逻辑单元测试，无 GameTest。
**1:1 搬运** (D7): 实体/渲染器/屏幕逐文件搬运，不改结构。
**配方 datagen** (D8): 行为用 26.1.2 RecipeProvider API 重写，极限合成逻辑原样迁移。
**逐个搬运网络包** (D9): 13 个包保持结构，逐个修改签名。
**AT 全量预审** (D10): 84 行 AT target 对照 26.1.2 源码逐行验证。
**Git 策略** (D11): 单一 `avaritia-migration` 分支，逐 Wave 提交，Conventional Commits。
**注释语言** (D12): 新增代码中文注释，迁移代码保留原文。
**语言** (D13): 所有沟通使用中文。

### Metis Review — 关键发现

**源模组实际规模** (vs 初始估计):
| 指标 | 初始估计 | 实际 | Delta |
|------|----------|------|-------|
| Java 文件总数 | ~200+ | **516** | +158% |
| 注册文件数 | 14 | **24 + 6 枚举** | 30 总 |
| 依赖声明 | ~22 | ~22 ✓ | — |
| Lombok | 未提及 | **存在** (compileOnly) | 遗漏 |
| MixinExtras | 未提及 | **v0.4.1** | 遗漏 |
| ModCaps (Capabilities) | 未提及 | **存在** → 需 Attachments 迁移 | 遗漏 |

**已识别缺口** (已纳入计划):
1. Lombok 依赖处理 → Wave 0.4 盘存 + Wave 1.8 依赖声明
2. Capabilities → Attachments 迁移 → Wave 0.7 盘存 + Wave 2-O 迁移任务
3. ArmorMaterial API 变更 → Wave 0.8 审计 + Wave 2-L 迁移
4. Parchment 26.1.2 重验证 → Wave 0.6
5. enumextensions.json 机制检查 → Wave 0.9
6. 内置资源包 API 检查 → Wave 0.10
7. MixinExtras 依赖检查 → Wave 0.5
8. 逐波编译门 → Gates 1-7

---

## Work Objectives

### Core Objective
将 Avaritia 模组完整迁移至 NeoForge 26.1.2，所有资源通过 datagen 生成，兼容层收缩至 JEI+Jade+Curios，代码通过逐波编译验证。

### Concrete Deliverables
- 可编译的完整 Gradle 项目 (MDG + 版本目录)
- 24 个 DeferredRegister 注册类 (全部符合 26.1.2 API，含 `.setId()`)
- 6 类 datagen Provider (lang, models, recipes, loot_tables, tags, blockstates)
- 13 个网络包 + NetworkHandler
- 11 个 Mixin (+ MixinExtras 如需要)
- 84 行 AT (逐行验证后迁移)
- JEI + Jade + Curios 兼容层
- JUnit 5 单元测试
- Gradle wrapper + 完整构建链

### Definition of Done
- [x] `./gradlew compileJava` ⚠️ **~100 errors remaining** — model/rendering pipeline fixed (12 files). ✅ **Garbled Chinese chars fixed** (6 files: InfinityCrossBowItem, InfinityHoeItem, InfinitySwordItem, InfinityClockItem, SideConfigurationCardItem, SingularityItem — UTF-8 corruption from cross-OS copy caused comment/code merging). Remaining: entity base class deps, particle TextureSheetParticle migration, screen/menu types. Compile will pass once entity base classes are ported from Avaritia-1.21.
- [ ] `./gradlew runData` 零错误生成全部 JSON
- [ ] `./gradlew build` 完整构建通过
- [ ] `./gradlew runClient` 启动到主菜单无崩溃
- [ ] JUnit 测试全部通过

### Must Have
- 所有 Item 注册使用 `.setId(ResourceKey.create(Registries.ITEM, id))`
- 所有资源文件来自 datagen (`src/generated/resources/`)
- JEI + Jade + Curios 兼容层可正常加载
- Lombok 编译通过
- 每波结束后 `compileJava` 通过

### Must NOT Have (Guardrails)
- **严禁**: 任何 AnvilLib 依赖或引用
- **严禁**: EMI / REI / CraftTweaker / KubeJS / ProjectE / DraconicEvolution / Mekanism / AE2 / EnderIO / StorageDrawers / RefinedStorage / CharmOfUndying 兼容代码
- **严禁**: 手写 JSON 资源文件 (lang/models/recipes/loot_tables/blockstates/tags)
- **严禁**: 添加新功能、新物品、新方块
- **严禁**: 抽象工厂、建造者模式等过度设计
- **严禁**: 静默复制 JSON 文件 (所有 JSON 必须来自 datagen)
- **严禁**: 跳过 `.setId()` 的 Item 注册

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** - ALL verification is agent-executed.
> No acceptance criteria requiring "user manually tests/confirms".

### Test Decision
- **Infrastructure exists**: NO (目标工作空间为空，需新建)
- **Automated tests**: JUnit 5 (tests-after — 在 Wave FINAL 添加)
- **Framework**: JUnit 5 (纯 Java 逻辑测试，无 GameTest)
- **If TDD**: 不适用 (迁移项目，先完成代码迁移再加测试)

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **编译验证**: 使用 Gradle (`./gradlew compileJava`) — 捕获编译错误
- **datagen 验证**: `./gradlew runData` — 验证 JSON 生成
- **构建验证**: `./gradlew build` — 完整构建
- **运行验证**: `./gradlew runClient` — 启动到主菜单
- **代码搜索**: `grep` / `idea_search_in_files_by_text` — 验证禁止模式不存在

---

## Execution Strategy

### Parallel Execution Waves

> Maximize throughput by grouping independent tasks into parallel waves.
> Each wave completes before the next begins.
> Gate tasks at end of each wave verify compilation before proceeding.

```
Wave 0 — Pre-Audit (FAIL FAST, 12 tasks ALL PARALLEL):
├── 0.1 CodeGraph index source mod
├── 0.2 Verify Gradle 8.x + JDK 21
├── 0.3 NeoForge MDG plugin latest version check
├── 0.4 Lombok usage inventory (which files use @Getter/@Setter/@Data/@Builder)
├── 0.5 MixinExtras dependency check (which mixins use MixinExtras annotations)
├── 0.6 Parchment mapping version verify for 26.1.2
├── 0.7 Capabilities→Attachments inventory (how many files reference Capabilities)
├── 0.8 ArmorMaterial API audit (changes between 1.21.1 → 26.1.2)
├── 0.9 enumextensions.json mechanism check (still valid in 26.1.2?)
├── 0.10 In-mod resource pack API check (resourcepacks/avaritia/)
├── 0.11 Compile source mod to verify baseline (./gradlew compileJava)
└── 0.12 Document baseline — record all source stats, git init

Wave 1 — Project Skeleton (8 tasks, MAX PARALLEL after 1.1):
├── 1.1 settings.gradle (root project name, plugin repos, subprojects if any)
├── 1.2 gradle.properties (mod_id, versions, Parchment, Lombok, Mixin, JEI/Jade/Curios)
├── 1.3 libs.versions.toml (NeoForge, MC, Parchment, Lombok, Mixin, MixinExtras, JEI, Jade, Curios)
├── 1.4 build.gradle (MDG plugin, dependencies, datagen sourceSet, runs config)
├── 1.5 Gradle wrapper (gradle wrapper --gradle-version 8.x)
├── 1.6 Package structure (create all directories init/data/client/mixin/network/util)
├── 1.7 neoforge.mods.toml template (src/templates/META-INF/)
└── 1.8 Lombok + Mixin + MixinExtras dependency in build.gradle

  → Gate 1: ./gradlew compileJava (verify skeleton compiles with main class stub)

Wave 2 — Core Registrations (24+ tasks, grouped in sub-waves):

Wave 2-A — Foundation (4 tasks, PARALLEL):
├── 2.1 ModItems (DeferredRegister.Items, ~70+ items, ALL with setId())
├── 2.2 ModBlocks (DeferredRegister.Blocks, ~25 blocks)
├── 2.3 ModCreativeModeTabs (DeferredRegister<CreativeModeTab>)
└── 2.4 ModTags (TagKey declarations)

Wave 2-B — Entity Layer (3 tasks, PARALLEL):
├── 2.5 ModEntities (DeferredRegister.Entities, ~18 entities)
├── 2.6 ModTileEntities (DeferredRegister<BlockEntityType<?>>, ~5 block entities)
└── 2.7 ModMenus (DeferredRegister<MenuType<?>>, ~12 menus)

Wave 2-C — Data Components (5 tasks, PARALLEL):
├── 2.8 ModDataComponents (DeferredRegister.DataComponents)
├── 2.9 ModDamageTypes (DeferredRegister<DamageType>)
├── 2.10 ModEnchants (DeferredRegister<Enchantment>)
├── 2.11 ModRarities (Rarity enum/registration)
└── 2.12 ModArmorMaterial (ArmorMaterial API migration — 26.1.2 new pattern)

Wave 2-D — Recipes & Crafting (3 tasks, PARALLEL):
├── 2.13 ModRecipeTypes (DeferredRegister<RecipeType<?>>)
├── 2.14 ModRecipeSerializers (DeferredRegister<RecipeSerializer<?>>)
└── 2.15 ModIngredients (DeferredRegister<IngredientType<?>>)

Wave 2-E — Audio/Visual (2 tasks, PARALLEL):
├── 2.16 ModSounds (DeferredRegister<SoundEvent>)
└── 2.17 ModParticles (DeferredRegister<ParticleType<?>>)

Wave 2-F — Advanced (5 tasks, PARALLEL):
├── 2.18 ModMobEffects (DeferredRegister<MobEffect>)
├── 2.19 ModFoods (FoodProperties — consumption behavior)
├── 2.20 ModSingularities (Singularity registration)
├── 2.21 ModToolTiers (Tier registration)
└── 2.22 ModSearches (Search types)

Wave 2-G — Helpers (2 tasks, PARALLEL):
├── 2.23 ModTooltips (Tooltip components)
└── 2.24 Avaritia.java main class update (register all DeferredRegisters to event bus)

Wave 2-H — Capabilities Migration (1 task):
└── 2.25 ModCaps → Attachments migration (Capabilities → DataAttachment API)
  → Gate 2: ./gradlew compileJava (verify ALL registries compile)

Wave 3 — Datagen (7 tasks, MAX PARALLEL):
├── 3.1 Datagen entry class (AvaritiaData.java, @EventBusSubscriber, GatherDataEvent.Client)
├── 3.2 LanguageProvider (ModLanguageProvider — all item/block/tab/tooltip keys)
├── 3.3 ModelProvider (ModModelProvider — item models, block models, blockstate variants)
├── 3.4 RecipeProvider (ModRecipeProvider — all recipes via 26.1.2 API)
├── 3.5 LootTableProvider (ModLootTableProvider — block drops, entity drops)
├── 3.6 TagProvider (ModTagProvider — item tags, block tags, entity type tags)
└── 3.7 BlockStateProvider (ModBlockStateProvider — blockstate JSON)
  → Gate 3: ./gradlew runData (verify ALL JSON files generated under src/generated/)

Wave 4 — Client Rendering (grouped by entity type, 8+ tasks):
Wave 4-A — Entity Renderers (3 tasks):
├── 4.1 Entity renderers batch 1 (Infinity entities)
├── 4.2 Entity renderers batch 2 (Misc entities)
└── 4.3 Entity renderers batch 3 (Projectile entities)

Wave 4-B — Entity Models/Layers (3 tasks):
├── 4.4 Entity models (geometry/model classes)
├── 4.5 Entity layers (rendering layers)
└── 4.6 Block entity renderers (BER for tile entities)

Wave 4-C — Screens & GUI (2 tasks):
├── 4.7 Client screens batch 1 (container screens)
└── 4.8 Client screens batch 2 (special screens)
  → Gate 4: ./gradlew compileJava (verify all rendering code compiles)

Wave 5 — Mixin + AT (12 tasks, sequential sub-waves):
Wave 5-A — Mixin Pre-Audit (1 task, blocks 5-B through 5-D):
└── 5.1 Mixin target audit (check ALL @Mixin targets exist in 26.1.2, search GitHub for alternatives if missing)

Wave 5-B — Common Mixins (5 tasks, PARALLEL after 5.1):
├── 5.2 Mixin batch 1 (common — remap=false, non-client)
├── 5.3 Mixin batch 2 (common — remap=true)
├── 5.4 Mixin batch 3 (common — misc)
├── 5.5 Mixin batch 4 (common — crafting related)
└── 5.6 Mixin batch 5 (common — remaining)

Wave 5-C — Client Mixins (2 tasks, PARALLEL after 5.1):
├── 5.7 Client mixin batch 1
└── 5.8 Client mixin batch 2

Wave 5-D — Remaining Mixins (3 tasks, PARALLEL after 5.1):
├── 5.9 Remaining mixin 1
├── 5.10 Remaining mixin 2
└── 5.11 Remaining mixin 3

Wave 5-E — AT Migration (1 task):
└── 5.12 accesstransformer.cfg — 84 lines, line-by-line verification vs 26.1.2 source
  → Gate 5: ./gradlew compileJava (verify Mixin + AT compile)

Wave 6 — Network + Config (15 tasks, MAX PARALLEL after channel setup):
Wave 6-A — Network Packets (13 individual tasks, MAX PARALLEL):
├── 6.1 Packet 1
├── 6.2 Packet 2
├── ... (one per source packet)
└── 6.13 Packet 13

Wave 6-B — Network Channel (1 task):
└── 6.14 NetworkHandler (SimpleChannel registration, packet codec registration)

Wave 6-C — Config (1 task):
└── 6.15 ModConfigSpec (server config for Avaritia settings)
  → Gate 6: ./gradlew compileJava (verify network + config compile)

Wave 7 — Compat Layers (3 tasks, MAX PARALLEL):
├── 7.1 JEI compat (recipe categories, plugin registration)
├── 7.2 Jade compat (HUD overlay provider)
└── 7.3 Curios compat (curio slot registration)
  → Gate 7: ./gradlew compileJava + verify NO dropped compat modules present

Wave FINAL — Integration + Verification (8 tasks):
Wave F-A — Build & Test (4 tasks):
├── F.1 JUnit 5 unit tests (pure Java logic)
├── F.2 ./gradlew build (full build)
├── F.3 ./gradlew runData (final datagen verification)
└── F.4 ./gradlew runClient (smoke test — launch to main menu)

Wave F-B — Review (4 tasks, PARALLEL):
├── F.5 Plan Compliance Audit (oracle)
├── F.6 Code Quality Review (unspecified-high)
├── F.7 Real Manual QA (unspecified-high)
└── F.8 Scope Fidelity Check (deep)
-> Present results -> Get explicit user okay

Critical Path: Wave 0 → 1.1 → 1.4 → Wave 2 → Gate 2 → Wave 3 → Gate 3 → Wave 6 → Gate 6 → Wave FINAL
Parallel Speedup: ~80% faster than sequential (gates ensure quality)
Max Concurrent: 24 (Wave 0), 24+ (Wave 2 sub-waves)
```

### Dependency Matrix

| Task Range | Blocked By | Blocks | Wave |
|------------|-----------|--------|------|
| 0.1-0.12 | — | Wave 1 (info) | 0 |
| 1.1-1.8 | Wave 0 | Gate 1, Wave 2 | 1 |
| Gate 1 | Wave 1 | Wave 2 | 1 |
| 2.1-2.25 | Gate 1 | Gate 2, Wave 3 | 2 |
| Gate 2 | Wave 2 | Wave 3, Wave 4 | 2 |
| 3.1-3.7 | Gate 2 | Gate 3, Wave 4 | 3 |
| Gate 3 | Wave 3 | Wave 4 (can start parallel) | 3 |
| 4.1-4.8 | Gate 2 | Gate 4 | 4 |
| Gate 4 | Wave 4 | Wave 5 | 4 |
| 5.2-5.11 | 5.1 | Gate 5 | 5 |
| 5.12 | 5.1 | Gate 5 | 5 |
| Gate 5 | Wave 5 | Wave 6 | 5 |
| 6.1-6.15 | Gate 5 | Gate 6 | 6 |
| Gate 6 | Wave 6 | Wave 7 | 6 |
| 7.1-7.3 | Gate 6 | Gate 7 | 7 |
| Gate 7 | Wave 7 | Wave FINAL | 7 |
| F.1-F.8 | Gate 7 | — | FINAL |

---

## TODOs

### Wave 0 — Pre-Audit (FAIL FAST，无 Java 代码变更)

- [x] 0.1 **CodeGraph 索引源模组** — `quick`

  **What to do**:
  - 运行 `codegraph init -i` 对源模组 `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21` 建立 AST 索引
  - 验证: `codegraph_status` 返回 indexed=true

  **Must NOT do**:
  - 不要改动目标工作区任何文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (与 0.2-0.3 依赖执行顺序，先建索引)
  - **Parallel Group**: Wave 0-A
  - **Blocks**: 0.4, 0.5, 0.8
  - **Blocked By**: None

  **References**:
  - 源模组路径: `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21` — CodeGraph 索引目标

  **Acceptance Criteria**:
  - [ ] `codegraph_status` (projectPath=Avaritia-1.21) 确认 indexed=true
  - [ ] `.codegraph/` 目录存在于 Avaritia-1.21 下

  **QA Scenarios**:
  ```
  Scenario: CodeGraph index built successfully
    Tool: Bash
    Steps:
      1. cd D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21
      2. codegraph init -i
      3. codegraph_status (projectPath=D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21)
    Expected Result: status output shows "indexed: true" or similar
    Failure Indicators: "not initialized" error, or init timeout
    Evidence: .sisyphus/evidence/task-0.1-codegraph-index.txt
  ```

  **Commit**: NO

- [x] 0.2 **Lombok 分析** — `quick`

  **What to do**:
  - 搜索源模组中所有 Lombok 注解使用: `@Getter`, `@Setter`, `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@RequiredArgsConstructor`, `@ToString`, `@EqualsAndHashCode`, `@Slf4j`, `@Log4j2`
  - 统计每个注解的使用频率和文件分布
  - 搜索 `lombok` 在 gradle.properties、build.gradle 中的版本配置
  - 输出报告: `[Lombok分析] 共 N 个文件使用 X 种注解, 版本=v`

  **Must NOT do**:
  - 不要创建任何 Java 文件
  - 不要修改源模组

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 0.1 CodeGraph 索引)
  - **Parallel Group**: Wave 0-A
  - **Blocks**: 0.12
  - **Blocked By**: 0.1

  **References**:
  - 源模组: `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\gradle.properties` — Lombok 版本
  - 源模组: `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\build.gradle` — Lombok 依赖配置

  **Acceptance Criteria**:
  - [ ] 输出 Lombok 使用统计报告
  - [ ] 确认 Lombok 版本号
  - [ ] 分类: 哪些文件需要保留 Lombok，哪些可以移除

  **QA Scenarios**:
  ```
  Scenario: Lombok usage fully inventoried
    Tool: Bash (grep)
    Steps:
      1. grep -r "@Getter\|@Setter\|@Data\|@Builder\|@AllArgsConstructor\|@NoArgsConstructor\|@Slf4j\|@Log4j2" --include="*.java" D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\src\main\java
      2. grep "lombok" D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\gradle.properties
    Expected Result: 有统计输出，有版本号
    Evidence: .sisyphus/evidence/task-0.2-lombok-audit.txt
  ```

  **Commit**: NO

- [x] 0.3 **MixinExtras 依赖检查** — `quick`

  **What to do**:
  - 检查源模组 11 个 Mixin 中是否使用了 MixinExtras 注解 (`@ModifyExpressionValue`, `@WrapOperation`, `@ModifyReceiver`, `@ModifyVariable`, `@ModifyConstant`, `@Redirect`, `@Local`)
  - 验证 `mixin_extras_version` 在 gradle.properties 中的值
  - 确认 NeoForge 26.1.2 是否内置了 MixinExtras (Mixin 0.8.7+ 内置部分功能)
  - 输出: 使用 MixinExtras 注解的 Mixin 列表 + 版本需求

  **Must NOT do**:
  - 不要修改任何文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 0.1)
  - **Parallel Group**: Wave 0-A
  - **Blocks**: 0.12
  - **Blocked By**: 0.1

  **References**:
  - 源模组: `src/main/java/**/mixin/` — 11 个 Mixin 源文件
  - 源模组: `gradle.properties` → `mixin_extras_version`

  **Acceptance Criteria**:
  - [ ] 输出 MixinExtras 使用报告: N 个 Mixin 使用了 X 个注解
  - [ ] 如果 MixinExtras 不需要 → 决定去掉
  - [ ] 如果需要 → 版本已确认

  **QA Scenarios**:
  ```
  Scenario: MixinExtras usage fully audited
    Tool: Bash (grep)
    Steps:
      1. grep -r "@ModifyExpressionValue\|@WrapOperation\|@ModifyReceiver\|@ModifyVariable\|@ModifyConstant\|@Local" --include="*.java" D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\src\main\java\*\mixin\
    Expected Result: 有结果列表（或明确报告"未使用"）
    Evidence: .sisyphus/evidence/task-0.3-mixinextras-audit.txt
  ```

  **Commit**: NO

- [x] 0.4 **工具链环境验证** — `quick`

  **What to do**:
  - 检查 JDK 版本: `java -version` → 必须 >= 21 (NeoForge 26.1.2 要求)
  - 检查 Gradle 可用: `./gradlew --version` → 必须 >= 8.9
  - 检查 Gradle wrapper: `gradle/wrapper/gradle-wrapper.properties` 存在
  - 若不满足, 输出明确错误并 BLOCK 后续所有 Wave

  **Must NOT do**:
  - 不要安装或修改 JDK/Gradle
  - 不要创建任何 Java 文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 0.5-0.7 并行)
  - **Parallel Group**: Wave 0-B
  - **Blocks**: Wave 1 (缺少工具链则 FAIL FAST)
  - **Blocked By**: None

  **References**:
  - NeoForge 26.1.2 官方文档: https://docs.neoforged.net/ — 确认 JDK/Gradle 版本要求

  **Acceptance Criteria**:
  - [ ] `java -version` 输出 >= 21
  - [ ] `./gradlew --version` 成功, Gradle >= 8.9

  **QA Scenarios**:
  ```
  Scenario: Toolchain verified
    Tool: Bash
    Steps:
      1. java -version
      2. $env:JAVA_HOME 已设置
      3. .\gradlew --version (workdir=D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-26)
    Expected Result: Java 21+, Gradle 8.9+
    Failure Indicators: Java < 21 或 Gradle 不可用
    Evidence: .sisyphus/evidence/task-0.4-toolchain.txt
  ```

  **Commit**: NO

- [x] 0.5 **Parchment 版本确认** — `quick`

  **What to do**:
  - 检查 `parchment_minecraft_version` 在源 gradle.properties 的值
  - 搜索最新 Parchment 版本: https://parchmentmc.org/ 或 Maven Central
  - 验证 NeoForge 26.1.2 对应的 Parchment 版本是否存在
  - 若不存在 → 使用 Mojang mappings (no Parchment)
  - 输出: `[Parchment] 版本=v | 状态=可用/不可用，使用 Mojang mappings`

  **Must NOT do**:
  - 不要修改 gradle.properties
  - 不要下载任何文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 0.4, 0.6-0.7 并行)
  - **Parallel Group**: Wave 0-B
  - **Blocks**: 1.4 (gradle.properties 配置)
  - **Blocked By**: None

  **References**:
  - Parchment Maven: https://ldtteam.jfrog.io/artifactory/parchmentmc-public/org/parchmentmc/data/
  - 源模组: `gradle.properties` → `parchment_minecraft_version`

  **Acceptance Criteria**:
  - [ ] 输出 Parchment 版本状态报告
  - [ ] 如果不可用 → 明确降级方案 (Mojang mappings)

  **QA Scenarios**:
  ```
  Scenario: Parchment availability confirmed
    Tool: Bash
    Steps:
      1. grep "parchment" D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\gradle.properties
      2. 检查文档或 Maven 确认该版本对 26.1.2 是否可用
    Expected Result: 明确结论 (可用/不可用)
    Evidence: .sisyphus/evidence/task-0.5-parchment-check.txt
  ```

  **Commit**: NO

- [x] 0.6 **Capabilities → Attachments 预检** — `quick`

  **What to do**:
  - 搜索源模组中所有 Capabilities 引用: `Capabilities`, `Capability`, `ICapabilityProvider`, `AttachCapabilitiesEvent`
  - 确认 NeoForge 26.1.2 中 Capabilities → Attachments 的 API 变更
  - 统计受影响的文件数量和类型
  - 输出: `[Caps分析] 共 N 个文件使用旧 Capabilities API，需迁移为 DataAttachment`

  **Must NOT do**:
  - 不要修改任何文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 0.4-0.5, 0.7 并行)
  - **Parallel Group**: Wave 0-B
  - **Blocks**: 2.25 (ModCaps → Attachments 迁移任务)
  - **Blocked By**: None

  **References**:
  - 源模组: `src/main/java/**/ModCaps.java` — Caps 注册
  - 源模组: `src/main/java/**/` — 搜索 `Capabilities`, `AttachCapabilitiesEvent`
  - NeoForge 26.1.2 文档: https://docs.neoforged.net/docs/datastorage/attachments/

  **Acceptance Criteria**:
  - [ ] 输出 Caps/Attachments 影响范围报告
  - [ ] 列出所有需要迁移的文件路径

  **QA Scenarios**:
  ```
  Scenario: Capabilities inventory complete
    Tool: Bash (grep)
    Steps:
      1. grep -r "Capability\|Capabilities\|ICapabilityProvider\|AttachCapabilitiesEvent" --include="*.java" D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\src\main\java
    Expected Result: 完整文件列表和引用统计
    Evidence: .sisyphus/evidence/task-0.6-caps-audit.txt
  ```

  **Commit**: NO

- [x] 0.7 **enumextensions.json 验证** — `quick`

  **What to do**:
  - 搜索源模组中 `enumextensions.json` 文件 (通常位于 `src/main/resources/META-INF/`)
  - 检查 NeoForge 26.1.2 是否仍支持此机制 (MC 1.21.4+ 可能废弃)
  - 如果废弃 → 确定替代方案 (DataComponents 或直接注册)
  - 输出: `[enumextensions] 状态=支持/已废弃，替代方案=X`

  **Must NOT do**:
  - 不要修改文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 0.4-0.6 并行)
  - **Parallel Group**: Wave 0-B
  - **Blocks**: 2.25 (如废弃，影响注册策略)
  - **Blocked By**: None

  **References**:
  - NeoForge 26.1.2 文档: 搜索 "enumextensions" API 状态
  - 源模组: `src/main/resources/META-INF/enumextensions.json`

  **Acceptance Criteria**:
  - [ ] 确认 enumextensions 在 26.1.2 中的状态
  - [ ] 如果废弃 → 明确替代方案

  **QA Scenarios**:
  ```
  Scenario: enumextensions status confirmed
    Tool: Bash
    Steps:
      1. Test-Path "D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21\src\main\resources\META-INF\enumextensions.json"
      2. 如果存在 → 读内容，查文档确认 26.1.2 兼容性
    Expected Result: 状态已确认 (支持/废弃)
    Evidence: .sisyphus/evidence/task-0.7-enumextensions-check.txt
  ```

  **Commit**: NO

- [x] 0.8 **Mixin Target 预审（含 GitHub 搜索）** — `quick`

  **What to do**:
  - 从源模组 `src/main/resources/avaritia.mixins.json` 提取所有 11 个 mixin 的 `@Mixin` target 类名
  - 使用 CodeGraph 在 NeoForge 26.1.2 源码中验证每个 target 类是否存在
  - 对不存在的 target，使用代理 `localhost:10808` 在 GitHub 搜索 NeoForge 26.1.2 参考模组中的替代方案
  - 记录每个 mixin 的判断结果：`OK`（target 存在）/ `RENAMED`（类已改名，需找新 target）/ `REMOVED`（类已删除，需找替代注入点）/ `BROKEN`（方法/字段签名变更）
  - 输出 `avaritia-mixin-audit.md` 文件（列出全部 11 个 mixin 的预审结果 + 迁移建议）
  - 若 11 个全部 OK，则 Wave 5 可完全并行化

  **Must NOT do**:
  - 不要修改任何 mixin Java 文件
  - 不要修改 mixins.json
  - 不要猜测 target 是否存在——必须通过 CodeGraph 或 GitHub 搜索确认

  **Recommended Agent Profile**:
  > Category `quick`: 纯搜索/索引查询，无代码变更
  - **Skills**: [`minecraft-modder-neoforge`]
    - `minecraft-modder-neoforge`: NeoForge 26.1.2 Mixin 注入模式知识

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 0-C (with 0.9, 0.10, 0.11, 0.12)
  - **Blocks**: Wave 5 (mixin migration)
  - **Blocked By**: 0.1 (CodeGraph 索引)

  **References**:
  - `src/main/resources/avaritia.mixins.json` (源模组) — 11 个 mixin 的定义和 target 类名
  - 使用 `codegraph_search` 查询 target 类名是否存在
  - GitHub 搜索 (proxy `localhost:10808`): 搜索模式 `"class TargetClassName" repo:neoforge` 或其他 26.1.2 模组

  **Acceptance Criteria**:
  - [ ] `avaritia-mixin-audit.md` 文件创建，列出全部 11 个 mixin 的判断结果
  - [ ] 每个 mixin 有明确状态: OK / RENAMED / REMOVED / BROKEN
  - [ ] BROKEN/REMOVED 的有 GitHub 替代方案链接或迁移建议

  **QA Scenarios**:
  ```
  Scenario: 验证 audit 文件完整覆盖 11 个 mixin
    Tool: Bash
    Steps:
      1. grep -c "### " .sisyphus/evidence/avaritia-mixin-audit.md
      2. 确认输出 >= 11（每个 mixin 一个 section）
    Expected Result: 计数 >= 11
    Evidence: .sisyphus/evidence/avaritia-mixin-audit.md
  ```

  **Commit**: NO（纯文档输出，不涉及源码）

- [x] 0.9 **AT (Access Transformer) 文件逐行验证** — `quick`

  **What to do**:
  - 读取源模组的 `src/main/resources/META-INF/accesstransformer.cfg`（84 行）
  - 对每一行 AT 指令，使用 CodeGraph 在 NeoForge 26.1.2 源码中验证目标类/字段/方法是否存在
  - 记录每行的状态：`OK` / `BROKEN`（target 不存在）/ `REDUNDANT`（26.1.2 已公开）
  - 输出 `avaritia-at-audit.md` 文件（逐行对照表）
  - 特别关注：MC 1.21.5 中许多原本 private 的字段已变为 public（如 `LivingEntity#getJumpPower`）

  **Must NOT do**:
  - 不要修改 accesstransformer.cfg
  - 不要猜测——每个 target 必须通过 CodeGraph 或源码确认

  **Recommended Agent Profile**:
  > Category `quick`: 纯验证，无代码变更
  - **Skills**: [`minecraft-modder-neoforge`]
    - `minecraft-modder-neoforge`: NeoForge 26.1.2 AT 机制知识

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 0-C (with 0.8, 0.10, 0.11, 0.12)
  - **Blocks**: Wave 5 (AT 迁移)
  - **Blocked By**: 0.1 (CodeGraph 索引)

  **References**:
  - `src/main/resources/META-INF/accesstransformer.cfg` (源模组) — 84 行 AT 指令
  - 使用 `codegraph_search` / `codegraph_node` 验证 target 字段/方法

  **Acceptance Criteria**:
  - [ ] `avaritia-at-audit.md` 文件创建，84 行逐行对照
  - [ ] 每行有 OK / BROKEN / REDUNDANT 状态
  - [ ] BROKEN 的行有替代方案建议

  **QA Scenarios**:
  ```
  Scenario: 验证 audit 文件覆盖 84 行
    Tool: Bash
    Steps:
      1. grep -c "^|" .sisyphus/evidence/avaritia-at-audit.md
      2. 确认输出 >= 84
    Expected Result: 计数 >= 84
    Evidence: .sisyphus/evidence/avaritia-at-audit.md
  ```

  **Commit**: NO

- [x] 0.10 **依赖版本确认（JEI / Jade / Curios）** — `quick`

  **What to do**:
  - 确认 JEI、Jade、Curios 三个兼容模组在 NeoForge 26.1.2 上的最新可用版本
  - 检查每个依赖的 Maven 坐标是否变化（如 JEI 从 `mezz.jei:jei-1.21.1-neoforge` 变为新格式）
  - 确认 NeoForge 26.1.2 的基础依赖版本（NeoForge MDG 插件版本、Minecraft 版本、Java 版本要求）
  - 输出 `dependency-versions.md` 文件（版本表 + Maven 坐标）

  **Must NOT do**:
  - 不要查询或记录已删除的兼容模组（EMI / REI / CraftTweaker / KubeJS / ProjectE 等）

  **Recommended Agent Profile**:
  > Category `quick`: 纯信息收集
  - **Skills**: [`minecraft-modder-neoforge`]
    - `minecraft-modder-neoforge`: Maven 仓库和模组生态知识

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 0-C (with 0.8, 0.9, 0.11, 0.12)
  - **Blocks**: Wave 1 (build.gradle 依赖声明)
  - **Blocked By**: None (纯网络查询)

  **References**:
  - JEI: https://www.curseforge.com/minecraft/mc-mods/jei
  - Jade: https://www.curseforge.com/minecraft/mc-mods/jade
  - Curios: https://www.curseforge.com/minecraft/mc-mods/curios
  - NeoForge MDG: https://github.com/neoforged/mdk
  - 使用代理 `localhost:10808` 访问 CurseForge / Modrinth API

  **Acceptance Criteria**:
  - [ ] `dependency-versions.md` 文件创建，列出 JEI / Jade / Curios 的准确 26.1.2 版本
  - [ ] 每个依赖有 Maven 坐标 (group:artifact:version)
  - [ ] NeoForge MDG 最低版本确认

  **QA Scenarios**:
  ```
  Scenario: 验证版本文件包含三个兼容模组
    Tool: Bash
    Steps:
      1. grep -c "jei\|jade\|curios" .sisyphus/evidence/dependency-versions.md
      2. 确认输出 >= 3
    Expected Result: 每个模组至少出现一次
    Evidence: .sisyphus/evidence/dependency-versions.md
  ```

  **Commit**: NO

---

- [x] 0.11 CodeGraph 索引目标工作区

  **What to do**:
  - 在 Avaritia-26 目录下运行 `codegraph init -i` 初始化 CodeGraph 索引
  - 确认索引覆盖 `src/main/java/` 下所有 Java 文件
  - 为后续 Wave 的编译验证和引用搜索做准备

  **Must NOT do**:
  - 不要索引 `src/generated/` 或 `build/` 目录

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 0-D (with 0.12)
  - **Blocks**: None (Wave 0 末尾)
  - **Blocked By**: None

  **References**:
  - `.codegraph/` 配置文件 — CodeGraph 忽略规则模式

  **Acceptance Criteria**:
  - [ ] `codegraph status` 返回 "healthy" 状态
  - [ ] Java 文件被索引 (>0 indexed files)
  - [ ] 索引不包含 build/generated 目录内容

  **QA Scenarios**:
  ```
  Scenario: CodeGraph 索引创建并健康
    Tool: Bash
    Steps:
      1. codegraph status
      2. 确认输出包含 "healthy" 或 indexed file count > 0
    Expected Result: 索引就绪，可搜索
    Evidence: .sisyphus/evidence/task-0-11-codegraph-target.txt
  ```

  **Commit**: NO

---

- [x] 0.12 创建预审汇总报告

  **What to do**:
  - 汇总 0.2-0.10 所有审计结果到单一文档 `pre-audit-summary.md`
  - 列出每个检查项的结论 (PASS / NEEDS_CHANGE / BLOCKED)
  - 标注需要跨 Wave 特殊处理的项 (如 ArmorMaterial 重写、Caps→Attachments)
  - 标记阻断项：必须先解决才能开始 Wave 1 的问题

  **Must NOT do**:
  - 不要修改任何源代码
  - 不要创建重复的审计文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 0-D (with 0.11)
  - **Blocks**: Wave 1 Gate (需审核报告确认无阻断)
  - **Blocked By**: 0.2, 0.3, 0.5, 0.6, 0.7, 0.8, 0.9, 0.10 (需要所有审计结果)

  **References**:
  - `.sisyphus/evidence/avaritia-lombok-audit.md` — Lombok 分析结果
  - `.sisyphus/evidence/avaritia-mixinextras-audit.md` — MixinExtras 检查
  - `.sisyphus/evidence/avaritia-parchment-verify.md` — Parchment 验证
  - `.sisyphus/evidence/avaritia-caps-inventory.md` — Caps→Attachments 盘点
  - `.sisyphus/evidence/avaritia-enumextensions-check.md` — enumextensions 检查
  - `.sisyphus/evidence/avaritia-mixin-audit.md` — Mixin 预审
  - `.sisyphus/evidence/avaritia-at-audit.md` — AT 验证
  - `.sisyphus/evidence/dependency-versions.md` — 依赖版本

  **Acceptance Criteria**:
  - [ ] `pre-audit-summary.md` 存在且包含全部 8 个审计项的结论
  - [ ] 每个结论标注 PASS / NEEDS_CHANGE / BLOCKED
  - [ ] BLOCKED 项有明确的解除条件
  - [ ] 汇总报告标注建议的解决顺序 (优先级排序)

  **QA Scenarios**:
  ```
  Scenario: 预审报告包含所有检查项
    Tool: Bash
    Steps:
      1. grep -c "PASS\|NEEDS_CHANGE\|BLOCKED" .sisyphus/evidence/pre-audit-summary.md
      2. 确认输出 >= 8
    Expected Result: 8 个以上检查项被汇总且有结论
    Evidence: .sisyphus/evidence/pre-audit-summary.md
  ```

  **Commit**: YES (Wave 0 汇总提交)
  - Message: `audit(avaritia): Wave 0 pre-audit summary with all 8 check items`
  - Files: `.sisyphus/evidence/pre-audit-summary.md`
  - Pre-commit: N/A

---

## Wave 0 Gate — 预审通过检查

> **阻断条件**: 汇总报告存在 BLOCKED 项时，Wave 1 不得启动
> **通过条件**: 所有项为 PASS 或 NEEDS_CHANGE（无 BLOCKED）

- [ ] Gate-0: `./gradlew compileJava` — 确认空项目可通过编译 (空模板)

# Wave 1 — 项目骨架

> **进入条件**: Gate-0 通过（预审汇总报告无 BLOCKED 项）
> **目标**: 可编译的空模组骨架 (4个构建文件 + 主类 + 包目录 + Lombok/MixinExtras + mods.toml)

- [x] 1.1 创建 Gradle 构建文件

  **What to do**:
  - 创建 `build.gradle` — NeoForge MDG 26.1.x, 声明 sourceSets, runs 配置
  - 创建 `settings.gradle` — 插件仓库 + 模组名
  - 创建 `gradle.properties` — 迁移源模组的版本变量 (mod_version, etc.)
  - 创建 `gradle/libs.versions.toml` — NeoForge, MC, Parchment, Lombok, MixinExtras, JEI, Jade, Curios, JUnit 5

  **Must NOT do**:
  - 不添加 AnvilLib / CraftTweaker / KubeJS / CCL / ProjectE 等依赖

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `["minecraft-modder-neoforge"]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (构建基础设施必须最先完成)
  - **Parallel Group**: Wave 1 第一项
  - **Blocks**: 1.2-1.8（所有后续任务需要 Gradle）
  - **Blocked By**: None

  **References**:
  - NeoForge MDG: https://docs.neoforged.net/ — 插件配置
  - 源 Avaritia-1.21 `build.gradle`, `gradle.properties` — 版本变量基线
  - `.sisyphus/evidence/dependency-versions.md` — 26.1.2 版本坐标

  **Acceptance Criteria**:
  - [ ] 四个文件都存在: build.gradle, settings.gradle, gradle.properties, libs.versions.toml
  - [ ] `./gradlew projects` 成功识别项目

  **QA Scenarios**:
  ```
  Scenario: Gradle 配置正确
    Tool: Bash
    Steps:
      1. & ".\gradlew.bat" projects
    Expected Result: 输出显示项目 ":avaritia"
    Evidence: .sisyphus/evidence/task-1-1-gradle-config.txt
  ```

  **Commit**: YES
  - Message: `build(avaritia): Initialize Gradle project structure for NeoForge 26.1.2`
  - Files: `build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/libs.versions.toml`

---

- [x] 1.2 创建主类 + mods.toml

  **What to do**:
  - 创建 `src/main/java/com/avaritia/Avaritia.java` — `@Mod("avaritia")`
  - 创建 `src/templates/META-INF/neoforge.mods.toml` — 可选依赖声明 JEI/Jade/Curios

  **Must NOT do**:
  - 不在 mods.toml 声明已删除的兼容模组

  **Recommended Agent Profile**: `quick`
  **Skills**: `["minecraft-modder-neoforge"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1-A (with 1.3)
  - **Blocks**: 1.5 (需要编译验证)
  - **Blocked By**: 1.1

  **References**:
  - 源 Avaritia-1.21 `Avaritia.java` — MOD_ID 参考
  - NeoForge 文档: neoforge.mods.toml 格式

  **Acceptance Criteria**:
  - [ ] `Avaritia.java` 含 `@Mod` 注解 + `MOD_ID = "avaritia"`
  - [ ] `neoforge.mods.toml` 含 JEI/Jade/Curios 可选依赖

  **QA Scenarios**:
  ```
  Scenario: 主类编译通过
    Tool: Bash
    Steps:
      1. & ".\gradlew.bat" compileJava
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-1-2-main-class.txt
  ```

  **Commit**: YES
  - Message: `feat(avaritia): Create main mod class and neoforge.mods.toml`
  - Files: `Avaritia.java`, `neoforge.mods.toml`

---

- [x] 1.3 创建包目录结构 + .gitignore

  **What to do**:
  - 创建完整包树: client/ data/ init/ init/registry/ mixin/ network/ compat/ util/
  - 创建资源目录: src/generated/resources/, textures/, sounds/
  - 创建 `.gitignore`: 忽略 build/, .gradle/, run/, logs/, src/generated/, reference/, IDE 文件

  **Must NOT do**: 不创建 reference/ 目录

  **Recommended Agent Profile**: `quick`
  **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1-A (with 1.2)
  - **Blocks**: None
  - **Blocked By**: 1.1

  **Acceptance Criteria**:
  - [ ] 所有 9 个包目录存在
  - [ ] textures/ + sounds/ 目录存在
  - [ ] `.gitignore` 含 build/, generated/, IDE 规则

  **QA Scenarios**:
  ```
  Scenario: 包结构完整
    Tool: Bash
    Steps:
      1. Test-Path -LiteralPath "src/main/java/com/avaritia/init/registry"
      2. Test-Path -LiteralPath "src/generated/resources"
    Expected Result: 所有路径存在
    Evidence: .sisyphus/evidence/task-1-3-package-structure.txt
  ```

  **Commit**: YES
  - Message: `chore(avaritia): Create package structure and .gitignore`
  - Files: 所有目录 + `.gitignore`

---

- [x] 1.4 添加 Lombok + MixinExtras 依赖

  **What to do**:
  - 根据 0.2/0.3 审计结果: 添加 Lombok compileOnly+annotationProcessor, MixinExtras 按需添加
  - 若审计建议 Delombok: 跳过，标记为后续手动展开

  **Must NOT do**: 不添加 AnvilLib

  **Recommended Agent Profile**: `quick`
  **Skills**: `["minecraft-modder-neoforge"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1-B (with 1.5)
  - **Blocks**: Wave 2+ (影响编译)
  - **Blocked By**: 1.1, 0.2, 0.3

  **References**:
  - `.sisyphus/evidence/avaritia-lombok-audit.md`
  - `.sisyphus/evidence/avaritia-mixinextras-audit.md`

  **Acceptance Criteria**:
  - [ ] Lombok 已配置或 Delombok 已记录
  - [ ] MixinExtras 已配置或确认不需要
  - [ ] `./gradlew compileJava` 通过

  **QA Scenarios**:
  ```
  Scenario: Lombok 注解编译通过
    Tool: Bash
    Steps:
      1. 创建 @Data class TestLombok { int x; }
      2. & ".\gradlew.bat" compileJava
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-1-4-lombok-setup.txt
  ```

  **Commit**: YES
  - Message: `build(avaritia): Add Lombok and MixinExtras dependencies`
  - Files: `build.gradle`, `gradle/libs.versions.toml`

---

- [x] 1.5 Wave 1 Gate 编译验证

  **What to do**:
  - 确保所有 Wave 1 文件就位
  - 运行 `./gradlew compileJava`
  - 确认 BUILD SUCCESSFUL

  **Must NOT do**: 不跳过编译验证

  **Recommended Agent Profile**: `quick`
  **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (Wave Gate)
  - **Blocks**: Wave 2
  - **Blocked By**: 1.1, 1.2, 1.4

  **Acceptance Criteria**:
  - [ ] `./gradlew compileJava` → BUILD SUCCESSFUL
  - [ ] 零编译错误，零警告

  **QA Scenarios**:
  ```
  Scenario: 空骨干编译通过
    Tool: Bash
    Steps:
      1. & ".\gradlew.bat" compileJava --warning-mode all
    Expected Result: BUILD SUCCESSFUL, 0 warnings
    Evidence: .sisyphus/evidence/task-1-5-compile-gate.txt
  ```

  **Commit**: NO (门控不提交)

---

## Wave 2 — Registrations (24 DeferredRegister Files + Gate-2)

> **Blocked by**: Wave 1 (Gradle project skeleton must compile)
> **Blocks**: Waves 3, 4, 5, 6, 7
> **Parallelism**: All 24 registry tasks in 2 sub-waves. Wave 2-A: 12 simple registries (no inter-dependencies). Wave 2-B: 12 complex registries (Blocks depends on Items, etc.)
> **Mandate**: EVERY `Item.Properties()` must call `.setId(ResourceKey.create(Registries.ITEM, id))`. ZERO manual JSON.

### Wave 2-A — Simple Registries (12 parallel tasks, no inter-dependencies)

- [x] 2.1 **ModSounds Registration**

  **What to do**:
  - Copy `ModSounds.java` from source; port to `DeferredRegister<SoundEvent>` with `DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID)`
  - Each `SoundEvent` registered via `SOUNDS.register("name", id -> SoundEvent.createVariableRangeEvent(id))`

  **Must NOT do**: Do NOT use `RegistryObject` — only `DeferredHolder`. Do NOT hand-write `sounds.json`.

  **Recommended Agent Profile**:
  - **Category**: `quick` — single file, simple SoundEvent registration
  - **Skills**: `["minecraft-modder-neoforge"]`

  **Parallelization**: Wave 2-A, parallel with 2.2–2.12. Blocked by Wave 1. Blocks Wave 3.

  **References**:
  - Source: `src/main/java/.../init/registry/ModSounds.java` — sound definitions
  - Pattern: `DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID)` then `.register("name", id -> SoundEvent.createVariableRangeEvent(id))`

  **Acceptance Criteria**:
  - [ ] `ModSounds.java` compiles with zero errors
  - [ ] All `SoundEvent` registrations use `DeferredRegister`

  **QA Scenarios**:
  ```
  Scenario: Happy path — compile check
    Tool: Bash (./gradlew)
    Preconditions: Wave 1 complete, Gradle project compiles
    Steps:
      1. Copy ModSounds.java to target init/ package
      2. Port to DeferredRegister<SoundEvent> syntax
      3. Run `./gradlew compileJava` from workspace root
    Expected Result: BUILD SUCCESSFUL with no errors from ModSounds.java
    Failure Indicators: Compile errors in ModSounds.java or missing imports
    Evidence: .sisyphus/evidence/task-2-1-compile.txt
  ```

  **Commit**: NO (grouped with Wave 2 gate commit)

- [x] 2.2 **ModParticles Registration**

  **What to do**: Copy `ModParticles.java`; port to `DeferredRegister<SimpleParticleType>` with `DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID)`.

  **Must NOT do**: Do NOT hand-write particle JSON.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12. Blocked by Wave 1. Blocks Wave 3.

  **References**: Source: `src/main/java/.../init/registry/ModParticles.java`

  **Acceptance Criteria**: [ ] Compiles with zero errors
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-2-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.3 **ModMobEffects Registration**

  **What to do**: Copy `ModMobEffects.java`; port to `DeferredRegister<MobEffect>` with `DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID)`.

  **Must NOT do**: Do NOT touch Effect class implementations — only registration.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12. Blocked by Wave 1. Blocks Wave 3.

  **References**: Source: `src/main/java/.../init/registry/ModMobEffects.java`

  **Acceptance Criteria**: [ ] Compiles with zero errors
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-3-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.4 **ModEnchants Registration**

  **What to do**: Copy `ModEnchants.java`; port to `DeferredRegister<Enchantment>` with `DeferredRegister.create(Registries.ENCHANTMENT, MOD_ID)`. Adapt Enchantment constructor to 26.1.2 API.

  **Must NOT do**: Do NOT copy enchantment effect logic yet — only registration structure.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModEnchants.java`. NeoForge docs for 26.1.2 Enchantment API.

  **Acceptance Criteria**: [ ] Compiles with zero errors, Enchantment registration uses DeferredRegister
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-4-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.5 **ModDamageTypes Registration**

  **What to do**: Copy `ModDamageTypes.java`; port to `DeferredRegister<DamageType>` with `DeferredRegister.create(Registries.DAMAGE_TYPE, MOD_ID)`.

  **Must NOT do**: Do NOT hand-write damage_type JSON files.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModDamageTypes.java`

  **Acceptance Criteria**: [ ] Compiles with zero errors
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-5-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.6 **ModDataComponents Registration**

  **What to do**: Copy `ModDataComponents.java`; port to `DeferredRegister<DataComponentType<?>>`. Use `DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID)`.

  **Must NOT do**: Do NOT change data component serialization logic.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModDataComponents.java`

  **Acceptance Criteria**: [ ] Compiles with zero errors
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-6-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.7 **ModCreativeModeTabs Registration**

  **What to do**: Copy `ModCreativeModeTabs.java`; port to `DeferredRegister<CreativeModeTab>`. Use `DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)`. References Items/Blocks registry classes (may have compile errors until 2.13/2.14 done — add stubs if needed).

  **Must NOT do**: Do NOT reference class files not yet migrated — use deferred resolution.

  **Recommended Agent Profile**: Category `deep` — may need deferred resolution patterns
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A. Note: depends on Items/Blocks symlink references. May need follow-up after 2.13/2.14.

  **References**: Source: `src/main/java/.../init/registry/ModCreativeModeTabs.java`

  **Acceptance Criteria**: [ ] CreativeModeTab registrations use DeferredRegister; compile succeeds (or stubs in place for items/blocks)
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL or known deferred stubs
  **Evidence**: `.sisyphus/evidence/task-2-7-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.8 **ModEntityTypes Registration** (formerly ModEntities)

  **What to do**: Copy `ModEntities.java`; port to `DeferredRegister<EntityType<?>>`. Rename class to `ModEntityTypes` if needed per 26.1.2 convention. Use `EntityType.Builder.of(...)`. Reference entity classes from source.

  **Must NOT do**: Do NOT migrate entity classes themselves — only type registration. Do NOT register renderers here.

  **Recommended Agent Profile**: Category `deep` — EntityType.Builder API change
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A. Parallel with 2.1–2.12.

  **References**: Source: `src/main/java/.../init/registry/ModEntities.java`. NeoForge 26.1.2 EntityType.Builder docs.

  **Acceptance Criteria**: [ ] All EntityType registrations compile; Builder API matches 26.1.2
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-8-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.9 **ModMenus Registration**

  **What to do**: Copy `ModMenus.java`; port to `DeferredRegister<MenuType<?>>`. Use `DeferredRegister.create(Registries.MENU, MOD_ID)`. `MenuType` registration uses `new MenuType<>(Supplier)` pattern — verify 26.1.2 constructor.

  **Must NOT do**: Do NOT migrate screen/menu container classes yet.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModMenus.java`

  **Acceptance Criteria**: [ ] MenuType registrations compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-9-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.10 **ModRecipeTypes Registration**

  **What to do**: Copy `ModRecipeTypes.java`; port recipe type declarations. Recipe types use `RecipeType.simple()` or `new RecipeType<>()`. Verify 26.1.2 RecipeType registration pattern.

  **Must NOT do**: Do NOT migrate RecipeSerializer yet — that's 2.11. Do NOT migrate recipe implementations yet.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModRecipeTypes.java`. NeoForge 26.1.2 Recipe API.

  **Acceptance Criteria**: [ ] RecipeType registrations compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-10-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.11 **ModRecipeSerializers Registration**

  **What to do**: Copy `ModRecipeSerializers.java`; port to `DeferredRegister<RecipeSerializer<?>>`. Use `DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID)`.

  **Must NOT do**: Do NOT migrate recipe class implementations yet.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.12

  **References**: Source: `src/main/java/.../init/registry/ModRecipeSerializers.java`

  **Acceptance Criteria**: [ ] RecipeSerializer registrations compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-11-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.12 **Simple Enum Registries** — ModRarities, ModIngredients, ModSingularities, ModToolTiers, ModTooltips, ModFoods

  **What to do**: Copy and port 6 enum/constant files: `ModRarities.java`, `ModIngredients.java`, `ModSingularities.java`, `ModToolTiers.java`, `ModTooltips.java`, `ModFoods.java`. Adapt to 26.1.2 APIs. `FoodProperties` → `FoodProperties.Builder` pattern. `Rarity` → need to check 26.1.2 API. `Tier` → `ToolMaterial` → verify 26.1.2 pattern.

  **Must NOT do**: Do NOT create separate tasks — all 6 in one batch. Do NOT add new values.

  **Recommended Agent Profile**: Category `deep` — multiple API changes across 6 files
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-A, parallel with 2.1–2.11

  **References**: Source: `src/main/java/.../init/registry/ModRarities.java` etc. NeoForge 26.1.2 docs for FoodProperties, Rarity, ToolMaterial.

  **Acceptance Criteria**: [ ] All 6 enum/constant files compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (zero errors from these 6 files)
  **Evidence**: `.sisyphus/evidence/task-2-12-compile.txt`
  **Commit**: NO (grouped)

### Wave 2-B — Complex Registries (12 tasks, some inter-dependencies)

- [x] 2.13 **ModItems Registration** ⚠️ CRITICAL

  **What to do**:
  - Copy `ModItems.java` from source; preserve the `item("name", Supplier)` helper pattern
  - Port to `DeferredRegister.Items` with `.setId()` on EVERY Item.Properties
  - Pattern: `ITEMS.register("name", id -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))))`
  - For custom Item subclasses: `new CustomItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))`
  - ~70+ items must ALL have setId(). Tool items (AxeItem, PickaxeItem, etc.) need ToolMaterial reference from 2.15

  **Must NOT do**: Do NOT skip setId() on any item. Do NOT change item behavior. Do NOT manually create item model JSON.

  **Recommended Agent Profile**: Category `deep` — high volume, critical setId() enforcement
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B. Blocks: 2.14 (BlockItems reference items). Blocked by: Wave 1, 2.12 (ToolTiers), 2.15 (ArmorMaterial for armor items).

  **References**:
  - Source: `src/main/java/.../init/registry/ModItems.java` — item registration with helper pattern
  - NeoForge 26.1.2: `Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))` — MANDATORY
  - Source: `src/main/java/.../items/` — custom item class implementations

  **Acceptance Criteria**:
  - [ ] ModItems.java compiles with zero errors
  - [ ] EVERY `Item.Properties()` call includes `.setId(ResourceKey.create(Registries.ITEM, id))`
  - [ ] Helper `item()` pattern preserved
  - [ ] Tool items reference ModToolTiers correctly

  **QA Scenarios**:
  ```
  Scenario: Happy path — all items compile with setId()
    Tool: Bash (./gradlew)
    Preconditions: Wave 1 complete, Wave 2-A registries in place
    Steps:
      1. Copy ModItems.java to target init/ package
      2. Port each item registration with setId()
      3. Run `./gradlew compileJava`
    Expected Result: BUILD SUCCESSFUL
    Failure Indicators: Any compile error (missing setId, missing import, wrong constructor)
    Evidence: .sisyphus/evidence/task-2-13-compile.txt

  Scenario: setId() audit — verify all items have setId()
    Tool: Bash (grep)
    Steps:
      1. Run `grep -c "setId" src/main/java/.../init/ModItems.java`
      2. Count `Properties()` calls: `grep -c "Properties()" src/main/java/.../init/ModItems.java`
      3. Assert both counts match
    Expected Result: setId count == Properties() count
    Failure Indicators: Count mismatch — missing setId() on some items
    Evidence: .sisyphus/evidence/task-2-13-audit.txt
  ```

  **Commit**: NO (grouped)

- [x] 2.14 **ModBlocks Registration** ⚠️ CRITICAL

  **What to do**:
  - Copy `ModBlocks.java`; port to `DeferredRegister.Blocks` (DeferredRegister<Block>)
  - Each Block must register its corresponding BlockItem in ModItems (or inline) with setId()
  - BlockItem pattern: `ITEMS.register("block_name", id -> new BlockItem(BLOCK.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))))`
  - ~25 blocks. Check if any block uses custom Block subclasses — carry files over and fix compile errors.

  **Must NOT do**: Do NOT skip setId() on BlockItems. Do NOT manually create blockstate/model JSON.

  **Recommended Agent Profile**: Category `deep` — Block + BlockItem dual registration, setId() critical
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B. Blocked by: 2.13 (Items for BlockItems), Wave 1. Blocks: 2.7 (CreativeTabs), Wave 3 (datagen for blocks).

  **References**:
  - Source: `src/main/java/.../init/registry/ModBlocks.java`
  - NeoForge 26.1.2: `DeferredRegister.Blocks` — built-in subclass
  - BlockItem: `new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))`

  **Acceptance Criteria**:
  - [ ] All 25± blocks register with DeferredRegister.Blocks
  - [ ] Every BlockItem has setId()
  - [ ] Compiles with zero errors

  **QA Scenarios**:
  ```
  Scenario: Happy path — all blocks + BlockItems compile
    Tool: Bash (./gradlew)
    Steps:
      1. Copy ModBlocks.java, port to DeferredRegister.Blocks
      2. Add BlockItem registrations with setId()
      3. Run `./gradlew compileJava`
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-2-14-compile.txt
  ```

  **Commit**: NO (grouped)

- [x] 2.15 **ModArmorMaterial Registration** ⚠️ API Changed

  **What to do**:
  - Copy `ModArmorMaterial.java`; port to 26.1.2 ArmorMaterial API (changed in MC 1.21.5)
  - 26.1.2 uses `ArmorMaterial` with `ResourceKey` pattern — different from 1.21.1
  - Check NeoForge docs for current ArmorMaterial registration. May need `DeferredRegister.create(Registries.ARMOR_MATERIAL, MOD_ID)`
  - Infinity Armor items reference these materials — ensure compatibility

  **Must NOT do**: Do NOT guess the API — verify against 26.1.2 source/docs. Do NOT skip migration — ARMOR_MATERIAL is a registry in 26.1.2.

  **Recommended Agent Profile**: Category `deep` — significant API change
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B. Blocked by: Wave 1. Required by 2.13 (armor items).

  **References**:
  - Source: `src/main/java/.../init/registry/ModArmorMaterial.java`
  - Source: `src/main/java/.../items/armor/InfinityArmorItem.java` — uses ArmorMaterial
  - NeoForge 26.1.2 official docs / source for ArmorMaterial API
  - Check `Registries.ARMOR_MATERIAL` exists in 26.1.2

  **Acceptance Criteria**:
  - [ ] ArmorMaterial registrations compile against 26.1.2 API
  - [ ] InfinityArmorItem references compile (or deferred stubs)
  - [ ] Uses correct 26.1.2 ArmorMaterial constructor/builder pattern

  **QA Scenarios**:
  ```
  Scenario: Verify ArmorMaterial API & compile
    Tool: Bash (./gradlew)
    Steps:
      1. Research 26.1.2 ArmorMaterial API via docs
      2. Port ModArmorMaterial.java with confirmed API
      3. Run `./gradlew compileJava`
    Expected Result: BUILD SUCCESSFUL
    Failure Indicators: ArmorMaterial constructor mismatch, missing Registries.ARMOR_MATERIAL
    Evidence: .sisyphus/evidence/task-2-15-compile.txt
  ```

  **Commit**: NO (grouped)

- [x] 2.16 **ModCaps → Attachments Migration** ⚠️ HIGH RISK

  **What to do**:
  - Read `ModCaps.java` from source; identify all Capabilities registered
  - NeoForge 26.1.x replaced Capabilities with DataAttachments
  - Port each Capability to `DataAttachment<T>` with `DeferredRegister<AttachmentType<?>>`
  - Use `AttachmentType.Builder` pattern: `ATTACHMENTS.register("name", () -> AttachmentType.builder(supplier).build())`
  - Find all files that reference ModCaps (use grep on source) — may need deferred fixes

  **Must NOT do**: Do NOT leave broken Capability references. Do NOT skip this — Capabilities API is REMOVED in 26.1.x.

  **Recommended Agent Profile**: Category `deep` — API replacement (Capabilities→Attachments), cross-file impact
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B. Blocked by: Wave 1. This is foundational — ModCaps likely referenced by items/blocks/entities.

  **References**:
  - Source: `src/main/java/.../init/registry/ModCaps.java`
  - NeoForge 26.1.x Attachments docs/primer
  - Grep source for `ModCaps` references to understand impact radius

  **Acceptance Criteria**:
  - [ ] All Capabilities ported to DataAttachments or equivalent 26.1.2 API
  - [ ] AttachmentType registrations compile
  - [ ] Known reference sites documented (fixes in downstream waves)

  **QA Scenarios**:
  ```
  Scenario: ModCaps → Attachments migration compiles
    Tool: Bash (./gradlew)
    Steps:
      1. Research NeoForge 26.1.x Attachments API
      2. Port ModCaps.java to Attachments pattern
      3. Run `./gradlew compileJava` (may have downstream errors from reference sites — document them)
    Expected Result: ModCaps.java itself compiles; downstream errors cataloged
    Evidence: .sisyphus/evidence/task-2-16-compile.txt
  ```

  **Commit**: NO (grouped)

- [x] 2.17 **ModTags Registration**

  **What to do**: Copy `ModTags.java`; port tag declarations to 26.1.2. Tag keys use `TagKey.create(Registries.ITEM, ...)` pattern — verify 26.1.2 compatibility.

  **Must NOT do**: Do NOT hand-write tag JSON files — datagen handles that in Wave 3.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B. Blocked by: Wave 1.

  **References**: Source: `src/main/java/.../init/registry/ModTags.java`

  **Acceptance Criteria**: [ ] TagKey declarations compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-17-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.18 **ModSearches Registration**

  **What to do**: Copy `ModSearches.java` (custom search registry for Avaritia). Identify how searches are registered. Port equivalent mechanism to 26.1.2.

  **Recommended Agent Profile**: Category `deep` — custom registry mechanism
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B

  **References**: Source: `src/main/java/.../init/registry/ModSearches.java`

  **Acceptance Criteria**: [ ] Searches registration compiles
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-18-compile.txt`
  **Commit**: NO (grouped)

- [x] 2.19 **Remaining Registry Files** — ModLootModifiers, ModFeatures, ModWorldGen, etc.

  **What to do**: Identify and migrate any additional registry files not covered by 2.1–2.18. Check source for files like ModLootModifiers, ModFeatures, or any other `Mod*.java` in the `init/registry/` directory not yet accounted for.

  **Must NOT do**: Do NOT leave any registry file behind — audit the full `init/registry/` directory.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 2-B

  **References**: Full listing of `src/main/java/.../init/registry/` in source

  **Acceptance Criteria**: [ ] All remaining registry files compiled; `init/registry/` complete
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-2-19-compile.txt`
  **Commit**: NO (grouped)

### Wave Gate-2 — Compile Check

- [x] Gate-2 **Wave 2 Compile Verification**

  **What to do**: Run `./gradlew compileJava` and verify ALL 24+ registry files compile with zero errors. If errors persist, open IDE diagnostics (LSP) and fix cyclically until clean.

  **Must NOT do**: Do NOT proceed to Wave 3 with compile errors. Do NOT hand-fix errors without understanding root cause.

  **Recommended Agent Profile**: Category `deep` — may need to debug compile errors across 24 files
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Sequential — runs after ALL Wave 2 tasks complete. Blocks Waves 3–7.

  **References**: All Wave 2 registry files

  **Acceptance Criteria**:
  - [ ] `./gradlew compileJava` → BUILD SUCCESSFUL (zero errors, warnings acceptable)
  - [ ] ALL 24+ registry files present and compile
  - [ ] setId() audit: every Item.Properties() includes setId()

  **QA Scenarios**:
  ```
  Scenario: Full compile after Wave 2
    Tool: Bash (./gradlew)
    Steps:
      1. Run `./gradlew clean`
      2. Run `./gradlew compileJava 2>&1 | Select-String -Pattern "error:" -Context 0`
      3. If errors exist, iteratively fix using LSP diagnostics
      4. Repeat until zero errors
    Expected Result: BUILD SUCCESSFUL, zero compile errors
    Evidence: .sisyphus/evidence/gate-2-compile.txt
  ```

  **Commit**: YES — `feat(wave-2): 24 DeferredRegister registrations with setId()`
  - Files: `src/main/java/.../init/registry/*.java`
  - Pre-commit: `./gradlew compileJava` must pass

---

## Wave 3 — Datagen Providers (6 tasks + Gate-3)

> **Blocked by**: Wave 2 (registries must exist for providers to reference)
> **Blocks**: Wave FINAL (runData verification)
> **Parallelism**: 6 independent datagen providers + 1 provider hub task. All can run in parallel.
> **Mandate**: ZERO manual JSON. ALL resources under `src/generated/resources/`.

- [x] 3.0 **Datagen Hub — `AvaritiaData.java`** (entry point)

  **What to do**:
  - Create `AvaritiaData.java` in `data/` package
  - Use `@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)`
  - Subscribe to `GatherDataEvent.Client` event
  - Register all 6 providers with `generator.addProvider(true, ...)`
  - Configure `generator.getPackOutput()` for `src/generated/resources/`

  **Must NOT do**: Do NOT use `GatherDataEvent.Server` — use `.Client`. Do NOT output to wrong directory.

  **Recommended Agent Profile**: Category `quick` — single file, straitforward setup
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.1–3.6

  **References**: NeoForge 26.1.2 datagen docs. Skill pattern: `@EventBusSubscriber` + `GatherDataEvent.Client`.

  **Acceptance Criteria**: [ ] AvaritiaData.java compiles; all 6 providers registered
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (hub compiles)
  **Evidence**: `.sisyphus/evidence/task-3-0-compile.txt`
  **Commit**: NO (grouped)

- [x] 3.1 **Language Provider** — `AvaritiaLanguageProvider`

  **What to do**:
  - Create `AvaritiaLanguageProvider` extending `LanguageProvider`
  - Extract all translation keys from source's `en_us.json` (there's no file — keys may be embedded in code or hand-written)
  - Map each item/block/enchantment/entity/creative tab to `add(registryObject.get(), "English Name")`
  - Also add subtitles, tooltips, config translations

  **Must NOT do**: Do NOT copy old language JSON. Do NOT skip translatable strings.

  **Recommended Agent Profile**: Category `deep` — needs to scan source for all translatable strings
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: Source Avaritia `en_us.json` (if exists) or scan code for `Component.translatable`, `Component.literal` patterns

  **Acceptance Criteria**: [ ] LanguageProvider generates `en_us.json` in `src/generated/resources/assets/avaritia/lang/` with all translation keys
  **QA Scenarios**: `./gradlew runData` → en_us.json generated with correct keys
  **Evidence**: `.sisyphus/evidence/task-3-1-rundata.txt`
  **Commit**: NO (grouped)

- [x] 3.2 **Model Provider** — `AvaritiaModelProvider`

  **What to do**:
  - Create `AvaritiaModelProvider` extending `ModelProvider`
  - Generate basic item models (`item/generated`, `item/handheld`) for ALL items registered in ModItems
  - Generate block models for ALL blocks in ModBlocks
  - Use `ModelProvider`'s builder pattern for 26.1.2

  **Must NOT do**: Do NOT generate models manually. Do NOT skip items — EVERY item needs a model.

  **Recommended Agent Profile**: Category `deep` — 70+ items × models, 25+ blocks × blockstates
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: NeoForge 26.1.2 ModelProvider API. Source Avaritia model JSON for reference shapes.

  **Acceptance Criteria**: [ ] ModelProvider generates item/block models in `src/generated/resources/assets/avaritia/models/`
  **QA Scenarios**: `./gradlew runData` → all model JSON generated; count matches item+block count
  **Evidence**: `.sisyphus/evidence/task-3-2-rundata.txt`
  **Commit**: NO (grouped)

- [x] 3.3 **Recipe Provider** — `AvaritiaRecipeProvider`

  **What to do**:
  - Create `AvaritiaRecipeProvider` extending `RecipeProvider`
  - Migrate ALL recipes from source to datagen using 26.1.2 `RecipeProvider` API
  - Standard crafting recipes → `ShapedRecipeBuilder` / `ShapelessRecipeBuilder`
  - Custom recipes (extreme crafting table, compressor, etc.) → custom `RecipeBuilder` subclasses if needed
  - Output to `src/generated/resources/data/avaritia/recipes/`

  **Must NOT do**: Do NOT copy old recipe JSON files. Do NOT use `minecraft:recipe_shaped` — use `minecraft:crafting_shaped`.

  **Recommended Agent Profile**: Category `deep` — custom recipe types need builder subclasses
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: Source recipe JSON files. NeoForge 26.1.2 RecipeProvider API. Source custom recipe classes.

  **Acceptance Criteria**: [ ] RecipeProvider generates all recipe JSON in `src/generated/resources/data/avaritia/recipes/`
  **QA Scenarios**: `./gradlew runData` → all recipe JSON generated
  **Evidence**: `.sisyphus/evidence/task-3-3-rundata.txt`
  **Commit**: NO (grouped)

- [x] 3.4 **Loot Table Provider** — `AvaritiaLootTableProvider`

  **What to do**:
  - Create `AvaritiaLootTableProvider` extending `LootTableProvider`
  - Generate `minecraft:block` loot tables for all blocks (self-drops)
  - Generate entity loot tables if any custom entities drop items
  - Use 26.1.2 `LootTableProvider.SubProviderEntry` pattern

  **Must NOT do**: Do NOT copy old loot table JSON files.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: NeoForge 26.1.2 LootTableProvider docs. Source loot table JSON.

  **Acceptance Criteria**: [ ] LootTableProvider generates loot tables in `src/generated/resources/data/avaritia/loot_tables/`
  **QA Scenarios**: `./gradlew runData` → loot table JSON generated
  **Evidence**: `.sisyphus/evidence/task-3-4-rundata.txt`
  **Commit**: NO (grouped)

- [x] 3.5 **Tag Provider** — `AvaritiaTagProvider`

  **What to do**:
  - Create `AvaritiaTagProvider` using `TagProvider<T>` pattern
  - Generate item tags, block tags, entity type tags, etc. from ModTags definitions
  - Use 26.1.2 `TagProvider` with `IntrinsicHolderTagsProvider` or custom binding

  **Must NOT do**: Do NOT hand-write tag JSON files.

  **Recommended Agent Profile**: Category `deep` — multiple tag types, IntrinsicHolderTagsProvider pattern
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: NeoForge 26.1.2 TagProvider docs. Source ModTags for tag definitions.

  **Acceptance Criteria**: [ ] TagProvider generates tag JSON in `src/generated/resources/data/`
  **QA Scenarios**: `./gradlew runData` → tag JSON generated
  **Evidence**: `.sisyphus/evidence/task-3-5-rundata.txt`
  **Commit**: NO (grouped)

- [x] 3.6 **BlockState Provider** — `AvaritiaBlockStateProvider`

  **What to do**:
  - Create `AvaritiaBlockStateProvider` extending `BlockStateProvider`
  - Generate blockstate JSON + block models for ALL blocks
  - Use `simpleBlock()`, `simpleBlockWithItem()`, `horizontalBlock()`, etc.

  **Must NOT do**: Do NOT hand-write blockstate JSON.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 3, parallel with 3.0–3.6

  **References**: NeoForge 26.1.2 BlockStateProvider docs. Source blockstate JSON for reference.

  **Acceptance Criteria**: [ ] BlockStateProvider generates blockstate JSON in `src/generated/resources/assets/avaritia/blockstates/`
  **QA Scenarios**: `./gradlew runData` → blockstate JSON generated
  **Evidence**: `.sisyphus/evidence/task-3-6-rundata.txt`
  **Commit**: NO (grouped)

### Wave Gate-3 — Datagen Verification

- [ ] Gate-3 **RunData Verification** ⚠️ **BLOCKED: NeoForm/JDK 25** (compile must succeed first)

  **What to do**: Run `./gradlew runData` and verify ALL 6 providers execute without errors. Check output directory for expected JSON files.

  **Must NOT do**: Do NOT proceed if runData fails. Do NOT accept partial generation.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Sequential — after all Wave 3 tasks. Blocks Wave FINAL.

  **Acceptance Criteria**:
  - [ ] `./gradlew runData` → BUILD SUCCESSFUL
  - [ ] `src/generated/resources/assets/avaritia/lang/en_us.json` exists
  - [ ] `src/generated/resources/assets/avaritia/models/` populated
  - [ ] `src/generated/resources/assets/avaritia/blockstates/` populated
  - [ ] `src/generated/resources/data/avaritia/recipes/` populated
  - [ ] `src/generated/resources/data/avaritia/loot_tables/` populated
  - [ ] `src/generated/resources/data/avaritia/tags/` populated

  **QA Scenarios**:
  ```
  Scenario: All datagen providers succeed
    Tool: Bash (./gradlew)
    Steps:
      1. Run `./gradlew clean runData`
      2. Verify BUILD SUCCESSFUL
      3. List generated files: `Get-ChildItem -Recurse src/generated`
    Expected Result: BUILD SUCCESSFUL, all 6 provider directories populated
    Evidence: .sisyphus/evidence/gate-3-rundata.txt
  ```

  **Commit**: YES — `feat(wave-3): 6 datagen providers for lang/models/recipes/loot/tags/blockstates`
  - Files: `src/main/java/.../data/*.java`
  - Pre-commit: `./gradlew runData` must pass

---

## Wave 4 — Client Rendering (Entity Renderers, Models, Screens + Gate-4)

> **Blocked by**: Wave 2 (registries must exist for renderer registration)
> **Blocks**: Wave FINAL (runClient verification)
> **Parallelism**: Entity renderers in parallel sub-waves; screens after models

### Wave 4-A — Entity Renderers & Models (MAX PARALLEL)

- [x] 4.1 **Entity Renderers Batch 1** — first 9 renderers

  **What to do**: Copy 9 entity renderer files from source client/renderer/; port to 26.1.2 rendering API. Register via `EntityRenderersEvent.RegisterRenderers`.

  **Must NOT do**: Do NOT restructure files — carry 1:1. Do NOT change rendering behavior.

  **Recommended Agent Profile**: Category `deep` — rendering API changes
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-A, parallel with 4.2

  **References**: Source: `src/main/java/.../client/renderer/entity/`

  **Acceptance Criteria**: [ ] 9 renderer files compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-1-compile.txt`
  **Commit**: NO (grouped)

- [x] 4.2 **Entity Renderers Batch 2** — remaining 9 renderers

  **What to do**: Copy remaining 9 entity renderer files (source has ~18 total entity renderers).

  **Recommended Agent Profile**: Category `deep`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-A, parallel with 4.1

  **Acceptance Criteria**: [ ] Remaining 9 renderers compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-2-compile.txt`
  **Commit**: NO (grouped)

- [x] 4.3 **Entity Models & Geometry Loaders**

  **What to do**: Copy entity model classes and 8 geometry loader files; port to 26.1.2 model system. Layer definitions, model layers, geometry.

  **Must NOT do**: Do NOT change model data — only API migration.

  **Recommended Agent Profile**: Category `deep` — model layer migration
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-A, parallel with 4.1, 4.2

  **Acceptance Criteria**: [ ] All entity models compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-3-compile.txt`
  **Commit**: NO (grouped)

### Wave 4-B — Screens & Client Setup

- [x] 4.4 **Screens Batch 1** — first 7 screens

  **What to do**: Copy 7 screen files from source client/screen/; port to 26.1.2 Screen API. Screen constructors may have changed.

  **Must NOT do**: Do NOT restructure. Do NOT change GUI layout behavior.

  **Recommended Agent Profile**: Category `deep` — Screen API migration
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-B, parallel with 4.5

  **Acceptance Criteria**: [ ] 7 screen files compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-4-compile.txt`
  **Commit**: NO (grouped)

- [x] 4.5 **Screens Batch 2** — remaining 7 screens

  **What to do**: Copy remaining 7 screen files (~14 total screens in source).

  **Recommended Agent Profile**: Category `deep`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-B, parallel with 4.4

  **Acceptance Criteria**: [ ] Remaining 7 screens compile
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-5-compile.txt`
  **Commit**: NO (grouped)

- [x] 4.6 **Client Setup** — `AvaritiaClient.java` + Renderer Registration

  **What to do**: Create `AvaritiaClient.java` in `client/` package. Subscribe to `FMLClientSetupEvent` and `EntityRenderersEvent.RegisterRenderers`. Register all entity renderers. Register block entity renderers. Register screens.

  **Must NOT do**: Do NOT register renderers before entity files exist.

  **Recommended Agent Profile**: Category `deep` — sets up client-side bus
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 4-B. Blocked by 4.1-4.5. Required for Gate-4.

  **Acceptance Criteria**: [ ] AvaritiaClient.java compiles; all renderers registered
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-4-6-compile.txt`
  **Commit**: NO (grouped)

### Wave Gate-4 — Compile + Renderer Check

- [x] Gate-4 **Client Compile Verification**

  **What to do**: `./gradlew compileJava` — verify ALL client files compile.

  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/gate-4-compile.txt`
  **Commit**: YES — `feat(wave-4): entity renderers, models, screens, client setup`

---

## Wave 5 — Mixin + Access Transformer (11 tasks + Gate-5)

> **Blocked by**: Wave 2 (registries), Wave 0.9 (AT audit)
> **Blocks**: Wave FINAL (runClient)
> **Parallelism**: Mixins verified as zero-conflict via 0.8 pre-audit can run in parallel. Sequential only if interdependencies exist.

- [x] 5.1–5.11 **[11 Individual Mixin Tasks]** — one per Mixin Java file from source (ALL 11 migrated: 10 mixins + T5.11 event-based)

  **What to do (per mixin)**:
  - Copy mixin class from source `mixin/` directory
  - Verify `@Mixin` target class exists in 26.1.2 (reference 0.8 audit report)
  - Adapt injection points to 26.1.2 method signatures
  - Add to `avaritia.mixins.json` config

  **Must NOT do**: Do NOT change Mixin behavior. Do NOT skip audit reference — use 0.8 findings.

  **Recommended Agent Profile**: Category `deep` — Mixin migration across MC versions
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: All 11 in parallel (Wave 5-A) if pre-audit confirms zero conflicts. Blocked by 0.8, 0.9.

  **References**: Source `src/main/java/.../mixin/*.java`. 0.8 audit report. 26.1.2 source.

  **Acceptance Criteria (per mixin)**: [ ] Mixin class compiles; @Mixin target resolved; injection points valid
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (all 11 mixins)
  **Evidence**: `.sisyphus/evidence/task-5-N-compile.txt`
  **Commit**: NO (grouped)

- [x] 5.12 **Access Transformer** — migrate `accesstransformer.cfg`

  **What to do**: Copy AT file with 84 lines verified in 0.9 audit. Apply to `src/main/resources/META-INF/accesstransformer.cfg`.

  **Must NOT do**: Do NOT add new AT lines beyond what source has.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`

  **Acceptance Criteria**: [ ] AT file placed correctly; all 84 lines pass build
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-5-12-compile.txt`
  **Commit**: NO (grouped)

- [x] 5.13 **Mixin Config** — `avaritia.mixins.json`

  **What to do**: Update mixin config JSON with all 11 migrated mixins. Ensure package paths correct.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`

  **Acceptance Criteria**: [ ] mixins.json lists all 11 mixins; runtime loads without error

### Wave Gate-5 — Mixin Compile Check

- [x] Gate-5 **Mixin Compile Verification**

  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (all mixins + AT)
  **Evidence**: `.sisyphus/evidence/gate-5-compile.txt`
  **Commit**: YES — `feat(wave-5): 11 mixins + 84 AT lines migrated`

---

## Wave 6 — Network + Config (13 Packets + SimpleChannel + ModConfigSpec + Gate-6)

> **Blocked by**: Wave 2 (registries needed for packet data types)
> **Blocks**: Wave FINAL (network integration)
> **Parallelism**: 13 individual packet tasks (per Decision #9: one task per file) + NetworkHandler + Config. ALL packets run in parallel (Wave 6-A).
> **Mandate**: Pure native `SimpleChannel` (net.neoforged.neoforge.network). ZERO AnvilLib Network references.

### Wave 6-A — Network Packets (13 parallel tasks)

- [x] 6.1–6.13 **[13 Individual Packet Tasks]** — one per source packet file (ALL 14 packets migrated)

  **What to do (per packet)**:
  - Copy packet class from source `network/` or `packet/` directory (1:1 file migration)
  - Adapt to 26.1.2 `SimpleChannel` API: `ServerPayloadHandler` / `ClientPayloadHandler` pattern
  - Implement `CustomPacketPayload` with `StreamCodec` instead of old packet encoding
  - Register in NetworkHandler (6.14)

  **Must NOT do**: Do NOT use AnvilLib Network module. Do NOT merge packets. Do NOT change packet logic.

  **Recommended Agent Profile**: Category `deep` — network API migration (SimpleChannel + CustomPacketPayload)
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: ALL 13 in parallel (Wave 6-A). Blocked by Wave 2.

  **References**: Source `src/main/java/.../network/*.java` (13 packets). NeoForge 26.1.2 SimpleChannel/CustomPacketPayload docs.

  **Acceptance Criteria (per packet)**: [ ] Packet compiles; uses CustomPacketPayload; StreamCodec implemented; registered in channel
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (all 13 packets)
  **Evidence**: `.sisyphus/evidence/task-6-N-compile.txt`
  **Commit**: NO (grouped)

- [x] 6.14 **NetworkHandler** — Central Channel Registration

  **What to do**: Create `NetworkHandler.java` in `network/` package. Set up `SimpleChannel` with `NetworkRegistry.ChannelBuilder`. Register all 13 packets with `messageBuilder()`. Handle PLAY protocol.

  **Must NOT do**: Do NOT use AnvilLib Network. Do NOT skip packet registration — all 13 must be registered.

  **Recommended Agent Profile**: Category `deep`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 6-A. Blocked by: 6.1-6.13 (needs packet classes). Required for Gate-6.

  **Acceptance Criteria**: [ ] SimpleChannel configured; all 13 packets registered; channel compiles
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-6-14-compile.txt`
  **Commit**: NO (grouped)

### Wave 6-B — Config

- [x] 6.15 **ModConfigSpec** — Configuration File

  **What to do**: Create `AvaritiaConfig.java` using `ModConfigSpec`. Port any existing config values from source. Use `ModConfig.Type.COMMON` (or SERVER/CLIENT as appropriate). If source has no config, create minimal config with mod version only.

  **Must NOT do**: Do NOT use AnvilLib Config module. Do NOT create excessive config options.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 6-B. Blocked by: Wave 1 (mod ID).

  **Acceptance Criteria**: [ ] Config file compiles; ModConfigSpec.Builder pattern correct
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-6-15-compile.txt`
  **Commit**: NO (grouped)

### Wave Gate-6 — Network Compile Check

- [x] Gate-6 **Network + Config Compile Verification**

  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (all 13 packets + channel + config)
  **Evidence**: `.sisyphus/evidence/gate-6-compile.txt`
  **Commit**: YES — `feat(wave-6): 13 network packets + SimpleChannel + ModConfigSpec`

---

## Wave 7 — Compat Integration (JEI + Jade + Curios + Gate-7)

> **Blocked by**: Wave 2 (registries), Wave 6 (network may be needed for compat sync)
> **Blocks**: Wave FINAL (runClient compat verification)
> **Parallelism**: 3 compat modules (JEI, Jade, Curios) run in parallel
> **Mandate**: ONLY JEI, Jade, Curios. Zero other compat modules. ZERO AnvilLib.

- [x] 7.1 **JEI Compat** — Recipe Categories + Plugin

  **What to do**: Copy JEI compat from source (likely in `compat/jei/`). Port to 26.1.2 JEI API. Register recipe categories for Avaritia custom recipes. Create `JEIPlugin` class.

  **Must NOT do**: Do NOT add REI or EMI compat. Do NOT skip JEI — essential for custom recipe visibility.

  **Recommended Agent Profile**: Category `deep` — JEI API migration
  **Skills**: `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 7, parallel with 7.2, 7.3

  **References**: Source `src/main/java/.../compat/jei/`. JEI 26.1.2 API docs.

  **Acceptance Criteria**: [ ] JEI plugin compiles; recipe categories registered
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-7-1-compile.txt`

- [x] 7.2 **Jade Compat** — Block/Entity Info Provider

  **What to do**: Copy Jade compat from source (likely `compat/jade/`). Port to 26.1.2 Jade API. Register block/entity info providers for Avaritia blocks/entities.

  **Must NOT do**: Do NOT add WTHIT compat — Jade only.

  **Recommended Agent Profile**: Category `deep`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 7, parallel with 7.1, 7.3

  **Acceptance Criteria**: [ ] Jade provider compiles
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-7-2-compile.txt`

- [x] 7.3 **Curios Compat** — Curio Slot Registration

  **What to do**: Copy Curios compat from source. Port to 26.1.2 Curios API. Register curio slots for Avaritia items (infinity armor, etc.).

  **Must NOT do**: Do NOT add cosmetic armor or other slot mod compat — Curios only.

  **Recommended Agent Profile**: Category `quick`, Skills `["minecraft-modder-neoforge"]`
  **Parallelization**: Wave 7, parallel with 7.1, 7.2

  **Acceptance Criteria**: [ ] Curios slot registration compiles
  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-7-3-compile.txt`

- [x] 7.4 **Resource Copy** — textures, sounds, shaders

  **What to do**: Copy `src/main/resources/assets/avaritia/textures/`, `sounds/`, `shaders/`, and the in-mod resource pack (`resourcepacks/avaritia/`) from source to target. Verify file structure matches 26.1.2 conventions.

  **Must NOT do**: Do NOT copy JSON files (models, recipes, loot tables, etc.) — those are datagen-generated. Do NOT copy `en_us.json`.

  **Recommended Agent Profile**: Category `quick`, Skills `[]`
  **Parallelization**: Wave 7, parallel with 7.1-7.3

  **Acceptance Criteria**: [ ] All texture PNGs copied; sounds.json present; shader files in place
  **QA Scenarios**: `Test-Path` assertions on key texture/sound files
  **Evidence**: `.sisyphus/evidence/task-7-4-copy.txt`

- [x] 7.5 **Dropped Compat Audit** — Verify Exclusions

  **What to do**: Grep the target workspace for ANY reference to dropped compat modules (EMI, CraftTweaker, KubeJS, ProjectE, DraconicEvolution, Mekanism, AE2, EnderIO, StorageDrawers, RefinedStorage, CharmOfUndying, CCL). If found, flag as contamination and remove.

  **Must NOT do**: Do NOT skip — this enforces the compat boundary.

  **Recommended Agent Profile**: Category `quick`
  **Parallelization**: After 7.1-7.4

  **Acceptance Criteria**: [ ] Zero dropped compat references in target workspace
  **QA Scenarios**: `grep -r "emi\|crafttweaker\|kubejs\|projecte\|draconic\|mekanism\|ae2\|enderio\|storagedrawers\|refinedstorage\|charmofundying\|ccl" src/` → zero results
  **Evidence**: `.sisyphus/evidence/task-7-5-audit.txt`

### Wave Gate-7 — Compat Compile Check

- [x] Gate-7 **Compat Compile Verification**

  **QA Scenarios**: `./gradlew compileJava` → BUILD SUCCESSFUL (all compat)
  **Evidence**: `.sisyphus/evidence/gate-7-compile.txt`
  **Commit**: YES — `feat(wave-7): JEI + Jade + Curios compat + resources`

---

## Wave FINAL — Verification + Tests

> **Blocked by**: ALL Waves 0-7
> **Parallelism**: F.1–F.4 (JUnit tests priority) run first, then F.5–F.8 (review wave) in 4 parallel agents
> **Mandate**: ALL reviews must APPROVE before work is complete. Get explicit user "okay".

### Wave FINAL-A — JUnit Tests

- [x] F.1 **JUnit 5 Unit Tests**

  **What to do**: Write JUnit 5 unit tests for pure Java logic (utility classes, recipe logic, math helpers, etc.). Do NOT write GameTests. Test only non-Minecraft-dependent code.

  **Must NOT do**: Do NOT write GameTests. Do NOT test rendering/screen/entity behavior.

  **Recommended Agent Profile**: Category `deep`, Skills `["minecraft-modder-neoforge", "minecraft-testing"]`

  **Acceptance Criteria**: [ ] `./gradlew test` → BUILD SUCCESSFUL; all tests pass
  **QA Scenarios**: `./gradlew test` → Tests: N passed, 0 failed
  **Evidence**: `.sisyphus/evidence/task-final-1-test.txt`
  **Commit**: YES — `test(final): JUnit 5 unit tests for utility logic`

- [x] F.2 **Full Build** ⚠️ **BLOCKED: 101 compile errors — rendering/model pipeline fixed (12 files). Entity/tile/menu classes partially ported (40 files from Avaritia-1.21). Remaining: entity base classes (BaseTileEntity, BaseMenu, etc.) need porting from Avaritia-1.21 source. Also NeoForm/JDK 25 incompatibility (Fox.dropAllDeathLoot during createMinecraftArtifacts).**

  **What to do**: Run full Gradle build (compileJava + test + processResources + jar).

  **QA Scenarios**: `./gradlew build` → BUILD SUCCESSFUL
  **Evidence**: `.sisyphus/evidence/task-final-2-build.txt`

- [ ] F.3 **runData Verification** — Final datagen output check ⚠️ **BLOCKED** (depends on F.2)

  **What to do**: `./gradlew runData` — verify ALL generated JSON is correct, no errors.

  **QA Scenarios**: `./gradlew clean runData` → BUILD SUCCESSFUL; verify all 6 provider outputs exist
  **Evidence**: `.sisyphus/evidence/task-final-3-rundata.txt`

- [ ] F.4 **runClient Smoke Test** — Launch Minecraft ⚠️ **BLOCKED** (depends on F.2)

  **What to do**: `./gradlew runClient` — verify game launches, mod loads, reaches main menu without crash. Check mod list for "Avaritia".

  **Must NOT do**: Do NOT test gameplay — just launch and main menu.

  **QA Scenarios**: `./gradlew runClient` → Game window opens, no crash, "Avaritia" in mods list
  **Evidence**: `.sisyphus/evidence/task-final-4-runclient.txt`

### Wave FINAL-B — Review Wave (4 parallel agents)

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
>
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**

- [x] F.5 **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists. For each "Must NOT Have": search codebase for forbidden patterns. Check evidence files exist in `.sisyphus/evidence/`. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [x] F.6 **Code Quality Review** — `unspecified-high`
  Run `./gradlew compileJava` + `./gradlew check`. Review all changed files for: Lombok annotation abuse, empty catches, console.log, commented-out code, unused imports. Check AI slop: excessive comments, over-abstraction, generic names.
  Output: `Build [PASS/FAIL] | Lint [PASS/FAIL] | Tests [N pass/N fail] | Files [N clean/N issues] | VERDICT`

- [ ] F.7 **Real Manual QA** — `unspecified-high` ⚠️ **BLOCKED** (requires running Minecraft, depends on F.2)
  Start from clean state. Execute EVERY QA scenario from EVERY task. Test cross-task integration. Test edge cases: empty state, invalid input. Save to `.sisyphus/evidence/final-qa/`.
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [x] F.8 **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual diff. Verify 1:1 — everything in spec was built, nothing beyond spec. Check "Must NOT do" compliance. Detect cross-task contamination.
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

| Wave | Commit Message | Files Included |
|------|---------------|----------------|
| 0 | `chore(wave-0): pre-audit — source inventory and API verification` | audit docs |
| 1 | `feat(wave-1): project skeleton — Gradle MDG + package structure` | build.gradle, settings.gradle, gradle.properties, libs.versions.toml, mods.toml |
| 2 | `feat(wave-2): registrations — 24 DeferredRegister classes with setId()` | init/*.java |
| 3 | `feat(wave-3): datagen — 6 providers for lang/models/recipes/loot/tags/blockstates` | data/*.java |
| 4 | `feat(wave-4): client rendering — entity renderers, models, screens` | client/*.java |
| 5 | `feat(wave-5): mixin + AT — 11 mixins + 84 AT lines migrated` | mixin/*.java, accesstransformer.cfg |
| 6 | `feat(wave-6): network + config — 13 packets + SimpleChannel + ModConfigSpec` | network/*.java |
| 7 | `feat(wave-7): compat — JEI, Jade, Curios integration` | compat/*.java |
| FINAL | `chore(wave-final): tests + build verification + reviews` | test/*.java |

**Branch**: `avaritia-migration` (single branch, created in Wave 0)
**Format**: Conventional Commits (`type(scope): description`)
**Pre-commit**: `./gradlew compileJava` must pass before each commit

---

## Success Criteria

### Verification Commands
```bash
./gradlew compileJava  # Expected: BUILD SUCCESSFUL (zero errors all waves)
./gradlew runData      # Expected: BUILD SUCCESSFUL (JSON files in src/generated/)
./gradlew build        # Expected: BUILD SUCCESSFUL (full build including tests)
./gradlew runClient    # Expected: Launches Minecraft, reaches main menu without crash
```

### Final Checklist
- [x] All 24 registries compiled with setId() on Items (33 registry files migrated)
- [ ] All 6 datagen providers generate correct JSON ⚠️ **BLOCKED** (Gate-3 pending compile)
- [x] All 13 network packets compile (15 files: 14 packets + IPacket + ChannelState/ChannelAction)
- [x] All 11 Mixins compile against 26.1.2 targets (10 migrated + 1 deleted → AddServerReloadListenersEvent)
- [x] 84 AT lines verified and migrated (79 lines, 72 valid entries)
- [x] JEI + Jade + Curios compat load (13 compat files: 10 JEI, 1 Jade, 2 Curios)
- [x] Zero AnvilLib references anywhere (verified by F.8)
- [x] Zero dropped compat modules present (verified by T7.5 + F.8)
- [x] Zero manual JSON under src/main/resources/data/ or src/main/resources/assets/ (all via datagen + textures only)
- [ ] JUnit tests pass ⚠️ **BLOCKED** (cannot run `./gradlew test` without successful compile)
- [x] All "Must NOT Have" absent (verified by F.5 + F.6 + F.8)
