<p align="center">
    <img width="690" src="web/logo.png" alt="title">
</p>
<hr>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Minecraft-26.3%20NeoForge-c70039" alt="Supported Versions">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="CurseForge Download">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FARR-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> |
    <a href="README_CN.md">简体中文</a>
</p>

## **📕介绍:**
* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)的重铸版
* 此模组是非官方版本!

## **✏️作者:**

- 程序: `cnlimiter` `Asek3` `MikhailTapio` `lying_ice`

## **🔒许可:**

- 代码: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- 贴图资源：**All Rights Reserved（ARR，保留所有权利）**。贴图所有者：**MHanHanBing**。未经作者事先书面许可，不得复制、再分发、修改或用于整合包。所有者保留对未经授权的使用追究法律责任的权利。

## **📌其他下载:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* 将包含贴图资源的本模组用于整合包前，须取得贴图作者的事先书面许可。
* JEI查看无尽工作台和中子态素压缩机配方.
* 若旧版 NeoForge 26.3 改写了全服出生点，管理员可在原维度用 `/setworldspawn <x> <y> <z>` 恢复原坐标，例如原点为主世界 `(96, -60, -72)` 时执行 `/execute in minecraft:overworld run setworldspawn 96 -60 -72`。


## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)

## 开发

可以使用 [Re:Avaritia 魔改 Skill](skills/re-avaritia-datapack/SKILL.md) 编写数据包和本分支支持的脚本。

### 奇点与数据包

把定义放在 `data/<命名空间>/singularities/<路径>.json`，让 `name` 与文件 ID 对应。修改后执行 `/reload`。[字段与配方示例](skills/re-avaritia-datapack/references/datapack.md#singularities)

### KubeJS

本分支打包了 KubeJS 接口，运行需要支持 NeoForge 26.3 的 KubeJS 与 Rhino。有匹配版本时，把脚本放入 `kubejs/server_scripts/`。[脚本接口](skills/re-avaritia-datapack/references/scripts.md#kubejs)

### 无尽催化剂

使用 `avaritia:infinity_catalyst` 添加配方。`group: "default"` 会加入符合条件的奇点。[配方示例](skills/re-avaritia-datapack/references/datapack.md#tiered-crafting-tables)

### 附属模组依赖

把与目标版本一致的 `Re-Avaritia-neoforge-26.3-1.4.4-release.jar` 放到附属模组的 `libs/`，加入 NeoForge 工程的开发依赖。

```groovy
dependencies { implementation files('libs/Re-Avaritia-neoforge-26.3-1.4.4-release.jar') }
```
