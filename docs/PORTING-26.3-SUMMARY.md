# Re-Avaritia Fabric 26.3 移植总结文档

> 本文档总结 Re-Avaritia（无尽贪婪）模组从 Fabric 1.20.1 移植到 Minecraft 26.3 Fabric 的
> 目标约束、API 变化结论、工程约定与修复成果，供后续维护与上游协作参考。
> 更新时间：2026-10-18。

---

## 一、项目简介

### 1.1 项目概况

| 项目 | 说明 |
|------|------|
| 模组 | Re-Avaritia（无尽贪婪），Minecraft 内容模组 |
| 原版本 | Fabric 1.20.1（官方 `fabric/1.20.1` 分支） |
| 目标版本 | Minecraft 26.3，Fabric 加载器 |
| 映射 | 26.3 不再混淆，使用 Mojang 官方命名，**不再依赖 Yarn 映射** |
| 构建 | Loom 1.18 纯配置模式（无 `modImplementation` 方法），26.3 无混淆无需重映射 |
| 源码参考 | `.gradle/loom-cache/minecraftMaven` 下的 26.3 反编译源；项目根目录的官方 Neo 26.3 工程（`Re-Avaritia-neo-26.3`）与 template-mod 模板 |

### 1.2 依赖策略

- **必需依赖**：Fabric API（`fabric.mod.json` 声明为必需，避免干净环境缺类崩溃）。
- **JEI**：默认支持（需在 `ModRegistries.initialize` 遍历 `ModRecipeSerializers.getEntries()`
  注册到 `RecipeSynchronization.synchronizeRecipeSerializer`，否则 JEI 模组分类全空）。
- **Trinkets Updated**（Patbox 重写分支，包名 `eu.pb4.trinkets`）、**Jade**：启用编译。
  Jade 插件通过 `fabric.mod.json` 的 `"jade"` 入口点注册。
- **EMI / KubeJS / CraftTweaker**：代码保留但排除编译。
- **REI**：原版无兼容基础，不实现。
- 跨模组兼容配方（ae2/botania/mek 等 19 个）与 13 个跨模组奇点 JSON 已删除；
  Java 侧跨模组奇点由 `ResourceConditions.tagNotEmpty` 门控。

### 1.3 分支与 PR 工作流

```
本地 main ──push──> origin (XiaoQiAWA/re-avaritia-fab26.3)   # 备份
   │
   └─checkout fabric/26.3 ──cherry-pick──> fork (XiaoQiAWA/Re-Avaritia)
                                             └──> PR #387 → Nova-Committee/Re-Avaritia (base fabric/1.20.1)
```

- 两分支文件树一致，cherry-pick 干净应用；每次代码变更后同步两端。
- 另向官方 **neo/26.3** 分支提交了 PR #389（修复渲染崩溃，见成果清单 #12）。
- GitHub API token 可从 GCM 提取：`echo "protocol=https`nhost=github.com`n`n" | git credential fill`。

---

## 二、26.3 API 主要变化

### 2.1 数据包与资源包

| 变化 | 详情 |
|------|------|
| 目录单数化 | `advancements→advancement`、`loot_tables→loot_table`、`recipes→recipe`、`tags/items→tags/item`、`tags/blocks→tags/block`、`tags/entity_types→tags/entity_type`。**复数目录被静默跳过，日志无报错** |
| 配方硬错误 | 配方是动态注册表数据：`data/<ns>/recipe/*.json` 解析失败/缺物品/缺标签均为硬错误并阻断进世界（旧版静默跳过） |
| JSON 字段变化 | advancement 的 `icon.item→icon.id`；`recipe_unlocked` 条件 `recipe→recipes`；loot_table 的 `conditions:[{"condition":"x"}]→condition:{"type":"x"}`；`ItemStackTemplate.CODEC` 接受裸字符串 `result` |
| Ingredient 格式 | `Ingredient.CODEC` 不再接受 `{"item":"x"}`/`{"tag":"x"}` 对象格式，仅支持字符串 `"x"`/`"#x"` 或数组。模组自定义序列化器用 `RecipeCodecs.LEGACY_INGREDIENT/LEGACY_ITEM_STACK_TEMPLATE` 兼容旧格式 |
| 内置包 pack.mcmeta | 必须用 `min_format`/`max_format`（版本 `[97,1]`）；`pack_format+supported_formats` 旧写法抛 `JsonParseException` |
| 标签差异 | 无 `minecraft:music_discs` 标签（仅有 `creeper_drop_music_discs`）；`forge:eggs→minecraft:eggs`；`forge:end_stones/ender_pearls/nether_stars` 无等价标签须改具体物品 |

### 2.2 物品与模型

| 变化 | 详情 |
|------|------|
| ClientItem 必须 | 物品模型必须有 `assets/<ns>/items/<id>.json`（`type=minecraft:special` / 自定义类型等）；仅有 `models/item/*.json` 会导致**全部物品紫黑且日志不报错** |
| OBJ 加载器废弃 | 旧自定义加载器 `"avaritia:obj"` 不再被 vanilla 解析，使用它的模型被判定为空模型（物品消失、图标丢失）。须改写为 vanilla 标准格式 + `minecraft:special` 类型绑定自定义特殊渲染器 |
| 图集注册 | 自定义图集（如 halo）必须经 `RegisterTextureAtlasesEvent`（Neo）/ Mixin `AtlasManager.<init>` TAIL（Fabric，AW 拓宽 `AtlasEntry`）注册 `AtlasConfig`；仅放 `atlases/x.json` 不会自动加载，运行时抛 `Invalid atlas texture id` |
| 装备贴图路径 | 移至 `textures/entity/equipment/<layer>/<id>.png`（`humanoid`=旧 layer1、`humanoid_leggings`=layer_2、`horse_body`/`nautilus_body`/`wolf_body`/`happy_ghast_body` 同名）；humanoid 层必须 **64x32**（64x64 皮肤布局会按 0..1 UV 采样错乱） |
| Tint 返回值 | `ItemTintSource.calculate` 返回值直接写入顶点色，必须不透明 ARGB（vanilla 一律 `ARGB.opaque` 包装）；返回 `0x00RRGGBB` 会导致着色层透明/不上色 |
| 弩动画泛化 | 第一/第三人称拉弦动画判定不再硬编码原版弩，需 `instanceof CrossbowItem` 泛化匹配子类（用 @Redirect 覆写 `ItemStack.is`） |
| EquipmentSlot | 新增 `SADDLE`/`BODY` 等槽位，`isArmor()` 语义不可靠——判定类逻辑（如无尽甲全套判定）必须用显式 HEAD/CHEST/LEGS/FEET 四槽数组 |

### 2.3 渲染管线（重点）

| 变化 | 详情 |
|------|------|
| reversed-Z 深度 | `DepthStencilState` 默认 `GREATER_THAN_OR_EQUAL`（近平面深度值大、远平面 0.0）。自定义效果管线必须用 GEQUAL；第一人称手持 pass 深度清 0.0，沿用 LEQUAL 会整层被丢弃。需"穿透方块"的效果（黑洞）须单独注册 `DepthStencilState(CompareOp.ALWAYS_PASS, false)` |
| 自定义 RenderPipeline 必绑 UBO | Fabric 侧必须移植 Neo 的三个 Mixin：`RenderTypeMixin`（prepare RETURN→`RenderTypeDrawTracker.register`）、`PreparedRenderTypeMixin`（drawFromBuffer/drawFromBufferOit HEAD/TAIL 切 active RenderType）、`RenderSystemMixin`（getCompiledPipeline RETURN 记管线、bindDefaultUniforms TAIL 调 `bindIfAvaritia`）；帧末 `endFrame()+tracker.clear()` 挂 `Minecraft.renderFrame` TAIL。缺失则 PiP/世界绘制自定义管线时 `GpuBufferSlice.buffer()` NPE |
| GLSL 330 | 自定义着色器全部改用 GLSL 330 标准：所有 in/out 显式 `layout(location)`，禁止松散全局 uniform，自定义参数封装为 std140 标准块；改动必须与 Neo 参照工程逐字节对齐 |
| PiP 渲染器 | `PictureInPictureRendererRegistry.register`（Fabric）/ `RegisterPictureInPictureRenderersEvent`（Neo）必须在客户端初始化调用，否则提交的 PiP 状态无人渲染。PiP `prepare()` 自带 `scale(s,s,-s)` 与 blit V 翻转（净效果为一次 Y 翻转）；姿态校准应使用 `rotateZ(180°)`（vanilla `InventoryScreen` 约定，行列式 +1 保持 winding），**不要用 `scale(1,-1,1)`**（行列式 -1 会反转 winding 导致正面被剔除） |
| RenderTypes.lines | 顶点格式为 `POSITION_COLOR_NORMAL_LINE_WIDTH`（4 元素），`addVertex` 链必须写全 setPosition+setColor+setNormal+setLineWidth，否则 `BufferBuilder.endLastVertex` 抛 `IllegalStateException("Missing elements in vertex")`；线宽取 `gameRenderState().windowRenderState.appropriateLineWidth` |
| 实体剔除包围盒 | 自定义实体渲染的剔除包围盒需与实际渲染尺寸匹配，否则特效在视口边界被截断 |
| 手持三叉戟 | `tridentGeometry` 走 OBJ 实体层，不应叠加 effect 星空遮罩层（effectQuads 是 0..1 物品平面坐标，与 block 单位 OBJ 坐标空间不一致） |
| 实体渲染状态化 | 26.3 全面使用 RenderState。注意 `LivingEntityRenderer.extractRenderState` 中 `state.yRot = Mth.wrapDegrees(headRot - bodyRot)` 是**头相对身体的旋转（会 wrap 到 [-180,180]）**，非绝对朝向；需要绝对旋转时应通过 render data（Fabric `RenderStateDataKey` / Neo `ContextKey`+`AvatarRenderStateModifier`）传入 `entity.getViewYRot(partialTicks)/getViewXRot(partialTicks)` |

### 2.4 注册时序与运行时

| 变化 | 详情 |
|------|------|
| 资源加载时序 | `ReloadableServerResources.loadResources` 完成时组件和标签均未绑定；依赖组件/标签的逻辑必须在 `updateComponentsAndStaticRegistryTags` 尾部执行 |
| 注册顺序 | Block/Item 的 "id not set" 崩溃只暴露第一个，修复时必须全量审计所有构造链 |
| 客户端配方同步 | `ClientRecipeContainer` 不含完整配方表；`getSynchronizedRecipes()` 仅含显式注册的序列化器。客户端 Level 无 `getRecipeManager()`，用 `level.recipeAccess().getSynchronizedRecipes()` 的 `getFirstMatch/getAllOfType` |
| 动态配方 | `fabric-recipe-api` 的 `synchronizedRecipes` 仅在 `RecipeManager` 构造器 TAIL 快照一次；之后反射替换 recipes 字段必须同步重建 `synchronizedRecipes`（`new SynchronizedRecipesImpl(RecipeMap)`），否则动态配方查不到 |
| 容器菜单 | 客户端初始化需显式调 `refreshContainer` 重建视图；状态变更字段注册为 `ContainerData` 并 `broadcastChanges()` 即时同步；方块实体破坏时覆写 `preRemoveSideEffects` 避免虚拟存储物品散落丢失 |
| 右键拦截 | 客户端必须注入 `MultiPlayerGameMode.performUseItemOn` 的 HEAD（外层 `useItemOn` 用 `startPrediction` 包裹，取消外层会吞发包）；服务端注入 `ServerPlayerGameMode.useItemOn` HEAD 即可 |
| 物品附魔 | 自带附魔生效需通过 `EnchantmentHelperMixin` 补全遍历逻辑，不能直接读组件绕过标准遍历路径 |

---

## 三、工程约定（项目记忆）

1. **Mixin 注入点以 javap 字节码为准**：`mc263-sources` 反编译源与运行时 jar 有出入。
   `@Redirect/@ModifyArg` 处理器参数 = 被调用参数（可省略尾部外层方法参数）；`@Inject` = 目标方法全部参数 + CallbackInfo。
2. **@At INVOKE target 格式**：禁止 javap 风格 `->` 箭头（`Lowner;->name(desc)ret`），必须用标准格式
   `Lowner;name(desc)ret`，否则 `MemberInfo` 解析退化匹配到错误指令。
3. **Mixin 提前返回分支**需显式执行对应逻辑，避免后续注入点被绕过。
4. **存储链路物品身份**：必须完整保留所有组件（附魔、耐久、自定义名称、潜影盒内容等）；
   暂不支持的非默认组件物品应拒绝进入存储链路。删除频道的服务端逻辑必须根据数据包内目标身份解析并校验权限。
5. **旧配方转换**需保留奇点类型的 NBT 匹配条件，防止错误种类的奇点被替代。
6. **着色器与官方 Neo 26.3 逐字节对齐**，不强制额外渲染逻辑。
7. **不要依赖 `isArmor()`** 判定装备槽（见 2.2）。
8. **反射调用**（如 Trinkets 集成）需用正确的访问方案，禁止静默吞掉初始化失败。
9. **构建产物去重**：`main/resources` 与 `generated/resources` 存在同名同内容物品模型会触发 duplicate entry 构建错误。
10. **Loom 1.18 纯配置模式**：无 `modImplementation`，跨模组依赖用 `compileOnly` 引入。
11. Neo 侧 `state.getRenderData(ContextKey)` 对应 Fabric 侧 `state.getData(RenderStateDataKey)`；
    Fabric 无 Neo 的 `RegisterRenderStateModifiersEvent`，用 Mixin 注入 `AvatarRenderer.extractRenderState` TAIL 代替。

---

## 四、修复成果清单

| # | 问题 | 根因 | 修复方式 | 提交 |
|---|------|------|----------|------|
| 1 | 无尽盾非普通模式模型消失（含物品栏图标） | 6 个模式模型 JSON 使用 26.3 不再支持的 `avaritia:obj` 加载器被判定空模型 | 新增 `ShieldObjRender`（`NoDataSpecialModelRenderer`）特殊渲染器；模型改写为 vanilla `item/generated`，主模型声明 `minecraft:special` 绑定 `avaritia:shield_obj` | 6401bad / 7ef915d |
| 2 | 中子戒指预览空白 | `NeutronSpacePreviewRenderer` 的 PiP 注册方法从未被调用 | `AvaritiaModClient.onInitializeClient` 显式调用 `registerPip()` | 6401bad / 7ef915d |
| 3 | 全套无尽甲判定失效（无敌失效） | `EquipmentSlot.values()+isArmor()` 过滤在 26.3 永远 false | `ToolUtils.isInfinite` 改用显式 HEAD/CHEST/LEGS/FEET 四槽数组 | 已合入 |
| 4 | 压缩机动态配方查不到 | 反射替换 recipes 字段后未重建 `synchronizedRecipes` | 同步重建 `new SynchronizedRecipesImpl(RecipeMap)` | 已合入 |
| 5 | 纳须弥之戒右键选中方块失效（Neo 侧崩溃 #388） | Fabric 注入点错在外层 `useItemOn`（吞发包）；Neo 发布版 1.4.2 `RenderTypes.lines` 漏写 LineWidth | Fabric 改注入 `performUseItemOn` HEAD；Neo 侧 PR #389（f6a4b77，含 Closes #388） | ecdab08 / 272cbd2 等 |
| 6 | 戒指预览上下颠倒 | PiP 纹理 Y 轴与屏幕空间相反（净效果一次 Y 翻转） | 渲染姿态加 `rotateZ(180°)` 校正 | 727a9b5 / 5d113e0 |
| 7 | 无尽套星空渲染"边界"（视角转动突然停住） | 盔甲用 `state.yRot`（头-体相对旋转，wrap 到 ±180 饱和），物品用绝对旋转所以无边界 | 经 `RenderStateDataKey` 传 `getViewYRot/getViewXRot` 绝对旋转，模型改读绝对值 | 2e4dc38 / e2bb171 |
| 8 | 穿无尽套飞行不渲染翅膀 | Fabric 只判 `state.isFallFlying`（鞘翅），缺 Neo 的创造飞行判定 | `extractRenderState` TAIL 注入捕获 `player.getAbilities().flying` → `INFINITY_ARMOR_FLYING` render data；模型条件对齐 Neo | 2e4dc38 / e2bb171 |
| 9 | 预览渲染成反面 3 面（正面被剔除） | 上轮修复用的 `scale(1,-1,1)` 行列式 -1，叠加 PiP 自带 `scale(s,s,-s)` 后 winding 反转 | 改用 `rotateZ(180°)`（vanilla 实体 PiP 约定，保持 winding） | 727a9b5 / 5d113e0 |
| 10 | 预览鼠标拖动方向反 | Z 翻转后 yaw/pitch 视觉方向与屏幕拖动相反 | `orbitPreview` 增量取反补偿 | 727a9b5 / 5d113e0 |
| 11 | 全部奇点合成催化剂不消耗（消耗后又回来） | Fabric 版多出 Neo 没有的 `getRemainingItems` 覆写，把每个奇点槽位返还副本 | 删除该覆写，奇点按默认逻辑正常消耗（永恒奇点/非默认组不受影响） | 727a9b5 / 5d113e0 |
| 12 | 开启 Iris 光影后手部/掉落物/展示框渲染异常（物品被黑色遮罩，严重时 `hs_err` 落在 OpenGLOn12.dll），日志报 `Missing program avaritia:cosmic in override list` | 自定义 RenderPipeline 未通过 Iris API 登记；Iris 的 shadow pass 不走主映射，继续提交 cosmic/black_hole 管线会回退到错误程序 | 光影下统一关闭所有自定义星空效果层（物品所有 display context、盔甲 cosmic 层、黑洞回退 `VOID` 贴图）；同时 `IrisCompat.assignPipelines()` 仍把管线登记到 `ENTITIES_TRANSLUCENT` 作为兜底 | 本轮 |
| 13 | 启动日志 `Missing block model: minecraft:builtin/entity` | 26.3 已移除 `builtin/entity`，无尽箱物品模型仍以它为 parent | 改为 `minecraft:item/generated` + `layer0: avaritia:item/infinity_chest`；`minecraft:special` 的 `base` 只用于 display/particle，不影响特殊渲染 | 本轮 |
| 14 | 压缩机输出槽可能出现 >99 / 超过最大堆叠的非法 ItemStack（上游 #386 同类问题） | 输出容量检查只比较 `result.getCount()`，未乘 `outputAmplifier`；`materialStack` 直接复制外部存储的 128 堆叠 | 产出按 `outputAmplifier` 计算并夹到 `getMaxStackSize()`；`materialStack` 复制时夹到合法堆叠数 | 本轮 |

> 提交格式说明：`main 提交 / fabric/26.3 提交`；fabric/26.3 均由 main cherry-pick 而来并已推送 fork 更新 PR #387。

---

## 五、剩余注意事项

- **PR 状态**：PR #387（Fabric）、PR #389（Neo）均处于等待官方审阅状态；后续更新流程保持
  "main 提交 → cherry-pick → push fork"。
- **实测建议**：预览默认视角（应看到顶面+正面）、拖动旋转方向、全部奇点合成催化剂的消耗、
  无尽套星空连续转动、创造飞行翅膀渲染；开启 Iris 光影后手持/掉落/展示框中的星空物品与无尽套
  都不应出现黑色遮罩或崩溃（自定义星空层会被跳过，黑洞回退普通贴图），
  高速运行压缩机时输出槽不应出现超过最大堆叠的堆叠。
- **调试工具**：sources jar 解压到 `%TEMP%\mc-src` 用于查阅 vanilla 源码；
  构建命令 `.\gradlew.bat build -x test`。
