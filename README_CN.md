<p align="center">
    <img width="690" src="web/logo.png" alt="title">  
</p>
<hr>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Minecraft-1.20.1%20Fabric-c70039" alt="支持版本">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="CurseForge Download">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> | 
    <a href="README_CN.md">简体中文</a>
</p>





## **📕介绍:**
* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)的重铸版
* 此模组是非官方版本!

## Fabric 1.20.1

以本地 `Avaritia-1.20` 的 1.4.2 为迁移源，保留 `committee.nova.mods.avaritia` 包层级。

- 运行要求：Minecraft **1.20.1**、Java **17+**、Fabric Loader **0.17.2+**。
- **无前置模组**：不要求或内嵌 Fabric API、Porting Lib、Cardinal Components、Trinkets。
- 注册表、资源包、菜单、网络、存储和生命周期均采用项目内的原版接口与 Mixin 实现。服务端配置为 `config/avaritia-common.json`。
- Trinkets、EMI、JEI、Jade、KubeJS、CraftTweaker 为可选联动；安装这些模组时才需满足它们自身的依赖。
- 可选 Trinkets 槽位：两手 `ring` 槽支持戒指，`chest/back` 支持无尽鞘翅，`chest/back` 或 `charm/charm` 支持水晶矩阵锹，`charm/charm` 支持无尽图腾。
- 参考上游 [fabric/1.20.1 固定提交](https://github.com/Nova-Committee/Re-Avaritia/tree/4ec454e0847cefcd08434722ec7c6704e7564fd5)，不继承其前置依赖。

Windows 构建与运行：

```bat
gradlew.bat build
gradlew.bat runClient
gradlew.bat runServer --args=nogui
```

产物位于 `build/libs/`。原生回归夹具不打入发布 JAR：

```bat
gradlew.bat -PgameTests runGameTestServer --args=nogui
```

测试服务端控制台执行 `execute positioned 0 80 0 run test runall`，等原生测试全部报告后执行 `stop`。开发环境安装 Trinkets 时，在启动命令加 `-PwithTrinkets`；仅该可选配置引入 Trinkets 自身的传递运行时依赖。

Windows 原生客户端测试时，先确认游戏窗口焦点；如果聊天快捷键或英文命令无响应，检查中文输入法模式，可用 `Ctrl+Space` 切换后输入 `/time set day`。必须观察命令反馈，而不是仅以自动化工具报告按键已发送判断成功。

已验证独立运行基线：完整构建、42 项 JUnit、39 项原生 GameTest、独立服务端启动/重载/保存，以及真实 Fabric 客户端启动。可选联动和具体界面验收与该基线分开验证。


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

## **📌其他下载:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* 你**可以**将本模组添加到你制作的整合包.
* JEI查看无尽工作台和中子态素压缩机配方.
* 可以利用CraftTweaker和KubeJs自定义并修改奇点!
* 使用CraftTweaker修改无尽工作台和中子态素压缩机配方!
* 使用KubeJs修改无尽工作台和中子态素压缩机配方!

## **🔎文档:**
* [Wiki](wiki)

## **⚙️开发:**
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

mods.avaritia.CraftingTable.addCatalyst("name", ingredients, catalystCount)//添加无尽催化剂配方
mods.avaritia.CraftingTable.addEternal("name", ingredients)//添加永恒奇点配方
```
`mods.avaritia.Singularity.register` 为静态调用。非法 ID、非正数 `count` 或 `timeCost` 会记录错误并只跳过当前定义；与奇点校验无关的脚本异常仍交给脚本引擎报告。奇点操作会暂存到本轮资源重载的脚本阶段结束，再统一提交一次；冲突优先级为“数据包 < Java API < CraftTweaker < KubeJS”，同一来源内后操作覆盖前操作。脚本变更仅在执行 `/reload` 后生效。

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
            .compressor("#forge:ingots/copper", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
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
### **Dependencies:**
avaritia_version 请查看这里 [here](https://maven.nova-committee.cn/s3/committee/nova/mods/avaritia-forge/)
```groovy
repositories {
    maven {
        url "https://maven.nova-committee.cn/releases"
    }
}

dependencies {
    implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}")
}
```



