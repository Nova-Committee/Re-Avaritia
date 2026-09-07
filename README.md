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
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="https://github.com/Nova-Committee/Re-Avaritia/blob/1.20.1-forged/README.md">English</a> | 
    <a href="https://github.com/Nova-Committee/Re-Avaritia/blob/1.20.1-forged/README_CN.md">简体中文</a>
</p>





## **📕Introduction:**
* <span style="color: #ff0000;">This mod adds all from Avaritia.</span>
* This mod is <span style="color: #ff6600;">unofficial</span>!

## **✏️Authors:**

- Programmer: `cnlimiter` `Asek3` `MikhailTapio`

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
* You can add&nbsp;singularity by using json!
* You can add recipes by CraftTweaker!
* You can add recipes by KubeJs!

## **🔎Wiki:**
* [Wiki](wiki)

## **🦀Discord:**
* [Discord](https://discord.gg/u5GN2Wqsbx)

## Infinity Shield

Sneak-right-click to cycle through four modes; right-click normally to block. Both main-hand and off-hand use are supported.

| Mode | Effect |
| --- | --- |
| Normal | Active shield blocking. |
| Defending | Reflects blocked damage and deflects incoming projectiles while held. |
| Ultimate defending | Prevents most damage while held; Infinity damage and damage that bypasses invulnerability remain exceptions. |
| Floating | Prevents fall damage, slows descent, hovers while sneaking, and provides buoyancy in water and lava. |

Blocking takes effect immediately, does not slow movement, and allows sprinting. Piercing arrows and shield-bypassing damage retain their exceptions. Raising and lowering this shield emit no item-interaction vibrations. Floating mode does not grant creative flight.

## **⚙️Develop:**

### UI geometry inspection (development only)

Run `./gradlew runClient -PuiInspect=true` (`gradlew.bat` on Windows). This uses the isolated `build/ui-preview` game directory; create a disposable review world there instead of opening existing saves.

* **Ctrl+Alt+F8** toggles GUI bounds, clip bounds, hit bounds, and node status.
* **Ctrl+Alt+F9** requests a plain PNG, annotated PNG, and schema-version-1 JSON from the same rendered frame in `build/ui-inspection`. Files are never overwritten; JSON is written after both PNGs. Wait for the success path before using an export.
* Locate named controls within their `screenInstance`; use `layoutRevision` and `frameId` to reject stale coordinates. Anonymous widget/row IDs are not stable across rebuilding. `duplicate_id` or `invalid_bounds` issues disqualify automatic location; hidden, clipped, or input-blocked nodes provide no suggested click.
* GUI rendering uses `guiScale`, while mouse positions use the GUI/window-size ratio. `scissorPixels` uses OpenGL's bottom-left origin and Minecraft's integer truncation. `pixelMappingValid=false` means only GUI geometry is available; desktop/DPI coordinates require separate calibration.
* JSON omits entered text, names, item contents, NBT, and server addresses. PNGs can contain visible private information. Exports stay local; inspect them before sharing.

This is an observation tool, not an input dispatcher or proof of unknown third-party rendering. Normal `runClient` and production installations leave it disabled, including production launches with the inspector system property set.

Before further screen migrations, verify F8/F9 in an actual container, move an item through an observed slot, and repeat the export after resize/GUI-scale changes. Compilation and server GameTests do not validate screenshots or client input.

### **Singularities:**
Datapack definitions live at `data/<namespace>/singularities/<path>.json`. The JSON `name` must equal `<namespace>:<path>`, and both `count` and `timeCost` must be greater than zero.
```json
{
  "name": "example:iron",
  "displayName": "singularity.example.iron",
  "overlayColor": 14803425,
  "underlayColor": 7105644,
  "count": 1000,
  "timeCost": 240,
  "ingredient": { "item": "minecraft:iron_ingot" },
  "enabled": true,
  "recipeEnabled": true
}
```
The 1.20 aliases `timeRequired`, `recipeDisabled`, and hexadecimal string colors are migrated while loading. To preserve old data, the stored `recipeDisabled` boolean keeps its historical `recipeEnabled` meaning and is not inverted. Datapack and script changes take effect on `/reload`.

### **CraftTweaker:**
```
mods.avaritia.Compressor.addRecipe("name", input, output, inputCount, timeCost);
mods.avaritia.Compressor.remove(output);
mods.avaritia.CraftingTable.addShaped("name", tier, output, ingredients);
mods.avaritia.CraftingTable.addShapeless("name", tier, output, ingredients);
mods.avaritia.CraftingTable.remove(output);

mods.avaritia.Singularity.register("key", "displayName", overlayColor, underlayColor, count, timeCost, ingredient, enabled, recipeEnabled);
mods.avaritia.Singularity.remove("key");
mods.avaritia.Singularity.removeAll();
mods.avaritia.Singularity.removeRecipe("key");
mods.avaritia.Singularity.removeAllRecipe();
```
`mods.avaritia.Singularity.register` is a static call. Invalid IDs and non-positive `count` or `timeCost` values are logged and only that definition is skipped; unrelated script errors are still reported by the script engine. Singularity operations are staged until the current resource reload finishes its script phase and then committed once. Conflicts resolve as datapack < Java API < CraftTweaker < KubeJS, while later operations from the same source win. Script changes take effect only after `/reload`.

### **KubeJs:**
```javascript
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
        //infinity catalyst
        avaritia.infinity_catalyst(
            [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment",
            ]
        );
        console.log('Hello! The avaritia recipe event has fired!')
    }
)
AvaritiaEvents.singularity(event => {
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
```
### **InfinityCatalyst:**
```json5
{
  "type": "avaritia:infinity_catalyst",//Infinity Catalyst recipe type
  "category": "misc",
  "ingredients": [
    {
      "item": "minecraft:emerald_block"
    },
    {
      "item": "avaritia:crystal_matrix_ingot"
    },
    {
      "item": "avaritia:neutron_ingot"
    },
    {
      "item": "avaritia:cosmic_meatballs"
    },
    {
      "item": "avaritia:ultimate_stew"
    },
    {
      "item": "avaritia:endest_pearl"
    },
    {
      "item": "avaritia:record_fragment"
    }
  ]
}
```
### **Dependencies:**
avaritia_version see this [here](https://maven.nova-committee.cn/s3/committee/nova/mods/avaritia-forge/)
```groovy
repositories {
    maven {
        url "https://maven.nova-committee.cn/s3"
    }
}

dependencies {
    implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}")
}
```



