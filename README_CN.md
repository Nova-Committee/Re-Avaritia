<p align="center">
    <img width="690" src="web/logo.png" alt="title">  
</p>
<hr>
<p align="center">
    <img src="https://img.shields.io/badge/Minecraft-1.20.1%20Fabric-c70039" alt="支持版本">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia" title="原上游项目，不是本 Fabric 移植版的下载列表">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="原上游 CurseForge 下载统计">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> | 
    <a href="README_CN.md">简体中文</a>
</p>





## **📕介绍:**
* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)重铸版 **Re-Avaritia 的 Minecraft 1.20.1 Fabric 移植版**，提供无尽贪婪的物品、方块和配方。
* 此模组是非官方版本!

以本地 `Avaritia-1.20` 的 **1.4.2** 为迁移源，保留 `committee.nova.mods.avaritia` Java 包层级；本项目不是 Forge 模组。

### 运行要求与可选联动

- 运行要求：Minecraft **1.20.1**、Java **17+**、Fabric Loader **0.17.2+**。
- **无前置模组**：不要求或内嵌 Fabric API、Porting Lib、Cardinal Components、Trinkets。
- 注册表、资源包、菜单、网络、存储和生命周期均采用项目内的原版接口与 Mixin 实现。服务端配置为 `config/avaritia-common.json`。
- Trinkets、EMI、JEI、Jade、KubeJS、CraftTweaker 为可选联动；安装这些模组时才需满足它们自身的依赖。
- 可选 Trinkets 槽位：两手 `ring` 槽支持戒指，`chest/back` 支持无尽鞘翅，`chest/back` 或 `charm/charm` 支持水晶矩阵锹，`charm/charm` 支持无尽图腾。
- 参考上游 [fabric/1.20.1 固定提交](https://github.com/Nova-Committee/Re-Avaritia/tree/4ec454e0847cefcd08434722ec7c6704e7564fd5)，不继承其前置依赖。

### 安装

为 Minecraft 1.20.1 安装 Java 17+ 和 Fabric Loader 0.17.2+。按下文构建本项目后，将 **`build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`** 放入游戏实例的 `mods/` 目录；多人游戏时客户端和服务端都要安装。应使用重映射后的发布 JAR，而不是 `-sources` 或开发 JAR；Forge/NeoForge 产物不能替代该文件。

默认安装**仅要求 Fabric Loader**，不需要 Fabric API 或任何联动模组。如需 Trinkets、EMI、JEI、Jade、KubeJS 或 CraftTweaker，请另行安装其 **Fabric 1.20.1** 版本及各自所需依赖。下文可选开发配置还提供 Mod Menu。本页的上游链接与下载徽章仅用于历史参考，不代表本地 Fabric 移植版已在这些页面发布。

### 版本配置

请修改本项目的 **`gradle.properties`**，不要在 `build.gradle` 中硬编码版本。核心属性如下：

```properties
gradle_version=8.12
mc_version=1.20.1
loader_version=0.17.2
loom_version=1.10.5
java_version=17
mod_version=1.4.2
mod_version_tag=fabric
mod_platform=fabric
mod_group_id=committee.nova.mods
mod_name=Re-Avaritia
mod_id=avaritia
```

上述值组成的项目/元数据版本为 **`1.20.1-1.4.2-fabric`**（`mc_version-mod_version-mod_version_tag`），归档基础名称为 **`Re-Avaritia-fabric`**（`mod_name-mod_platform`），发布文件名保持 **`Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`**。`mod_group_id` 设置 Gradle 分组，不会重命名 Java 包，也不表示存在可用的 Maven 发布产物。构建插件与依赖版本（包括可选联动的固定版本）也统一在本项目属性文件中维护；修改版本不会将可选模组变成默认运行时前置。

已提交的 Gradle Wrapper 从 `gradle/wrapper/gradle-wrapper.properties` 启动。修改 `gradle_version` 后执行 `gradlew.bat wrapper`，重新生成其下载地址；再用新 Gradle 执行一次以更新 Wrapper 脚本/JAR。仅修改项目属性不会自动升级 Wrapper。

### 构建与开发环境

Windows 构建与运行：

```bat
gradlew.bat build
gradlew.bat runClient
gradlew.bat runServer --args=nogui
```

开发客户端同时启动 JEI、Mod Menu、CraftTweaker、KubeJS：

```bat
gradlew.bat -PwithClientIntegrations runClient
```

该可选配置固定 JEI **15.20.0.112**、Mod Menu **7.2.2**、CraftTweaker **14.0.12**、KubeJS **2001.6.5-build.20**，并加入它们自身所需的开发运行时；不内嵌或发布为前置模组。编译时提供 Fabric API 以保证 Loom 正确映射可选 API 的继承关系，默认运行时仍不加载它。编译阶段关闭依赖接口注入，安装联动模组后由其真实运行时 Mixin 提供这些接口。

重映射后的发布 JAR 为 `build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`。原生回归夹具不打入该 JAR，需单独运行：

```bat
gradlew.bat -PgameTests runGameTestServer --args=nogui
```

测试服务端控制台执行 `execute positioned 0 80 0 run test runall`，等原生测试全部报告后执行 `stop`。开发环境安装 Trinkets 时，在启动命令加 `-PwithTrinkets`；仅该可选配置加入 Trinkets 正式发布产物及其自身所需的 Fabric API / Cardinal Components 运行时，默认配置不变。

Windows 原生客户端测试时，先确认游戏窗口焦点；如果聊天快捷键或英文命令无响应，检查中文输入法模式，可尝试 `Ctrl+Space` 或临时英文键盘布局，再用 `/` 打开命令栏并输入 `/time set day`。窗口重新激活后须再次检查布局；测试结束恢复原布局，不必永久更改系统语言。必须观察命令反馈，而不是仅以自动化工具报告按键已发送判断成功。后台自动化若丢失组合键修饰符（如 `Shift+2` 输入 `2` 而非 `@`），应在已确认的游戏窗口前台重试，不应因此修改模组输入处理。

本次构建属性与 README 对齐前的历史验证记录：完整构建、53 项 JUnit、默认环境 50 项原生 GameTest，以及独立服务端启动/重载/保存。此前安装 Trinkets 环境通过 45 项原生 GameTest，另覆盖无尽鞘翅、水晶矩阵锹和无尽图腾。真实 Fabric 客户端覆盖个人维度存档重入与天气隔离、无尽箱搜索及 GUI 缩放恢复、Trinkets 饰品界面佩戴戒指、EMI 中绿宝石/萤石的不同压缩配方，以及独立 JEI 的中子素压缩分类及 16 页配方。光晕保留烘焙顶点 RGB/Alpha，另经真实顶点缓冲区回归及背包/快捷栏实测黑色光晕和中子物品半透明效果。四模组开发客户端也已启动并完成入世及 CraftTweaker/KubeJS 脚本加载。超立方体标题、普通/合成完整面板、无尽盔甲、四种盾牌模式及炽阳弓普通射击通过用户手动验收；Jade 未进行原生客户端验证。这些记录不代表本次构建配置改动已重新运行验证。

历史脚本引擎验证记录：CraftTweaker 14.0.12、KubeJS 2001.6.5-build.20 魔改已在隔离服务端通过真实脚本引擎实测：16 条 CT、14 条 KJS 直接配方及奇点生成配方，覆盖等级/3×3–9×9 材料、实际匹配/合成/剩余物、删除、元数据与 KJS 覆盖 CT 的优先级；启动及 `/reload` 后各通过 324 项断言。单独撤掉任一来源脚本后，旧定义/配方被移除并恢复另一来源；全部撤回后恢复基础配方及定义。未修改原客户端脚本及存档。


## **无尽盾牌:**

潜行 + 使用物品可循环切换四种模式；没有模式数据的旧盾牌默认使用普通模式。

- **普通：**保留原有盾牌外观与主动格挡。
- **防御：**举盾时反射格挡伤害，持有时自动弹开弹射物。
- **终极防御：**持有即可被动免伤，但不拦截无尽伤害及绕过无敌的伤害。
- **漂浮：**在水和岩浆中停止下沉，潜行悬停、缓降并免除坠落伤害；不改写创造飞行和游泳时的运动。

主动格挡即时生效、覆盖全方向，不降低移速且允许疾跑。穿透箭和绕过盾牌的伤害仍可穿过主动格挡；举盾和收盾不产生物品交互振动。

## **✏️作者:**

- 程序: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- 美术: `MHanHanBing` `Neo-Tix`

## **🔒许可:**

- 代码: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- 材质: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

## **📌原始项目与历史下载:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

以上链接属于原始 Avaritia 项目，不是本仓库 Fabric JAR 的下载来源；本移植版请参阅[安装](#安装)。

## **❗使用说明:**
* 你**可以**将本模组添加到你制作的整合包。
* 可选安装 Fabric 版 EMI 或 JEI 查看无尽工作台和中子态素压缩机配方。
* 可选安装 Fabric 版 CraftTweaker 或 KubeJS 自定义并修改奇点。
* 使用 CraftTweaker 修改无尽工作台和中子态素压缩机配方。
* 使用 KubeJS 修改无尽工作台和中子态素压缩机配方。

## **🔎文档:**
* [Wiki](wiki)

## Discord
* [Discord](https://discord.gg/u5GN2Wqsbx)

## **⚙️开发:**
数据包教程不需要脚本模组。下方脚本教程需要安装对应的可选 **Fabric 1.20.1** CraftTweaker 或 KubeJS 及其依赖；Re-Avaritia 本身并不要求这些模组。

### **Singularities**
* [奇点Wiki](wiki/KUBEJS_SINGULARITY_GUIDE_CN.md)

数据包定义位于 `data/<命名空间>/singularities/<路径>.json`。JSON 内的 `name` 必须等于文件资源 ID，且 `count`、`timeCost` 必须大于 0。
```json
{
  "name": "example:iron",
  "displayName": "singularity.example.iron",
  "overlayColor": "e1e1e1",
  "underlayColor": "6c6c6c",
  "count": 1000,
  "timeCost": 240,
  "ingredient": { "item": "minecraft:iron_ingot" },
  "enabled": true,
  "recipeEnabled": true
}
```
历史字段 `timeRequired`、`recipeDisabled` 仍可迁移读取，但会记录弃用提示。为保持旧版 1.20 数据语义，`recipeDisabled` 中保存的布尔值仍按历史上的 `recipeEnabled` 含义读取，不会取反。数据包和脚本修改在执行 `/reload` 后生效。

### **CraftTweaker:**
```
mods.avaritia.CraftingTable.addShaped("name", tier, output, ingredients);//添加无尽工作台有序配方。
mods.avaritia.CraftingTable.addShapeless("name", tier, output, ingredients);//添加无尽工作台无序配方。
mods.avaritia.CraftingTable.remove(output);//删除无尽工作台配方。

mods.avaritia.Compressor.addRecipe("name", input, output, inputCount, timeCost);//添加中子态素压缩配方。
mods.avaritia.Compressor.remove(output);//移除中子态素压缩配方。

mods.avaritia.Singularity.register("key", "displayName", overlayColor, underlayColor, count, timeCost, ingredient, enabled, recipeEnable);//添加奇点
mods.avaritia.Singularity.remove("key");//删除指定奇点
mods.avaritia.Singularity.removeAll();//删除所有奇点
mods.avaritia.Singularity.removeRecipe("key");//删除指定奇点配方
mods.avaritia.Singularity.removeAllRecipe();//删除所有奇点配方

mods.avaritia.CraftingTable.addCatalyst("name", ingredients, catalystCount);//添加无尽催化剂配方
mods.avaritia.CraftingTable.addEnternal("name", ingredients);//添加永恒奇点配方，产出1个
mods.avaritia.CraftingTable.addEnternal("name", ingredients, eternalCount);//指定永恒奇点产出数量
```
ZenScript 文件放在 `scripts/`，开发客户端对应 `run/client/scripts/`。配方名称固定使用 `crafttweaker` 命名空间，应传入 `"my_pack/recipe"` 这样的独立路径，不传带冒号的 ID。有序材料为二维矩阵，无序材料为一维数组；等级 `0` 不限制工作台，`1`–`4` 分别要求 3×3、5×5、7×7、9×9。现有公开方法拼写为 **`addEnternal`**；不带数量时产出一个，数量重载控制实际合成产出。催化剂/永恒奇点可附加材料，并自动加入当前有材料的有效奇点，产品固定为对应的 Avaritia 物品。极限锻造需要模板、基底以及三个添加材料槽。特种配方的附加材料仍支持 CraftTweaker 剩余物变换。

`mods.avaritia.Singularity.register` 为静态调用。非法 ID、非正数 `count` 或 `timeCost` 会记录错误并只跳过当前定义；与奇点校验无关的脚本异常仍交给脚本引擎报告。奇点操作暂存至**整个服务端资源重载及可选脚本监听器全部完成**，再统一提交一次；冲突优先级为“数据包 < Java API < CraftTweaker < KubeJS”，同一来源内后操作覆盖前操作。修改脚本后执行 `/reload`。`removeRecipe` 保留奇点定义但禁用生成的压缩配方；`remove` 同时删除定义和配方。

### **KubeJs:**
```javascript
AvaritiaEvents.singularity(event => {
    event.removeRecipe("avaritia:coal")//删除指定奇点配方
    event.remove("avaritia:coal")//删除指定奇点
    event.removeAllRecipe()//删除所有奇点配方
    event.removeAll()//删除所有奇点
    event.register("avaritia:example", s => {
        s
            .setDisplayName("singularity.avaritia.example")
            .setColors(0xC0C0C0, 0x808080) // [overlay color, underlay color]
            .setCount(1000)
            .setTimeCost(200)
            .setIngredient(Ingredient.of("minecraft:iron_ingot"))
            .setEnabled(true)
            .setRecipeEnabled(true)
    })
})
ServerEvents.recipes(
    event => {
        const { avaritia } = event.recipes;
        avaritia.shaped_table(
            // 无序配方是 avaritia.shapeless_table
            4,//工作台等级
            "avaritia:infinity_sword",//产品
            [
                "       I ",
                "      III",
                "     III ",
                "    III  ",
                " C III   ",
                "  CII    ",
                "  NC     ",
                " N  C    ",
                "X        ",
            ],
            {
                C: "avaritia:crystal_matrix_ingot",
                I: "avaritia:infinity_ingot",
                N: "avaritia:neutron_ingot",
                X: "avaritia:infinity_catalyst",
            }//输入
        );
        //compressor
        avaritia
            .compressor("minecraft:copper_ingot", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
            .timeCost(240)//所需时间
            .inputCount(2000);//所需数量
        //需要先删除对应奇点配方
        avaritia.compressor(Item.of("minecraft:coal"), Item.of("avaritia:singularity", '{Id:"avaritia:coal"}'))
            .inputCount(10000)
            .timeCost(100)
        ;
        //infinity catalyst
        avaritia.infinity_catalyst(
            "default1",
            [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment"
            ],
            2//自定义无尽催化剂数量
        );

        avaritia.eternal_singularity(
            [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment"
            ],
        );
        console.log('Hello! The avaritia recipe event has fired!')
    }
)
```

服务端脚本放在 `kubejs/server_scripts/`，开发客户端对应 `run/client/kubejs/server_scripts/`；修改后重载。Avaritia 配方构造方法示例：

```javascript
ServerEvents.recipes(event => {
    var avaritia = event.recipes.avaritia;
    avaritia.shapeless_table(2, Item.of("minecraft:blue_dye", 4),
        ["minecraft:redstone", "minecraft:coal"]).id("my_pack:shapeless");
    avaritia.compressor("minecraft:sugar_cane", Item.of("minecraft:sugar", 2))
        .id("my_pack:compressor"); // 默认消耗1000，耗时240
    avaritia.infinity_catalyst("my_pack", ["minecraft:clay_ball", "minecraft:redstone"], 6)
        .id("my_pack:catalyst");
    avaritia.eternal_singularity(["minecraft:gold_ingot"], 3)
        .id("my_pack:eternal");
    avaritia.extreme_smithing(Item.of("minecraft:rabbit_foot", 2),
        "minecraft:netherite_upgrade_smithing_template", "minecraft:iron_ingot", "minecraft:gold_ingot")
        .id("my_pack:smithing");
});
```

无序材料必须为**一维数组**，等级 4 支持最多 81 槽。有序构造仍为 `(tier, result, pattern, key)`；压缩构造为 `(ingredient, result[, inputCount[, timeCost]])`。催化剂为 `(group, ingredients[, count])`，`"default"` 组自动加入有效奇点，自定义组仅使用指定材料；永恒奇点为 `(additionalIngredients[, count])`，自动加入有效奇点。这两类产品固定为催化剂和永恒奇点物品。锻造为 `(result, template, base, addition)`，也可用 `(result, base, addition)` 自动采用 Avaritia 升级模板。固定 Rhino 版本中，生成循环里每轮重建的数组和尺寸使用 `var`；循环内重复声明 `const` 的行为与现代浏览器 JavaScript 不同。

### **Fabric 附属模组依赖:**

本仓库目前没有配置 Maven 发布或上传仓库。Fabric 附属模组不要使用历史 `avaritia-forge` 坐标或 ForgeGradle 依赖配置。

1. 在本仓库执行 `gradlew.bat build`，得到重映射后的发布 JAR **`build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`**，模组版本为 **`1.20.1-1.4.2-fabric`**。
2. 将该 JAR 复制到 Fabric 附属模组的 `libs/` 目录，在附属模组的 Fabric Loom `build.gradle` 中配置：

```groovy
dependencies {
    modImplementation files('libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar')
}
```

Loom 会将此模组依赖映射到附属模组的开发映射，并加入开发运行时。这**不会**将 Re-Avaritia 内嵌到附属模组中；玩家仍需单独安装 Fabric 发布 JAR。参考 [Fabric Loom 依赖文档](https://wiki.fabricmc.net/documentation:fabric_loom)。

3. 若附属模组必须依赖 Re-Avaritia，在其 `fabric.mod.json` 已有的 `depends` 对象中加入模组 ID：

```json
"avaritia": "1.20.1-1.4.2-fabric"
```

保留附属模组自己的 Minecraft 1.20.1 与 Fabric Loader 要求。上述本地 JAR 教程不表示已经发布了 Fabric Maven 产物。



