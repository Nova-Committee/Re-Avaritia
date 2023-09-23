<p align="center">
    <img class="circle" src="https://s2.loli.net/2022/05/22/VcARrbQdT5tjJiM.png" alt="title">  
</p>
<hr>
<p align="center">Avaritia Reforged is a Minecraft mod made for Minecraft Forge</p>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/avaritia-reforged"><img src="https://img.shields.io/badge/Available%20for-MC%201.20.1-c70039" alt="Supported Versions"></a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/avaritia-reforged">
        <img src="http://cf.way2muchnoise.eu/avaritia-reforged.svg" alt="CurseForge Download">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="#">English</a> | 
    <a href="#">简体中文</a>
</p>

<style type="text/css">
.circle{
    width: 250px;
    border-radius: 10%;
    border: 5px solid rosybrown;
}
</style>




## **📕Introduction:**
* <span style="color: #ff0000;">This mod adds all from Avaritia.</span>
* This mod is <span style="color: #ff6600;">unofficial</span> and have bugs!

## ✏️Authors

- Programmer: `cnlimiter` `Asek3` `MikhailTapio`

##  🔒License

- Code: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- Assets: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

## **📌Download official:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* You&nbsp;<span style="color: #00ff00;"><strong>DEFINITELY CAN</strong></span>&nbsp;add the mod to your modpack.</li>
* Recipe viewing is supported via JEI.&nbsp;
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
//ExtremeCraft
event.custom({
    type: 'avaritia:shaped_extreme_craft',//Shaped Extreme Craft,Shapeless is avaritia:shapeless_extreme_craft
    pattern: [
        "       II",
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
            Ingredient.of('avaritia:crystal_matrix_ingot').toJson()
        ],
        I: [
            Ingredient.of('avaritia:infinity_ingot').toJson()
        ],
        N: [
            Ingredient.of('avaritia:neutron_ingot').toJson()
        ],
        X: [
            Ingredient.of('avaritia:infinity_catalyst').toJson()
        ]
    },
    result: [
        Ingredient.of('avaritia:infinity_sword').toJson()
    ]
})
//Compressor
event.custom({
    type: 'avaritia:compressor',//Compressor Recipe
    materialCount: 1000,
    timeRequired: 240,
    ingredients: [
        Ingredient.of('#forge:ingots/bronze').toJson()
    ],
    result: [
        Ingredient.of('avaritia:bronze_singularity').toJson()
    ]
})
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



