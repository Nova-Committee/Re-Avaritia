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

Re:Avaritia is an unofficial recreation of Avaritia, with extreme crafting, neutron compression and singularities. Choose a mod file for your Minecraft version and loader. [CurseForge](https://www.curseforge.com/minecraft/mc-mods/re-avaritia) · [Modrinth](https://modrinth.com/mod/re-avaritia)

## Develop

| Minecraft | Build targets |
| --- | --- |
| 1.20.1 | [Forge](targets/forge/) · [Fabric](targets/fabric/) |
| 1.21.1 | [NeoForge](targets/neoforge-1.21.1/) |
| 1.21.11 | [NeoForge](targets/neoforge-1.21.11/) |
| 26.1.2 | [NeoForge](targets/neoforge-26.1.2/) |
| 26.3 | [Fabric](targets/fabric-26.3/) · [NeoForge](targets/neoforge-26.3/) |

From the repository root, run `bash build-targets.sh forge` for one target or `bash build-targets.sh all` for all seven. Each target writes its JARs to `targets/<target>/build/libs/`. [Build, test and generate data](guides/building.md)

Loader-specific code lives under [targets](targets/). Common Java and resources are in [shared/src/main](shared/src/main/).

## Datapacks and scripts

For Forge 1.20.1, see the [datapack reference](skills/re-avaritia-datapack/references/datapack.md), [scripting API](skills/re-avaritia-datapack/references/scripts.md) and [KubeJS singularity guide](guides/forge-1.20.1/KUBEJS_SINGULARITY_GUIDE.md), or use the [customization skill](skills/re-avaritia-datapack/SKILL.md).

## Authors

- Programmer: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- Artist: `MHanHanBing` `Neo-Tix`

## License

- Code: [MIT](LICENSE)
- Texture resources: **All Rights Reserved (ARR)**. Texture owner: **MHanHanBing**. Without prior written permission from the author, these resources may not be copied, redistributed, modified, or used in modpacks. The owner reserves the right to pursue legal liability for unauthorized use.

## Related Avaritia projects
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## In-game support
* Recipe viewing is supported via JEI.

## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)
