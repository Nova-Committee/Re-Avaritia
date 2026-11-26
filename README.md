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

## **📕Introduction:**
* <span style="color: #ff0000;">This mod adds all from Avaritia.</span>
* This mod is <span style="color: #ff6600;">unofficial</span>!

## **✏️Authors:**

- Programmer: `cnlimiter` `Asek3` `MikhailTapio`

## **🔒License:**

- Code: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- Texture resources: **All Rights Reserved (ARR)**. Texture owner: **MHanHanBing**. Without prior written permission from the author, these resources may not be copied, redistributed, modified, or used in modpacks. The owner reserves the right to pursue legal liability for unauthorized use.

## **📌Download official:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* Including this mod with its texture resources in a modpack requires prior written permission from the texture author.
* Recipe viewing is supported via JEI.

## **🔎Wiki:**
* [Wiki](wiki)

## Discord

* [Discord](https://discord.gg/u5GN2Wqsbx)

## Develop

Use the [Re:Avaritia customization skill](skills/re-avaritia-datapack/SKILL.md) to write datapacks and scripts supported by this branch.

### UI geometry inspection (development only)

Run `./gradlew runClient -PuiInspect=true` or use `gradlew.bat` on Windows. Use a dedicated inspection world in `build/ui-preview`.

- **Ctrl+Alt+F8** toggles control bounds, clipping, hit areas, and state.
- **Ctrl+Alt+F9** exports two PNGs and JSON from one rendered frame to `build/ui-inspection`. Wait for the success path before reading the files.

### Singularities and datapacks

Put definitions in `data/<namespace>/singularities/<path>.json` and match `name` to the file ID. Run `/reload` after edits. [Fields and recipe examples](skills/re-avaritia-datapack/references/datapack.md#singularities)

### CraftTweaker

Put `.zs` files in `scripts/`. Use `mods.avaritia.Singularity` for definitions and follow the branch’s worktable and compressor calls. The eternal-singularity method is `addEnternal`. [Examples](skills/re-avaritia-datapack/references/scripts.md#crafttweaker)

### KubeJS

Put `.js` files in `kubejs/server_scripts/`. Use `AvaritiaEvents.singularity` for definitions and `ServerEvents.recipes` for recipes. [Examples](skills/re-avaritia-datapack/references/scripts.md#kubejs)

### Infinity Catalyst

Use `avaritia:infinity_catalyst` to add a recipe. Group `"default"` includes eligible singularities. [Recipe example](skills/re-avaritia-datapack/references/datapack.md#tiered-crafting-tables)

### Addon dependencies

Place the matching `Re-Avaritia-neoforge-1.21.1-1.4.4-release.jar` in the addon’s `libs/` directory and add it as a development dependency in the NeoForge project.

```groovy
dependencies { implementation files('libs/Re-Avaritia-neoforge-1.21.1-1.4.4-release.jar') }
```
