<p align="center">
    <img width="690" src="web/logo.png" alt="title">
</p>
<hr>
<p align="center">
    <img src="https://img.shields.io/badge/Minecraft-26.3%20Fabric-c70039" alt="Supported Version">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia" title="Original upstream project, not a download listing for this Fabric port">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="Original upstream CurseForge downloads">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FARR-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> |
    <a href="README_CN.md">简体中文</a>
</p>

## **📕Introduction:**

* This unofficial Fabric port of Re:Avaritia brings Avaritia’s items, blocks, and recipes to Minecraft 26.3.

### Installation

Install Minecraft **26.3**, Java **25+**, and Fabric Loader **0.19.5+**. Install Fabric API for 26.3. This branch builds against `0.162.0+26.3`.

Place `Re-Avaritia-fabric-26.3-1.4.4-fabric.jar` in `mods/` on the client and server.

## **✏️Authors:**

- Programmer: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3`
- Artist: `MHanHanBing` `Neo-Tix`

## **🔒License:**

- Code: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- Texture resources: **All Rights Reserved (ARR)**. Texture owner: **MHanHanBing**. Without prior written permission from the author, these resources may not be copied, redistributed, modified, or used in modpacks. The owner reserves the right to pursue legal liability for unauthorized use.

## **📌Original projects and historical downloads:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

These are the original Avaritia projects, not download sources for this repository's Fabric JAR. See [Installation](#installation) for this port.

## **❗Attention:**
* Including this mod with its texture resources in a modpack requires prior written permission from the texture author.
* An optional Fabric release of JEI for Minecraft 26.3 can display recipes.

## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)

## Develop

Use the [Re:Avaritia customization skill](skills/re-avaritia-datapack/SKILL.md) to write datapacks and scripts supported by this branch.

### Build and run

Build with `./gradlew build`. Run the development client or server with `./gradlew runClient` or `./gradlew runServer`. On Windows, use `gradlew.bat`.

### Singularities and datapacks

Put definitions in `data/<namespace>/singularities/<path>.json` and match `name` to the file ID. Run `/reload` after edits. [Fields and recipe examples](skills/re-avaritia-datapack/references/datapack.md#singularities)

### Addon dependencies

Copy `Re-Avaritia-fabric-26.3-1.4.4-fabric.jar` into the addon’s `libs/` directory and add it to the Fabric Loom project.

```groovy
dependencies {
    implementation files('libs/Re-Avaritia-fabric-26.3-1.4.4-fabric.jar')
}
```

If the addon requires this mod, declare `"avaritia": "26.3-1.4.4-fabric"` in `fabric.mod.json` under `depends`.
