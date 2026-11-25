<p align="center">
    <img width="690" src="web/logo.png" alt="title">
</p>
<hr>
<p align="center">
    <img src="https://img.shields.io/badge/Minecraft-1.20.1%20Fabric-c70039" alt="支持版本">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia" title="原上游项目，不是本 Fabric 移植版的下载列表">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="原上游 CurseForge 下载统计">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FARR-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> |
    <a href="README_CN.md">简体中文</a>
</p>

## **📕介绍:**

* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)重铸版 **Re:Avaritia 的 Minecraft 1.20.1 Fabric 移植版**。
* 此模组是非官方版本。

### 安装

安装 Minecraft **1.20.1**、Java **17+** 和 Fabric Loader **0.17.2+**。 本体可单独运行。启用 Trinkets、EMI、JEI、Jade、CraftTweaker 或 KubeJS 等可选联动时，另装对应 Fabric 版本及其前置。

将 `Re-Avaritia-fabric-1.20.1-1.4.4-fabric.jar` 放入 `mods/`。多人游戏时，客户端和服务端都安装对应版本。

## **✏️作者:**

- 程序: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- 美术: `MHanHanBing` `Neo-Tix`

## **🔒许可:**

- 代码: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- 贴图资源：**All Rights Reserved（ARR，保留所有权利）**。贴图所有者：**MHanHanBing**。未经作者事先书面许可，不得复制、再分发、修改或用于整合包。所有者保留对未经授权的使用追究法律责任的权利。

## **📌原始项目与历史下载:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

以上链接属于原始 Avaritia 项目，不是本仓库 Fabric JAR 的下载来源；本移植版请参阅[安装](#安装)。

## **❗使用说明:**
* 将包含贴图资源的本模组用于整合包前，须取得贴图作者的事先书面许可。
* 可选安装 Fabric 版 EMI 或 JEI 查看无尽工作台和中子态素压缩机配方。

## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)

## 开发

可以使用 [Re:Avaritia 魔改 Skill](skills/re-avaritia-datapack/SKILL.md) 编写数据包和本分支支持的脚本。

### 构建与运行

运行 `./gradlew build` 构建，或用 `./gradlew runClient`、`./gradlew runServer` 启动开发环境。Windows 使用 `gradlew.bat`。 使用 `./gradlew -PwithClientIntegrations runClient` 启动带脚本和 JEI 的开发客户端。

### 奇点与数据包

把定义放在 `data/<命名空间>/singularities/<路径>.json`，让 `name` 与文件 ID 对应。修改后执行 `/reload`。[字段与配方示例](skills/re-avaritia-datapack/references/datapack.md#singularities)

### CraftTweaker

把 `.zs` 文件放进 `scripts/`。使用 `mods.avaritia.Singularity` 管理奇点，按本分支参考调用压缩机与工作台接口，永恒奇点方法名为 `addEnternal`。[调用示例](skills/re-avaritia-datapack/references/scripts.md#crafttweaker)

### KubeJS

把 `.js` 文件放进 `kubejs/server_scripts/`。用 `AvaritiaEvents.singularity` 写奇点，用 `ServerEvents.recipes` 写配方。[调用示例](skills/re-avaritia-datapack/references/scripts.md#kubejs)

### 附属模组依赖

将构建后的 `Re-Avaritia-fabric-1.20.1-1.4.4-fabric.jar` 放入附属模组的 `libs/`，在 Fabric Loom 工程中加入依赖。

```groovy
dependencies {
    modImplementation files('libs/Re-Avaritia-fabric-1.20.1-1.4.4-fabric.jar')
}
```

如果附属模组要求安装本体，在 `fabric.mod.json` 的 `depends` 中声明 `"avaritia": "1.20.1-1.4.4-fabric"`。
