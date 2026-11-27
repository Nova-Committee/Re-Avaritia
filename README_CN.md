<p align="center">
    <img width="690" src="web/logo.png" alt="title">
</p>
<hr>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Available%20for-MC%201.20.1/1.21.1/26.1.2-c70039" alt="Supported Versions">
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

- 程序: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- 美术: `MHanHanBing` `Neo-Tix`

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


## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)

## 开发

Forge 与 Fabric 1.20.1 的构建和开发启动见[双加载器试点说明](MULTITARGET.md)。

可以使用 [Re:Avaritia 魔改 Skill](skills/re-avaritia-datapack/SKILL.md) 编写数据包和本分支支持的脚本。

### 奇点与数据包

把定义放在 `data/<命名空间>/singularities/<路径>.json`，让 `name` 与文件 ID 对应。修改后执行 `/reload`。[字段与配方示例](skills/re-avaritia-datapack/references/datapack.md#singularities)

### CraftTweaker

把 `.zs` 文件放进 `scripts/`。使用 `mods.avaritia.Singularity` 管理奇点，按本分支参考调用压缩机与工作台接口，永恒奇点方法名为 `addEnternal`。[调用示例](skills/re-avaritia-datapack/references/scripts.md#crafttweaker)

### KubeJS

把 `.js` 文件放进 `kubejs/server_scripts/`。用 `AvaritiaEvents.singularity` 写奇点，用 `ServerEvents.recipes` 写配方。[调用示例](skills/re-avaritia-datapack/references/scripts.md#kubejs)

### 附属模组依赖

在 ForgeGradle 工程中使用项目 Maven 仓库，并将 `avaritia_version` 设为所需的 Forge 版本。

```groovy
repositories { maven { url "https://maven.nova-committee.cn/releases" } }
dependencies { implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}") }
```

也可以在本仓库运行 `./gradlew publishToMavenLocal`，然后在附属模组的仓库配置中加入 `mavenLocal()`，使用相同坐标。
