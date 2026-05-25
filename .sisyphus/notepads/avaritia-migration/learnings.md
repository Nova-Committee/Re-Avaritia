
## 2026-05-25 Gate-2 compile validation
- Minecraft 26.1.2 mappings use `net.minecraft.resources.Identifier` instead of the old `net.minecraft.resources.ResourceLocation`; registry helper methods and generic callback types in `com/avaritia/init` must use `Identifier` consistently.
- `Util` is under `net.minecraft.util.Util`, not `net.minecraft.Util`.
- Advancement predicate package is `net.minecraft.advancements.criterion`, not `net.minecraft.advancements.critereon`.
- After import fixes, compile is still blocked by deferred Wave 4 missing Avaritia implementation packages/classes (blocks, items, entities, components, menus/screens, crafting, particles, singularity core).

## Wave 3 Task 3.1 — AvaritiaLanguageProvider
- 新增 `src/main/java/com/avaritia/data/AvaritiaLanguageProvider.java`，继承 `net.neoforged.neoforge.common.data.LanguageProvider`，构造器为 `AvaritiaLanguageProvider(PackOutput output, String locale)`，`AvaritiaData` 使用 `new AvaritiaLanguageProvider(packOutput, "en_us")` 注册。
- NeoForge 26.1.2 的 `LanguageProvider` 构造签名是 `(PackOutput, String modid, String locale)`，可直接用 `add(Item/Block/EntityType/MobEffect, String)` 和 `add(String, String)`。
- 参考旧版 `Avaritia-1.21/src/main/resources/assets/avaritia/lang/en_us.json` 提取文本；用户给出的 `LangGenerator.java` 路径不存在，旧版项目也未找到同名文件。
- 当前目标项目还缺少大量 Wave 4+ common/client 类与其他 datagen provider 依赖，`compileJava`/`runData` 会在非本任务文件（如 `AvaritiaBlockStateProvider` 的旧 generator import、`ModBlocks` 缺少 common.block 类、`ModDataComponents` 缺少 component 类）失败；未发现输出中指向本次修改文件的编译错误。

## Wave 4.1 — Entity Renderers Batch 2 (4 files)
- 迁移文件: `HeavenArrowRender`, `TracerArrowRender`, `HeavenSubArrowRender`, `TNTProEntityRender`
- 源路径: `committee.nova.mods.avaritia.client.render.entity` → 目标: `com.avaritia.client.render.entity`
- 所有 4 个文件编译通过 (0 errors)，构建错误均为其他文件的预存问题
- 关键 API 迁移:
  - `ResourceLocation` → `Identifier` (import `net.minecraft.resources.Identifier`)
  - `Res.HEAVEN_ARROW` → 已在 `com.avaritia.Res` 新增 `HEAVEN_ARROW` 常量，使用 `Avaritia.rl("textures/entity/heaven_arrow.png")`
  - `ArrowRenderer.getTextureLocation()` 返回类型已改为 `Identifier`
  - `TextureAtlas.LOCATION_BLOCKS` 仍存在，类型自动适配 `Identifier`
  - `MultiBufferSource`, `PoseStack`, `Matrix4f/Matrix3f`, `OverlayTexture`, `Mth`, `Axis` 均保持不变
  - `@OnlyIn(Dist.CLIENT)` 保持不变
- `TNTProEntityRender` 使用 `BlockRenderDispatcher` + `TntMinecartRenderer.renderWhiteSolidBlock()` 模式，API 未变化
- `HeavenArrowRender` 有自定义顶点渲染 (`RenderType.create` + `DefaultVertexFormat.NEW_ENTITY`)，API 未变化
- 所有 renderer 引用的 entity 类 (`HeavenArrowEntity`, `TraceArrowEntity`, `HeavenSubArrowEntity`, `TNTProEntity`) 尚未迁移，等待 Wave 5+ entity 迁移

## Wave 4.2 — Screen Files (8 files: 7 target + BaseContainerScreen dependency)
- 迁移文件: `InfinityChestScreen`, `ExtremeAnvilScreen`, `CompressedChestScreen`, `AvaritiaConfigScreen`, `NeutronCompressorScreen`, `ExtremeSmithingScreen`, `ItemFilterScreen`
- 额外依赖: `BaseContainerScreen` (被 ExtremeAnvilScreen, CompressedChestScreen, NeutronCompressorScreen 继承)
- 源路径: `committee.nova.mods.avaritia.client.screen` → 目标: `com.avaritia.client.screen`
- 关键 API 迁移:
  - `ResourceLocation` → `Identifier` (所有文件)
  - `ResourceLocation.tryParse(x)` → `Identifier.parse(x)` (ExtremeSmithingScreen)
  - `BuiltInRegistries.ITEM` → `Registries.ITEM` (ItemFilterScreen)
  - 移除 Lombok `@Getter` → 手动生成 getter 方法 (ItemFilterScreen: `getSelectedItemId()`, `getScrollOffset()`)
  - `BaseContainerScreen` 中 `ResourceLocation bgTexture` → `Identifier bgTexture`
  - 包引用: `committee.nova.mods.avaritia.*` → `com.avaritia.*`
- 依赖关系: 这些 screen 引用的 menu/network/tile/config 类尚未迁移 (如 `InfinityChestMenu`, `ModConfig`, `NetworkHandler` 等)，将在后续 wave 解决

## Wave 4.2 — Tile Entity Renderers + Item Renderer (3 files)
- 迁移文件: `InfinityChestBlockRender`, `InfinitatoTileRender`, `InfinityShieldRender`
- 源路径: `committee.nova.mods.avaritia.client.render.{tile,item}` → 目标: `com.avaritia.client.render.{tile,item}`
- 关键 API 迁移:
  - `Const.rl(...)` → `Avaritia.rl(...)` (返回 `Identifier`)
  - `BrightnessCombiner<>().acceptNone().get(pPackedLight)` → 直接使用 `pPackedLight` (26.1.2 已移除 BrightnessCombiner)
  - `AvaritiaModClient.INFINITY_CHEST` / `AvaritiaModClient.INFINITY_SHIELD` → 在目标中通过 `ModelLayerLocation` 引用（类尚未迁移）
  - `Res.SHIELD_TEX` → `Avaritia.rl("textures/item/tools/infinity_shield/layer_0.png")`
  - `BlockEntityRenderer<InfinityChestTile>` / `BlockEntityRenderer<InfinitatoTile>` 类保持不变
  - `BlockEntityRendererProvider.Context` 构造模式保持不变（26.1.2 兼容）
  - `BlockEntityWithoutLevelRenderer` + `BlockEntityRenderDispatcher, EntityModelSet` 构造签名保持不变
  - `ItemRenderer.getFoilBufferDirect()` / `ItemRenderer.getFoilBuffer()` API 保持不变
  - `Sheets.CHEST_SHEET` / `Sheets.translucentCullBlockSheet()` 保持不变
  - `Material`, `RenderType`, `MultiBufferSource`, `PoseStack`, `Axis`, `ModelPart`, `LayerDefinition` 均保持不变
- `InfinitatoTileRender` 为完全注释状态——源文件 159 行全部为 `//` 注释

## Wave T4.4 — Client Model Entities + Particles (6 files)
- 迁移文件: `InfinityArmorModel.java`, `InfinityTridentModel.java`, `InfinityShieldModel.java` (模型), `ShockwaveParticleOptions.java`, `ShockwaveParticle.java`, `ChargeParticle.java` (粒子)
- 源路径: `committee.nova.mods.avaritia.client.model.entity` / `committee.nova.mods.avaritia.client.particle`
- 目标路径: `com.avaritia.client.model.entity` / `com.avaritia.client.particle`
- 关键 API 迁移:
  - 所有包: `committee.nova.mods.avaritia.*` → `com.avaritia.*`
  - `Res.EYE_TEX` / `Res.WING_GLOW_TEX` → 在 `InfinityArmorModel` 中添加为私有静态字段，使用 `Avaritia.rl("textures/models/armor/...")` 创建
  - `Const.rl()` / `Const.MOD_ID` → 全部移除，使用 `Avaritia.rl()` 和 `Avaritia.MOD_ID`
  - `ResourceLocation` → `Identifier` (import `net.minecraft.resources.Identifier`)
  - `BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(...))` → `Registries.PARTICLE_TYPE.get(Identifier.tryParse(...))`
  - `net.minecraft.network.FriendlyByteBuf` → `net.minecraft.network.RegistryFriendlyByteBuf` (26.1.2 重命名)
  - `net.minecraft.core.registries.BuiltInRegistries` → `net.minecraft.core.registries.Registries`
- 模型文件 (InfinityArmorModel, InfinityTridentModel, InfinityShieldModel): `HumanoidModel`, `Model`, `ModelPart`, `LayerDefinition`, `CubeDeformation`, `PartPose`, `VertexConsumer` 等 API 均未变化
- 粒子文件 (ShockwaveParticle, ChargeParticle): `TextureSheetParticle`, `ParticleProvider`, `SpriteSet`, `ParticleRenderType`, `SimpleParticleType` API 均未变化
- ShockwaveParticle 自定义顶点渲染使用 `Consumer<Quaternionf>`, `Axis.YP.rotation()`, `Axis.XP.rotation()` API 保持不变
- 所有 6 个文件无 Lombok、无 `committee.nova` 引用、无 `Const.MOD_ID/Const.rl` 引用
- 引用尚未迁移的类: `ModItems` (com.avaritia.init.registry), `ModParticles` (com.avaritia.init.registry), `AvaritiaForgeClient` (com.avaritia.client), `AvaritiaShaders` (com.avaritia.client.shader), `ColorRGBA`, `Res` - 等待后续 Wave 迁移
- `InfinityArmorModel.render()` 方法依赖 `Res.ARMOR_MASK`, `Res.ARMOR_MASK_INV`, `Res.ARMOR_WING_MASK` (TextureAtlasSprite)，以及 `AvaritiaRenderTypes.COSMIC_ARMOR`, `AvaritiaRenderTypes.Glow()`, `AvaritiaRenderTypes.WingGlow()` 和 `AvaritiaShaders.cosmicArmor*` uniforms，均在 target 中已存在

## Wave 4.8 — Client Screens Batch 2 (11 files + 3 prereqs)
- 迁移文件: InfinityClockScreen, NeutronRingScreen, NeutronCollectorScreen, SculkCraftScreen, NetherCraftScreen, EndCraftScreen, ExtremeCraftScreen, SideConfigButton, SideConfigScreen, SideButton, GuiElementAccess
- 前置迁移: `IDataReceiver.java` (api/iface), `BaseContainerScreen.java` (api/client/screen) — 所有 screen 的基类
- `Res.java` 新增 15 个 texture constants (GUI crafting/machine/chest 纹理)
- 关键 API 迁移:
  - `ResourceLocation` → `Identifier` (import `net.minecraft.resources.Identifier`)
  - `Const.rl("textures/gui/...")` → `Avaritia.rl("textures/gui/...")` (Res.java) 或直接 `Identifier.of(Avaritia.MOD_ID, ...)`
  - `BaseContainerScreen` 字段 `protected ResourceLocation bgTexture` → `protected Identifier bgTexture`
  - `Const.MOD_ID` → `Avaritia.MOD_ID` (NeutronRingScreen 直接使用)
- Lombok 移除: `SideConfigScreen` 的 `@Getter` 注解 → 手动添加 `getGuiLeft()` / `getGuiTop()` 方法
- 所有 14 个文件(含 prereqs)通过 regrex 验证: 零 `committee.nova` 引用、零 `ResourceLocation`、零 `Const.MOD_ID/Const.rl`、零 Lombok
- 未解决的依赖引用(等待后续 Wave):
  - Menu 类: `InfinityClockMenu`, `NeutronRingMenu`, `NeutronCollectorMenu`, `TierCraftMenu` (在 `com.avaritia.common.menu`)
  - 网络包: `C2SSetTimePacket` (在 `com.avaritia.common.net`)
  - Client 类: `AvaritiaForgeClient.RING_KEY` (在 `com.avaritia.client`)
  - API 接口: `ITileIO` (在 `com.avaritia.api.iface`)
  - Menu 基类: `BaseTileMenu` (在 `com.avaritia.api.common.menu`)
  - 配置/IO: `SideConfiguration` (在 `com.avaritia.core.io`), `NetworkHandler` (在 `com.avaritia.init.handler`)
  - Tooltips: `ModTooltips` 在 target 中已存在 (`com.avaritia.init.registry.ModTooltips`)

## Wave 4.1 �� Entity Renderers Batch 1 (4 files)
- Ǩ��/ȷ���ļ�: `InfinityThrownTridentRender`, `InfinityArmorRender`, `GapingVoidRender`, `BurningBallRender`
- Դ·��: `committee.nova.mods.avaritia.client.render.entity` �� Ŀ��: `com.avaritia.client.render.entity`
- �ؼ� API Ǩ��:
  - `ResourceLocation` �� `Identifier`��`getTextureLocation()` ���� `Identifier`
  - `Const.rl(...)` �� `Avaritia.rl(...)`��Ŀ������ helper������ `Identifier`��
  - ������ȫ����Ϊ `com.avaritia.*`
  - `Res.TRIDENT_TEX`, `Res.WING_TEX`, `Res.VOID`, `Res.DRAGON_FIREBALL` ����Ŀ�� `Res` �п���
- ���Ƴ������� renderer �о�Դ�ļ�Я���� author/date/version ͷ��Ϣ���ĸ��ļ����� Lombok���� `committee.nova`���� `ResourceLocation`���� `Const.*`��
- `InfinityThrownTridentRender` ���� OBJParser + CCRenderState + `AvaritiaRenderTypes.TRIDENT` ��Ⱦ�߼���ʵ���� `InfinityThrownTrident` ���������� entity Ǩ�ơ�
- `GapingVoidRender` ���� void color/scale/halo �߼��� hemisphere OBJ ��Ⱦ��halo ���������Դ�ļ��б���Ϊע�ͣ�����ע��״̬��
- `BurningBallRender` ���� dragon fireball billboard ��Ⱦ�� 15 ���ȸ��ǡ�
- ��֤: glob ȷ�� 4 ��Ŀ���ļ����ڣ����ļ� grep ȷ�ϱ������޽�������/ͷ��Ϣ��`lsp_diagnostics` �򱾻�ȱ�� `jdtls` �޷����У�`./gradlew.bat compileJava` ��Ԥ��Ǳ������ļ� UTF-8 �������ʧ�ܣ��� `api/client/util/TextureUtils.java`, `api/utils/WorldUtils.java`, ��� client model loader����δ����/δ���汾���� renderer ����

- Wave 4.1 renderer migration: HeavenArrowRender, TracerArrowRender, HeavenSubArrowRender, and TNTProEntityRender live under com.avaritia.client.render.entity; renderer texture return type is net.minecraft.resources.Identifier and mod textures should be constructed with Identifier.of(Avaritia.MOD_ID, path) when migrating away from old Res/ResourceLocation constants.

- Wave 4.2 renderer follow-up: migrated tile/item renderer resource creation now uses direct `Identifier.of(Avaritia.MOD_ID, path)` inside renderer files for model layers and textures; kept `Avaritia.rl` unchanged globally to avoid broad, unrelated changes. `compileJava` is still blocked by pre-existing non-renderer UTF-8 source encoding errors, and filtered compile output shows no errors for the three migrated renderer files.

## Wave 4 client models/particles migration (6 files)
- Migrated entity model files under src/main/java/com/avaritia/client/model/entity/: InfinityArmorModel, InfinityTridentModel, InfinityShieldModel; removed old author/date headers and retained render logic without Lombok.
- Migrated particle files under src/main/java/com/avaritia/client/particle/: ShockwaveParticleOptions, ShockwaveParticle, ChargeParticle; FriendlyByteBuf -> RegistryFriendlyByteBuf, ResourceLocation -> Identifier, 
et.minecraft.Util -> 
et.minecraft.util.Util.
- ShockwaveParticleOptions follows target ModParticles signature: StreamCodec<? super RegistryFriendlyByteBuf, ShockwaveParticleOptions> and resolves trail particles through BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.tryParse(...)).
- Verification: glob found all 6 target files; grep found no committee.nova, Const, ResourceLocation, Lombok, or old author/date headers in migrated model files. LSP unavailable because jdtls is not installed. ./gradlew.bat compileJava is still blocked by pre-existing UTF-8 encoding errors in unrelated files such as TextureUtils.java, WorldUtils.java, and model loader classes, before reaching migrated-file semantic errors.

## AvaritiaClient.java — Client Event Bus Registration

- Created `src/main/java/com/avaritia/client/AvaritiaClient.java` — centralized client-side event subscription class
- Package: `com.avaritia.client`, annotated with `@EventBusSubscriber(modid = Avaritia.MOD_ID, value = Dist.CLIENT)`
- **Entity renderers** registered via `EntityRenderersEvent.RegisterRenderers` (NOT old `FMLClientSetupEvent` + `EntityRenderers.register()`):
  - 2 vanilla-based: IMMORTAL→ItemEntityRenderer, ENDER_PEARL→ThrownItemRenderer
  - 7 custom from `render/entity/*`: GAPING_VOID→GapingVoidRender, HEAVEN_ARROW→HeavenArrowRender, HEAVEN_SUB_ARROW→HeavenSubArrowRender, BURNING_BALL→BurningBallRender, TRACE_ARROW→TracerArrowRender, TNT_PRO→TNTProEntityRender, INFINITY_THROWN_TRIDENT→InfinityThrownTridentRender
  - 1 from `render/tile/*` (but extends EntityRenderer): ACCELERATOR_DISPLAY→AcceleratorDisplayRender
  - 8 missing entity render classes commented out (NeutronArrowRender, ExplosionsArrowRender, BurningArrowRender, FireBallRender, BladeSlashRender, SunProRender, RainProRender, StormProRender) — expected to cause errors when uncommented
- **Block entity renderers** via same `EntityRenderersEvent.RegisterRenderers` event:
  - compressed_chest_tile→CompressedChestRenderer, INFINITY_CHEST_TILE→InfinityChestBlockRender
  - InfinitatoTileRender commented out — `infinitato_tile` not yet registered in ModTileEntities, InfinitatoTile class not yet created
- **InfinityArmorRender** NOT registered via RegisterRenderers (it extends `RenderLayer`, not `EntityRenderer`) — registered via `EntityRenderersEvent.AddLayers` as player armor layer
  - Constructor: `InfinityArmorRender(RenderLayerParent, EntityModelSet, boolean isSlim)`
  - Player layer: `playerRenderer.addLayer(new InfinityArmorRender<>(playerRenderer, entityModels, false))`
  - Skin layers: iterates `event.getSkins()`, checks `skin == PlayerSkin.Model.SLIM`
- **Screens** delegated to `ModMenus.onClientSetup(RegisterMenuScreensEvent)` — preserves existing 12-screen registration
- **Model loaders** via `ModelEvent.RegisterGeometryLoaders` (NOT old `ModelEvent.RegisterLoaders`):
  - 8 loaders: cosmic, cosmic_arc, hell, eternal, unstable, halo, halo_cosmic, halo_eternal
  - Each has `public static final INSTANCE` singleton
- **Particles** via `RegisterParticleProvidersEvent`:
  - `ModParticles.CHARGE.get()` → `ChargeParticle.Factory::new` — **Factory inner class does NOT exist yet** (expected error)
  - `ModParticles.SHOCKWAVE_PARTICLE.get()` → `ShockwaveParticle.Provider::new` — Provider exists
- **Key API differences** from old code:
  - `ModelEvent.RegisterLoaders` → `ModelEvent.RegisterGeometryLoaders` (26.1.2 rename)
  - `Identifier.fromNamespaceAndPath()` / `Identifier.of()` instead of `ResourceLocation`
  - `Avaritia.rl()` via `Avaritia.MOD_ID` instead of `Const.rl()` via `Const.MOD_ID`
- **LSP verification**: jdtls not available in environment; IDE inspection tools returned schema errors
- **Known issues**: `ChargeParticle.Factory` inner class missing; `AcceleratorDisplayEntity` class not yet created; `InfinitatoTile` class not yet created

- 2026-05-25: 新增 com.avaritia.client.AvaritiaClient 作为 NeoForge 26.1.2 客户端 MOD 事件订阅入口；渲染器使用 EntityRenderersEvent.RegisterRenderers，屏幕使用 RegisterMenuScreensEvent，模型加载器在本地 26.1.2.15-beta API 中对应 ModelEvent.RegisterLoaders（不是旧参考里的 RegisterGeometryLoaders），粒子使用 RegisterParticleProvidersEvent.registerSpriteSet。当前全量 compileJava 会先被既有非 UTF-8 源文件编码错误阻断，LSP 也因 jdtls 未安装不可用。

## Wave 4.1 entity renderer migration
- Migrated entity renderer batch into src/main/java/com/avaritia/client/render/entity/: InfinityThrownTridentRender, InfinityArmorRender, GapingVoidRender, BurningBallRender.
- Package/import migration pattern used: committee.nova.mods.avaritia -> com.avaritia; texture return type/imports use 
et.minecraft.resources.Identifier; OBJ locations use Identifier.of(Avaritia.MOD_ID, ...) per 26.1.2 target convention.
- InfinityThrownTridentRender and GapingVoidRender keep OBJParser + CCRenderState rendering and bind target AvaritiaRenderTypes render types; source Const.rl(...) was replaced with Identifier.of(Avaritia.MOD_ID, ...).
- BurningBallRender uses target Identifier texture return type and existing Res.DRAGON_FIREBALL; InfinityArmorRender uses target InfinityArmorModel and RenderType.armorCutoutNoCull(Res.WING_TEX).
- Verification note: gradlew compileJava currently fails before renderer-specific checking due pre-existing non-UTF-8/unmappable characters in unrelated files such as pi/client/util/TextureUtils.java and pi/utils/WorldUtils.java; LSP diagnostics unavailable because jdtls is not installed on PATH.


- Migrated first 7 client screen files in com/avaritia/client/screen for 26.1.2: InfinityChestScreen, ExtremeAnvilScreen, CompressedChestScreen, AvaritiaConfigScreen, NeutronCompressorScreen, ExtremeSmithingScreen, ItemFilterScreen. Target screen migration currently uses GuiGraphicsExtractor/extract* patterns for 26.1.2, with ScreenTextures centralizing migrated texture Identifiers. Verification note: lsp_diagnostics unavailable because jdtls is not installed; compileJava is blocked by pre-existing non-UTF-8 source encoding errors outside screen files (e.g. api/client/util/TextureUtils.java, api/utils/WorldUtils.java, client/model/loader/*).

## 2026-05-25 UTF-8 注释修复
- 任务：修复 12 个迁移后 Java 文件中的中文 Javadoc/`//` 注释乱码，原始来源为 `Avaritia-1.21/src/main/java/committee/nova/mods/avaritia`，目标为 `Avaritia-26/src/main/java/com/avaritia`。
- 方法：按注释块顺序从原始文件恢复注释文本，并仅在注释内应用迁移文本替换（`committee.nova.mods.avaritia`→`com.avaritia`、`ResourceLocation`→`Identifier`、`BuiltInRegistries`→`Registries`、`Const.*`→迁移后形式），非注释代码 span 做一致性校验后写回 UTF-8。
- 覆盖文件：TextureUtils、WorldUtils、CosmicArcBakeModel、CosmicArcModelLoader、CosmicBakeModel、EternalBakeModel、HaloCosmicBakedModel、HaloCosmicModelLoader、HaloEternalBakedModel、HaloEternalModelLoader、HellBakeModel、UnstableBakeModel。
- 验证：12 个目标文件均可按 UTF-8 解码且无 U+FFFD。`./gradlew.bat compileJava` 仍因既有迁移 API 缺失错误失败，但未出现编码/unmappable/MalformedInput/UTF-8 相关错误。
