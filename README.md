<p align="center">
    <img width="200" src="web/avaritia.png" alt="title">  
</p>
<hr>
<p align="center">Avaritia Reforged is a Minecraft mod made for Minecraft Forge</p>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Available%20for-MC%201.20.1-c70039" alt="Supported Versions">
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


## **🔎Develop:**
### **CraftTweaker:**
```
mods.avaritia.CompressionCrafting.addRecipe("name",input, inputCount, timeRequierd);
mods.avaritia.CompressionCrafting.remove(output);
mods.avaritia.ExtremeTableCrafting.addShaped("name",output, ingredients);
mods.avaritia.ExtremeTableCrafting.addShapeless("name",output, ingredients);
mods.avaritia.ExtremeTableCrafting.remove(output);
```

### **KubeJs:**
```javascript
ServerEvents.recipes(
    event => {
        event.custom({
            type: 'avaritia:shaped_extreme_craft',//shapeless is avaritia:shapeless_extreme_craft。
            pattern: [
                "       I ",
                "      III",
                "     III ",
                "    III  ",
                " C III   ",
                "  CII    ",
                "  NC     ",
                " N  C    ",
                "X        "
            ],
            key: {
                C: [
                    {item: 'avaritia:crystal_matrix_ingot'}
                ],
                I: [
                    {item: 'avaritia:infinity_ingot'}
                ],
                N: [
                    {item: 'avaritia:neutron_ingot'}
                ],
                X: [
                    {item: 'avaritia:infinity_catalyst'}
                ]
            },
            result: {item: 'avaritia:infinity_sword'}
        })
        event.custom({
            type: 'avaritia:compressor',
            inputCount: 2000,
            timeCost: 240,
            ingredient: {tag: 'forge:ingots/copper'},
            result: { item: 'avaritia:singularity', count: 1 , nbt: {Id: 'avaritia:copper'}}
        })
        console.log('Hello! The avaritia recipe event has fired!')
    }
)
```
### **InfinityCatalyst:**
```json5
{
  "type": "avaritia:infinity_catalyst_craft",//Infinity Catalyst recipe type
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
  ],
  "result": {
    "item": "avaritia:infinity_catalyst"//Fixed products
  }
}
```



