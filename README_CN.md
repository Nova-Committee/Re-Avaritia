<p align="center">
    <img width="690" src="web/logo.png" alt="title">
</p>
<hr>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="CurseForge Download">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FARR-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> |
    <a href="README_CN.md">简体中文</a>
</p>

Re:Avaritia 是 [Avaritia（无尽贪婪）](https://www.mcmod.cn/class/505.html) 的非官方重制版，提供无尽工作台、中子态素压缩机与奇点等内容。安装时请为你的游戏版本和加载器选择对应的模组文件。 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/re-avaritia) · [Modrinth](https://modrinth.com/mod/re-avaritia)

## 开发入口

| 游戏版本 | 构建目标 |
| --- | --- |
| 1.20.1 | [Forge](targets/forge/) · [Fabric](targets/fabric/) |
| 1.21.1 | [NeoForge](targets/neoforge-1.21.1/) |
| 1.21.11 | [NeoForge](targets/neoforge-1.21.11/) |
| 26.1.2 | [NeoForge](targets/neoforge-26.1.2/) |
| 26.3 | [Fabric](targets/fabric-26.3/) · [NeoForge](targets/neoforge-26.3/) |

在仓库根目录执行 `bash build-targets.sh forge` 构建单个目标，或执行 `bash build-targets.sh all` 构建七个目标。各目标在 `targets/<目标>/build/libs/` 生成 JAR。[构建、测试与数据生成](guides/building_CN.md)

加载器专用源码位于各 [目标目录](targets/)，共享 Java 和资源在 [shared/src/main](shared/src/main/)。

## 数据包与脚本

Forge 1.20.1 的[数据包](skills/re-avaritia-datapack/references/datapack.md)、[脚本 API](skills/re-avaritia-datapack/references/scripts.md)和 [KubeJS 奇点指南](guides/forge-1.20.1/KUBEJS_SINGULARITY_GUIDE_CN.md)可用于编写奇点和配方，也可以使用[魔改 Skill](skills/re-avaritia-datapack/SKILL.md)。

## 作者

- 程序: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- 美术: `MHanHanBing` `Neo-Tix`

## 许可

- 代码: [MIT](LICENSE)
- 贴图资源：**All Rights Reserved（ARR，保留所有权利）**。贴图所有者：**MHanHanBing**。未经作者事先书面许可，不得复制、再分发、修改或用于整合包。所有者保留对未经授权的使用追究法律责任的权利。

## 其他 Avaritia 项目
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## 游戏内支持
* 可用 JEI 查看无尽工作台和中子态素压缩机配方。

## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)
