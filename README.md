<p align="center">
    <img width="690" src="web/logo.png" alt="title">  
</p>
<hr>
<p align="center">
    <img src="https://img.shields.io/badge/Minecraft-1.20.1%20Fabric-c70039" alt="Supported Version">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia" title="Original upstream project, not a download listing for this Fabric port">
        <img src="https://cf.way2muchnoise.eu/623969.svg" alt="Original upstream CurseForge downloads">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> | 
    <a href="README_CN.md">简体中文</a>
</p>





## **📕Introduction:**
* This is the <span style="color: #ff0000;">Minecraft 1.20.1 Fabric port of Re-Avaritia</span>, bringing Avaritia's items, blocks, and recipes to Fabric.
* This mod is <span style="color: #ff6600;">unofficial</span>!

The migration source is the local `Avaritia-1.20` **1.4.2** codebase. This port retains the `committee.nova.mods.avaritia` Java package hierarchy; it is not a Forge mod.

### Requirements and optional integrations

- Requires Minecraft **1.20.1**, Java **17+**, and Fabric Loader **0.17.2+**.
- No prerequisite mods: Fabric API, Porting Lib, Cardinal Components, and Trinkets are not required or bundled.
- Uses vanilla registries, resource packs, menus, packets, storage interfaces, and lifecycle mixins. Server configuration is `config/avaritia-common.json`.
- Optional integrations: Trinkets, EMI, JEI, Jade, KubeJS, and CraftTweaker. Their own dependencies are needed only when installing those integrations.
- Optional Trinkets slots: rings in `hand/ring` and `offhand/ring`, infinity elytra in `chest/back`, crystal shovel in `chest/back` or `charm/charm`, and infinity totem in `charm/charm`.
- The upstream [fabric/1.20.1 reference](https://github.com/Nova-Committee/Re-Avaritia/tree/4ec454e0847cefcd08434722ec7c6704e7564fd5) is not used as a dependency baseline.

### Installation

Install Java 17+ and Fabric Loader 0.17.2+ for Minecraft 1.20.1. Build this repository as described below, then place **`build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`** in the instance's `mods/` directory, on both the client and the server when playing multiplayer. Use the remapped release JAR, not a `-sources` or development JAR; Forge/NeoForge builds are not interchangeable with it.

The default installation requires **only Fabric Loader**, not Fabric API or any integration mod. If you want Trinkets, EMI, JEI, Jade, KubeJS, or CraftTweaker, install their **Fabric 1.20.1** releases and their own required dependencies separately. Mod Menu is also available in the opt-in development profile below. The upstream links and download badge on this page are historical references, not proof that this local Fabric port is distributed there.

### Version configuration

Edit this repository's **`gradle.properties`**, rather than hard-coding versions in `build.gradle`. Core properties are:

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

With these values, the composed project/metadata version is **`1.20.1-1.4.2-fabric`** (`mc_version-mod_version-mod_version_tag`), the archive base is **`Re-Avaritia-fabric`** (`mod_name-mod_platform`), and the release filename remains **`Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`**. `mod_group_id` configures the Gradle group, not a Java package rename or an available Maven publication. Build plugin and dependency versions, including the optional integration pins, are also managed in this project properties file; changing them does not turn optional mods into default runtime prerequisites.

The checked-in Gradle Wrapper starts from `gradle/wrapper/gradle-wrapper.properties`. After changing `gradle_version`, run `gradlew.bat wrapper` to regenerate its distribution URL; run it again with the new Gradle version to refresh the wrapper scripts/JAR. It is not upgraded merely by editing the project property.

### Building and development

Build on Windows:

```bat
gradlew.bat build
gradlew.bat runClient
gradlew.bat runServer --args=nogui
```

Launch JEI, Mod Menu, CraftTweaker, and KubeJS together in the development client:

```bat
gradlew.bat -PwithClientIntegrations runClient
```

This opt-in profile pins JEI **15.20.0.112**, Mod Menu **7.2.2**, CraftTweaker **14.0.12**, and KubeJS **2001.6.5-build.20**, with their required development runtimes. They are not bundled or published as prerequisite mods. Fabric API is also available compile-only so Loom can correctly remap optional APIs; the default runtime still does not load it. Dependency interface injection is disabled for compilation: installed integrations supply those interfaces through their actual runtime mixins.

The remapped release JAR is `build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`. Native regression fixtures are excluded from that JAR; to run them separately:

```bat
gradlew.bat -PgameTests runGameTestServer --args=nogui
```

In the test server console, run `execute positioned 0 80 0 run test runall`, then `stop` after all native test results. Add `-PwithTrinkets` to exercise installed Trinkets in development; only this opt-in profile adds the official Trinkets release and its required Fabric API / Cardinal Components runtimes. The default profile is unchanged.

For native Windows client checks, confirm keyboard focus and check the Chinese input-method mode if chat or ASCII commands do not respond. Try `Ctrl+Space` or a temporary English keyboard layout, then open command chat with `/` and enter `/time set day`. Recheck the layout after reactivating the window and restore it after testing; permanent system-language changes are unnecessary. Require actual command feedback rather than automation key-delivery acknowledgement. If background automation loses modifiers (for example, `Shift+2` enters `2` instead of `@`), retry with the verified game window in the foreground rather than changing mod input handlers.

Historical verification before this build/property and README alignment: full build, 53 JUnit tests, 50 native GameTests without prerequisite mods, and dedicated-server startup/reload/save. The earlier installed-Trinkets pass covered 45 native GameTests, including infinity elytra, crystal shovel, and infinity totem. Actual Fabric client checks cover personal-dimension reentry/weather isolation, infinity-chest search and GUI-scale restoration, wearing a ring in the Trinkets screen, distinct emerald/glowstone compression recipes in EMI, and the 16-page neutron-compression category in standalone JEI. Halo rendering preserves baked vertex RGB/alpha, with native inventory/hotbar checks for black halos and translucent neutron effects plus real vertex-buffer regressions. The four-mod development client also boots and loads a world with CraftTweaker/KubeJS scripts. Tesseract labels, complete normal/crafting panels, infinity armor, all shield modes, and ordinary blaze-bow shots passed user-led manual acceptance. Jade has not received native client verification. These records do not claim a new verification run for the current build-configuration changes.

Historical script-engine verification: CraftTweaker 14.0.12 and KubeJS 2001.6.5-build.20 customization was exercised through the real script engines in an isolated dedicated server: 16 CT and 14 KJS direct recipes, generated singularity recipes, tiers/3×3–9×9 grids, actual ingredient matching/assembly/remainders, deletion, metadata, and KJS-over-CT precedence passed 324 assertions on startup and again after `/reload`. Withdrawing only one script source restored the surviving layer and removed its obsolete definitions/recipes; withdrawing both restored the baseline recipes and definitions. Original client scripts and worlds were not modified.


## **Infinity Shield:**

Sneak + use cycles through four modes. Existing shields without mode data default to Normal.

- **Normal:** the original shield appearance and active blocking.
- **Defending:** reflects blocked damage while raised and deflects projectiles while held.
- **Ultimate Defense:** grants passive protection while held, except against Infinity damage and damage that bypasses invulnerability.
- **Floating:** prevents sinking in water/lava, hovers while sneaking, slows falling, and prevents fall damage. Creative flight and swimming movement are left unchanged.

Active blocking is immediate and omnidirectional, does not slow movement, and permits sprinting. Piercing arrows and shield-bypassing damage still bypass active blocking. Raising and lowering the shield do not emit item-interaction vibrations.

## **✏️Authors:**

- Programmer: `cnlimiter` `IAFEnvoy` `Frostbite-time` `cu6` `MikhailTapio` `Asek3` 
- Artist: `MHanHanBing` `Neo-Tix`

## **🔒License:**

- Code: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- Assets: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

## **📌Original projects and historical downloads:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

These are the original Avaritia projects, not download sources for this repository's Fabric JAR. See [Installation](#installation) for this port.

## **❗Attention:**
* You&nbsp;<span style="color: #00ff00;"> **DEFINITELY CAN** </span>&nbsp;add the mod to your modpack.
* Recipe viewing is supported via the optional Fabric versions of EMI or JEI.
* You can customize singularities using the optional Fabric versions of CraftTweaker and KubeJS.
* You can add recipes using CraftTweaker.
* You can add recipes using KubeJS.

## **🔎Wiki:**
* [Wiki](wiki)

## **🦀Discord:**
* [Discord](https://discord.gg/u5GN2Wqsbx)

## **⚙️Develop:**
The datapack tutorial requires no script mod. The following script tutorials require the corresponding optional **Fabric 1.20.1** CraftTweaker or KubeJS installation and its dependencies; neither is required by Re-Avaritia itself.

### **Singularities:**
Datapack definitions live at `data/<namespace>/singularities/<path>.json`. The JSON `name` must equal `<namespace>:<path>`, and both `count` and `timeCost` must be greater than zero.
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
The historical `timeRequired` and `recipeDisabled` aliases are still read with a deprecation warning. To preserve old 1.20 data, the stored `recipeDisabled` boolean keeps its historical `recipeEnabled` meaning and is not inverted. Datapack and script changes take effect on `/reload`.

### **CraftTweaker:**
```
mods.avaritia.CraftingTable.addShaped("name", tier, output, ingredients);
mods.avaritia.CraftingTable.addShapeless("name", tier, output, ingredients);
mods.avaritia.CraftingTable.remove(output);

mods.avaritia.Compressor.addRecipe("name", input, output, inputCount, timeCost);
mods.avaritia.Compressor.remove(output);

mods.avaritia.Singularity.register("key", "displayName", overlayColor, underlayColor, count, timeCost, ingredient, enabled, recipeEnable);
mods.avaritia.Singularity.remove("key");
mods.avaritia.Singularity.removeAll();
mods.avaritia.Singularity.removeRecipe("key");
mods.avaritia.Singularity.removeAllRecipe();

mods.avaritia.CraftingTable.addCatalyst("name", ingredients, catalystCount);
mods.avaritia.CraftingTable.addEnternal("name", ingredients);
mods.avaritia.CraftingTable.addEnternal("name", ingredients, eternalCount);
```
Put ZenScript files in `scripts/` (`run/client/scripts/` for the development client). Recipe names use the `crafttweaker` namespace; use a unique path such as `"my_pack/recipe"`, not a colon-prefixed ID. Shaped ingredients are a matrix; shapeless ingredients are a flat array. Tier `0` is unrestricted, and tiers `1`–`4` require 3×3, 5×5, 7×7, or 9×9 tables. The existing public spelling is **`addEnternal`**; its two-argument ingredients overload produces one eternal singularity, while the count overload controls the actual crafted quantity. Catalyst/eternal recipes accept additional materials and automatically include effective material-bearing singularities; their outputs are fixed Avaritia items. Extreme smithing requires a template, base, and three copies of the addition ingredient. CraftTweaker ingredient remainder transformations remain supported for additional special-recipe materials.

`mods.avaritia.Singularity.register` is a static call. Invalid IDs and non-positive `count` or `timeCost` values are logged and only that definition is skipped; unrelated script errors are still reported by the script engine. Singularity operations are staged and committed once **after the entire server resource reload, including optional script listeners, completes**. Conflicts resolve as datapack < Java API < CraftTweaker < KubeJS, while later operations from the same source win. After editing a script, run `/reload`. `removeRecipe` retains the singularity definition while disabling its generated compressor recipe; `remove` deletes both.

### **KubeJs:**
```javascript
AvaritiaEvents.singularity(event => {
    event.removeRecipe("avaritia:coal")//remove recipe
    event.remove("avaritia:coal")//remove singularity
    event.removeAllRecipe()//remove all singularity recipe
    event.removeAll()//remove all singularity
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
            // shapeless is avaritia.shapeless_table
            4,
            "avaritia:infinity_sword",
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
            }
        );
        //compressor
        avaritia
            .compressor("minecraft:copper_ingot", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
            .timeCost(240)
            .inputCount(2000);
        avaritia.compressor(Item.of("minecraft:coal"), Item.of("avaritia:singularity", '{Id:"avaritia:coal"}'))
            .inputCount(10000)
            .timeCost(100)
        ;//remove singularity recipe first
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
            2//custom infinity catalyst count
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

Server scripts go in `kubejs/server_scripts/` (`run/client/kubejs/server_scripts/` for the development client). Reload after editing. The Avaritia recipe constructors are:

```javascript
ServerEvents.recipes(event => {
    var avaritia = event.recipes.avaritia;
    avaritia.shapeless_table(2, Item.of("minecraft:blue_dye", 4),
        ["minecraft:redstone", "minecraft:coal"]).id("my_pack:shapeless");
    avaritia.compressor("minecraft:sugar_cane", Item.of("minecraft:sugar", 2))
        .id("my_pack:compressor"); // default inputCount=1000, timeCost=240
    avaritia.infinity_catalyst("my_pack", ["minecraft:clay_ball", "minecraft:redstone"], 6)
        .id("my_pack:catalyst");
    avaritia.eternal_singularity(["minecraft:gold_ingot"], 3)
        .id("my_pack:eternal");
    avaritia.extreme_smithing(Item.of("minecraft:rabbit_foot", 2),
        "minecraft:netherite_upgrade_smithing_template", "minecraft:iron_ingot", "minecraft:gold_ingot")
        .id("my_pack:smithing");
});
```

Shapeless materials are a **flat array**, supporting up to 81 slots on tier 4. Shaped constructors remain `(tier, result, pattern, key)`. Compressor constructors accept `(ingredient, result[, inputCount[, timeCost]])`. Catalyst constructors accept `(group, ingredients[, count])`; group `"default"` adds effective singularities, while a custom group uses only the supplied ingredients. Eternal constructors accept `(additionalIngredients[, count])` and automatically add effective singularities. These two outputs are fixed to the catalyst and eternal-singularity items. Smithing is `(result, template, base, addition)` or `(result, base, addition)` with the Avaritia upgrade template. In this pinned Rhino version, use `var` for arrays/dimensions recreated inside generation loops; repeated block-local `const` declarations do not behave like modern browser JavaScript.


### **Dependencies for Fabric addons:**

This repository does not currently configure a Maven publication or upload repository. Do not use the historical `avaritia-forge` coordinate or ForgeGradle's dependency setup for a Fabric addon.

1. Run `gradlew.bat build` in this repository. The remapped release JAR is **`build/libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar`**, with mod version **`1.20.1-1.4.2-fabric`**.
2. Copy that JAR into the Fabric addon's `libs/` directory. In the addon's Fabric Loom `build.gradle`, use:

```groovy
dependencies {
    modImplementation files('libs/Re-Avaritia-fabric-1.20.1-1.4.2-fabric.jar')
}
```

Loom remaps this mod dependency to the addon's development mappings and adds it to its development runtime. This does **not** bundle Re-Avaritia into your addon; players still install the Fabric release JAR separately. See the [Fabric Loom dependency documentation](https://wiki.fabricmc.net/documentation:fabric_loom).

3. If the addon requires Re-Avaritia, add its mod ID to the existing `depends` object in the addon's `fabric.mod.json`:

```json
"avaritia": "1.20.1-1.4.2-fabric"
```

Keep the addon's own Minecraft 1.20.1 and Fabric Loader requirements. The local JAR tutorial is not a claim that a Fabric Maven artifact has been published.



