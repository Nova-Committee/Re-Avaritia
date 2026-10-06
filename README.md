<p align="center">
    <img width="690" src="web/logo.png" alt="title">  
</p>
<hr>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Minecraft-1.20.1%20Fabric-c70039" alt="Supported Version">
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





## **📕Introduction:**
* <span style="color: #ff0000;">This mod adds all from Avaritia.</span>
* This mod is <span style="color: #ff6600;">unofficial</span>!

## Fabric 1.20.1

This repository ports the local `Avaritia-1.20` 1.4.2 codebase to Fabric while retaining the `committee.nova.mods.avaritia` package hierarchy.

- Requires Minecraft **1.20.1**, Java **17+**, and Fabric Loader **0.17.2+**.
- No prerequisite mods: Fabric API, Porting Lib, Cardinal Components, and Trinkets are not required or bundled.
- Uses vanilla registries, resource packs, menus, packets, storage interfaces, and lifecycle mixins. Server configuration is `config/avaritia-common.json`.
- Optional integrations: Trinkets, EMI, JEI, Jade, KubeJS, and CraftTweaker. Their own dependencies are needed only when installing those integrations.
- Optional Trinkets slots: rings in `hand/ring` and `offhand/ring`, infinity elytra in `chest/back`, crystal shovel in `chest/back` or `charm/charm`, and infinity totem in `charm/charm`.
- The upstream [fabric/1.20.1 reference](https://github.com/Nova-Committee/Re-Avaritia/tree/4ec454e0847cefcd08434722ec7c6704e7564fd5) is not used as a dependency baseline.

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

Artifacts are under `build/libs/`. Native regression fixtures are excluded from the release JAR:

```bat
gradlew.bat -PgameTests runGameTestServer --args=nogui
```

In the test server console, run `execute positioned 0 80 0 run test runall`, then `stop` after all native test results. Add `-PwithTrinkets` to exercise installed Trinkets in development; only this opt-in profile adds the official Trinkets release and its required Fabric API / Cardinal Components runtimes. The default profile is unchanged.

For native Windows client checks, confirm keyboard focus and check the Chinese input-method mode if chat or ASCII commands do not respond. Try `Ctrl+Space` or a temporary English keyboard layout, then open command chat with `/` and enter `/time set day`. Recheck the layout after reactivating the window and restore it after testing; permanent system-language changes are unnecessary. Require actual command feedback rather than automation key-delivery acknowledgement. If background automation loses modifiers (for example, `Shift+2` enters `2` instead of `@`), retry with the verified game window in the foreground rather than changing mod input handlers.

Verified: full build, 53 JUnit tests, 46 native GameTests without prerequisite mods, and dedicated-server startup/reload/save. The earlier installed-Trinkets pass covered 45 native GameTests, including infinity elytra, crystal shovel, and infinity totem. Actual Fabric client checks cover personal-dimension reentry/weather isolation, infinity-chest search and GUI-scale restoration, wearing a ring in the Trinkets screen, distinct emerald/glowstone compression recipes in EMI, and the 16-page neutron-compression category in standalone JEI. Halo rendering preserves baked vertex RGB/alpha, with native inventory/hotbar checks for black halos and translucent neutron effects plus real vertex-buffer regressions. The four-mod development client also boots and loads a world with CraftTweaker/KubeJS scripts. Tesseract labels, complete normal/crafting panels, infinity armor, all shield modes, and ordinary blaze-bow shots passed user-led manual acceptance. Jade has not received native client verification.


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

## **📌Download official:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* You&nbsp;<span style="color: #00ff00;"> **DEFINITELY CAN** </span>&nbsp;add the mod to your modpack.
* Recipe viewing is supported via JEI.
* You can add&nbsp;singularity by using CraftTweaker and KubeJs!
* You can add recipes by CraftTweaker!
* You can add recipes by KubeJs!

## **🔎Wiki:**
* [Wiki](wiki)

## **🦀Discord:**
* [Discord](https://discord.gg/u5GN2Wqsbx)

## **⚙️Develop:**
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

mods.avaritia.CraftingTable.addCatalyst("name", ingredients, catalystCount)
mods.avaritia.CraftingTable.addEternal("name", ingredients)
```
`mods.avaritia.Singularity.register` is a static call. Invalid IDs and non-positive `count` or `timeCost` values are logged and only that definition is skipped; unrelated script errors are still reported by the script engine. Singularity operations are staged until the current resource reload finishes its script phase and then committed once. Conflicts resolve as datapack < Java API < CraftTweaker < KubeJS, while later operations from the same source win. Script changes take effect only after `/reload`.

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
            .compressor("#forge:ingots/copper", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
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

### **Dependencies:**
avaritia_version see this [here](https://maven.nova-committee.cn/s3/committee/nova/mods/avaritia-forge/)
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



