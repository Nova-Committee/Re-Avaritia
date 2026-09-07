---
name: avaritia-portable-ui
description: 在本项目 NeoForge 1.21.1 中开发、推广和排查 PortableUi 界面。用于 PortableLayout、PortableSelectionList、OperationMenu、原生 GUI layers、UiInspector、容器坐标/裁剪/命中、输入状态保持、JEI/EMI 避让以及截图验收；也用于排查重复绘制、弹窗关闭菜单、滚动重建输入框或背景切片缺失。
---

# Avaritia PortableUi

## 先确认目标和所有权

- 以当前 `build.gradle`、`gradle.properties` 和目标依赖源码为准。本轮验证基线是 Minecraft 1.21.1、NeoForge 21.1.215、Java 21、ModDevGradle 2.0.116；不要套用其他分支的 GUI 生命周期。
- 下文 `J` 代表 `src/main/java/committee/nova/mods/avaritia/`。优先阅读目标 Screen 及 `J/api/client/screen/component/` 中的现有组件。
- 坚持原版灰色面板、凹槽、原生按钮和专用机器贴图。不要增加 UI DSL、通用大父类、远程控制服务或无消费者的抽象。
- 保留服务端 Menu、Slot 索引、网络 action ID、数据权限检查与 recipe transfer。确有链路错误时修根因，不能让 UI 本地成功假装服务端已保存。

## 共用几何，不共用业务状态

使用 `PortableLayout` 的四个静态方法，矩形类型只用原版 `ScreenRectangle`：

```java
ScreenRectangle centered(int screenWidth, int screenHeight, int preferredWidth, int preferredHeight, int margin);
ScreenRectangle inset(ScreenRectangle bounds, int left, int top, int right, int bottom);
ScreenRectangle translate(ScreenRectangle bounds, int dx, int dy);
boolean contains(ScreenRectangle bounds, double mouseX, double mouseY);
```

- 在 init、resize 或内容变化时缓存 panel/list/scrollbar/preview 等有意义的矩形；绘制、裁剪、命中引用它们。不要每帧重建布局树。
- 使用 `PortableUi.panel/inset(graphics, bounds)`；逐行绘制保留整数原语，避免每帧为形式统一创建矩形。
- `contains` 保留小数鼠标坐标，使用左闭右开、上闭下开边界；`-0.25` 不能先转 int 变成 0。零面积不绘制、不接收输入。
- `ScreenRectangle.intersection()` 无交集返回 null；需要裁剪时转成空矩形，不得解释成“不裁剪”。`inset` 可用负边距生成外框，但尺寸夹到零。
- 固定表单行用原版 `GridLayout`。容器原点取 `getGuiLeft/getGuiTop`；widget 最终位置取 `getRectangle()`；Slot 取真实 `slot.x/y`。`renderLabels` 已在容器局部坐标，不能再次叠加原点。
- 不强制看似相邻的区域等宽：桶 search=244、数据 list=232，search 与 creatures tab 右边缘对齐。
- 纳须弥：previewOuter 比 library 上下各伸出 2；previewContent 比 library 上下各缩进 4。renderer 使用 content，拖拽区域使用 outer；不要混为一个矩形。

`PortableSelectionList<E extends ObjectSelectionList.Entry<E>>` 只共用外观和输入边界：

```text
(Minecraft, width, height, y, rowHeight, rowInset, scrollbarInset, drawInset)
PlayerList: 16, 4, 6, false
SpaceList: 28, 10, 8, true
```

保留原版滚动、选中和键盘逻辑；过滤、数据刷新、网络请求仍由 Screen 管理。轮滚只在列表内处理。

## 绘制只执行一次

- 本版本 `Screen.render()` 已调用 `renderBackground()`；`AbstractContainerScreen.render()` 也已自行调用背景，再绘制 widgets/slots。
- 不在 `super.render()` 前再手动调用 `renderBackground()`。真实客户端曾因此每帧两次登记 `bucket.panel`；修复点是 `BaseContainerScreen.render()`，不是 Inspector 去重。
- 已 `addRenderableWidget` 的控件不要在 `renderBgs` 再画一次。时钟输入框是此类实例。
- 铁砧名称框使用 `addWidget + renderFg`，保持这一路单次手工绘制，不能又加入 renderables。
- 纹理按钮始终用自己的坐标和尺寸；不要捕获父方法的 x/y 代替按钮位置。侧配置 clear/back 曾因此图片与点击区分离。
- 背景切片同时核对原图和目标高度。`TesseractScreenLayout` 当前最后一片为 `(destinationY=239, sourceY=190, height=26)`，总高度 265；原图第 207–215 行包含快捷栏底线、灰色留白和底边，截成 17 会丢掉它们。不要通过移动 Slot 弥补缺失背景。

## 用原生 GUI layers 保住容器

- `PortableUi.confirm/prompt` 内部使用 `Minecraft.pushGuiLayer`，仅当当前屏幕仍为传入 parent 才打开；不要改回 `setScreen(new Dialog(...))`。
- `Dialog.finish()` 只在自身仍是顶层且尚未结束时 pop，并返回成功。确认/提交仅在成功 finish 后回调；取消、Esc、removed 不执行业务回调。
- 不手动 tick/init/removed 父 Screen。push 不 removed 父容器，pop 恢复原父对象；真正 setScreen 会清理 GUI layers。
- 危险确认默认聚焦取消。Enter 只激活当前焦点，不能无条件确认；保留原生 Tab 导航。
- 保留 `PortableUi.root()` 回溯，服务端刷新包可能在 Dialog 打开期间更新父界面。
- 桶删除捕获 `DeleteTarget`，确认时仍执行 `ensureCache/canOperate/stillPresent`；客户端同步会替换 ItemStack，客户端检查当前类型/count，服务端保留严格原对象/槽位所有权。
- `OperationMenu` 关闭点击必须吞掉，不能穿透到下面的槽位/按钮；名称建议仍保留前缀匹配、最多 8 条、候选优先 Enter、Tab 关闭建议。
- `StringInputScreen` 的 JSON/NBT/数量 Function 回调、regex、无限长输入能力、shouldClose、空输入取消与错误留屏语义不能替换为简单的 64/128 字符 prompt。

## 保留编辑和选择状态

- resize 或返回子对话框前从实际 EditBox 读取未提交文本，不能重新套用 defaultValue；只在真正新查询/切换物品来源时重置滚动。
- 配置页一次创建 ConfigEntry 控件，`control()` 返回实际 widget（分类标题返回 null）。滚动只改位置/visible；resize 用 `repositionElements` 重排，不能在 render/scroll 中 init 重建。
- 只有完整处于 viewport 的配置控件可见；离开视口前解除焦点，沿用 RangedEditBox 验证。描述换行在 init/resize 缓存，限制到行高 40 内，截断内容悬停提供全文；页脚只画一次。
- 过滤器和选择器使用原生 `PortableItemGrid`：共享 PortableUi 的 inset/slot/row，按下与释放同一有效格子才选择；滚动或数据映射变化必须清除旧按下状态，空格子和格子间隙不接收选择。
- 操作栏使用 `PortableUi.button` 和原版 `Component`；`StringInputScreen` 构造器也使用 Component。OperationButton、OperationButtonType、Text、Coordinate、TextureCoordinate、GuiUtils 已移除，不得重新引入旧框架或兼容包装。
- 选择器默认所有物品来源是 `CreativeModeTabs.SEARCH`，不是空的 INVENTORY 创造标签页；保留显式标签页构造器。不要每帧复制整份创造物品列表。

## 使用 Inspector 取得证据

只有 `!FMLLoader.isProduction() && Boolean.getBoolean("avaritia.ui.inspect")` 时启用。

```bat
gradlew.bat runClient -PuiInspect=true --console=plain
gradlew.bat runClient -PuiInspect=true -Precipe_viewer=emi --console=plain
```

- 检查游戏目录：`build/ui-preview`；导出：`build/ui-inspection`。普通 `runClient` 使用原运行目录，不要误进入旧存档。
- Ctrl+Alt+F8 切换标注；Ctrl+Alt+F9 主动导出同帧 plain PNG、annotated PNG、JSON。使用受监督进程启动，等待 `Created:.*minecraft:textures/atlas/gui\.png-atlas`，不能把创建进程当作 ready。
- 默认由用户操作视觉验收；只有明确授权后才控制客户端。不要创建自动 GUI mock、生产调试命令，或自行操作/删除用户世界。
- 静态 widget 用 `UiInspector.name(widget,id)`，返回同一对象。实际绘制处登记 region；popup 用 popup 入口；动态行使用 row。
- 空间 businessKey 使用 space.id；玩家用规范化名字；频道用 type+id。Inspector 只映射为会话内匿名编号，禁止输出键本身。没有真实唯一键的物品/桶行用 null 和当前 sourceIndex，保持 frame-scoped 非稳定 ID。
- 匿名 widget 路径不是跨重建稳定 ID。名称只保证在各自 screenInstance 内唯一；同时检查 frameId/layoutRevision，不能拿旧快照点击新布局。
- 顶层 Post 才完成整帧；后台原生层全部 inputBlocked。OperationMenu 打开时同层其他节点也阻挡，不按局部覆盖面积猜测输入穿透。
- 自动槽位节点使用实际 containerId 和 Slot 坐标；原版图标 16×16、命中框外扩 1 成 18×18。遇到自定义命中覆盖，必须核对，不能把 Inspector 当作真正的事件分发器。
- `duplicate_id`、`invalid_bounds` 会使导出不适合作自动定位证据。修绘制或几何根因，不能过滤 issue、改名掩盖重复绘制。
- PNG 可以含可见私密内容，只在本机保存；JSON 不包含输入值、名字、物品/NBT 内容或服务器地址。关闭时不采集控件树、不抓图、不写目录。
- GUI 绘制坐标乘 guiScale；鼠标用 `windowPosition * guiSize / windowSize`，逆变换用反比。GUI 尺寸向上取整，两者在奇数像素下不完全相同。
- scissor 使用原版分别对位置/宽高做整型截断的规则和 OpenGL 左下角原点，不对四条边各自四舍五入。
- 截图尺寸不等于 framebuffer 时 pixelMappingValid=false，只报告 GUI 几何。桌面点击还需同窗口最新截图校准客户区和 DPI。
- 导出 PNG 先于 JSON，CREATE_NEW，不覆盖；失败保留部分文件并报错，不自动重试。NativeImage 必须关闭。
- `Const.GSON.toJson(JsonObject)` 默认也会省略 JsonNull。保留 schema 的 nullable 字段时使用当前 `serialize` 实现：`Const.GSON.newJsonWriter`、`setSerializeNulls(true)`、JsonObject adapter.write；不要修改全局 Gson 配置。

## 按故障层诊断，避免反复误判

1. 先问清“槽位/内容/快捷栏/HUD/背景留白”哪一层缺失；用裁剪放大的原始图确认具体位置，不能把“快捷栏下面板”误解成整个背包。
2. 怀疑标注遮挡时读取原版 F2/系统截图。F9 plain 主动移除 Inspector，可能把用户看到的遮挡也移除；它不是原始可见画面的替代品。
3. JSON 判断几何/层/可见性，PNG 判断画面；二者都不能独自证明服务端成功。用户报告完成行为可记录为人工验收，不能伪称缺失的截图或时间查询已取得。
4. 若提交失败，检查实际包方向、TYPE、注册和处理器保存。曾有 C2SItemFilterPack 错用 `s2c_totem` 且未注册，造成 serverbound EncoderException；修复后还需处理无 CUSTOM_DATA、复制并写回 TOOL_FILTERS、客户端重新读取。
5. 编解码回归要经过原版 `ServerboundCustomPayloadPacket.STREAM_CODEC` 和真实 ServerPayloadContext，单测私有 payload codec 抓不到方向/注册错误。参考 `src/gameTest/java/committee/nova/mods/avaritia/gametest/ItemFilterGameTests.java`。

## 验证和交付

- UI 变更后重启对应版本客户端才算验收；旧进程不能证明新代码。
- 分批检查实际输入模型：弹窗前/中/后的 containerId 与 carried 物品；取消不发业务操作；确认一次；滚动/resize 后的查询、选中、文本；存储/合成切换后的 visible 集合。
- 时钟输入框显示 12000 不等于服务器时间是 12000。关闭 doDaylightCycle 后分别设置 6000/12000，再执行 `/time query daytime` 或取得明确人工结果。
- 容器固定贴图需足够逻辑画布；不要为了截图重写槽位或新增全局缩放政策。无尽箱已有局部 fit/restore，保留退出恢复。
- JEI/EMI 避让继续用 `J/client/screen/element/GuiElementAccess` 与现有 handlers，不使用 debug Inspector 数据。保留 `AvaritiaJeiPlugin.registerGuiHandlers` 原箭头坐标。
- 关闭 Inspector 验收前记录导出文件名与校验值，普通启动后按 F8/F9，比对无新增/覆盖。若日志显示进入了世界，就不能写“只在标题页检查”。
- 最终运行 `gradlew.bat build runGameTestServer --console=plain`，读取真实测试统计并确认服务端退出。只保留能防守真实行为的回归；不要添加钉常量、源码字符串、字段转发的测试。
- 核对生产 JAR 不含 GameTest 夹具、检查世界、PNG/JSON 或临时脚本；保留用户的检查世界与导出，不擅自清理。报告构建、人工视觉、来源推断与未取得的证据，区分各自边界。
