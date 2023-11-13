<p align="center">
    <img width="200" src="web/avaritia.png" alt="title">  
</p>
<hr>
<p align="center">Re:Avaritia is a Minecraft mod made for Minecraft Forge</p>
<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://img.shields.io/badge/Available%20for-MC%201.20.1-c70039" alt="Supported Versions">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/re-avaritia">
        <img src="https://cf.way2muchnoise.eu/avaritia-reforged.svg" alt="CurseForge Download">
    </a>
    <img src="https://img.shields.io/badge/license-MIT%2FCC%20BY--NC--SA%204.0-green" alt="License">
</p>

<p align="center">
    <a href="README.md">English</a> | 
    <a href="README_CN.md">简体中文</a>
</p>





## **📕介绍:**
* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)的重铸版
* 此模组是非官方并含有Bugs!

## **✏️作者:**

- 程序: `cnlimiter` `Asek3` `MikhailTapio`

## **🔒许可:**

- 代码: [MIT](https://www.mit.edu/~amini/LICENSE.md)
- 材质: [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

## **📌其他下载:**
* [Avaritia (1.1x)](https://www.curseforge.com/minecraft/mc-mods/avaritia-1-10)
* [Avaritia (official)](https://www.curseforge.com/minecraft/mc-mods/avaritia)
* [AvaritiaLite](https://www.curseforge.com/minecraft/mc-mods/avaritia-lite)

## **❗Attention:**
* 你**可以**将本模组添加到你制作的整合包.
* JEI查看无尽工作台和中子态素压缩机配方.
* 可以利用json自定义奇点!
* 使用CraftTweaker修改无尽工作台和中子态素压缩机配方!
* 使用KubeJs修改无尽工作台和中子态素压缩机配方!


## **🔎开发:**

### **Singularities**
    位于config/avaritia/singularities文件夹下，可以利用json自定义奇点：
```json5
{
  "name": "singularity.avaritia.bronze", // 在语言文件中本地化的名称。
  "colors": [
    "d99f43",   //覆盖色。
    "bb6b3b"  //背景色。
  ],
  "materialCount": 1000, //默认是1000个合成一个对应奇点。
  "timeRequired": 240,  //在中子态素压缩机中产生对应奇点所需tick。
  "conditions": [    //启用条件。
    {
      "type": "forge:not",
      "value": {
        "tag": "forge:ingots/bronze",
        "type": "forge:tag_empty"
      }
    }
  ],
  "ingredient": {  //输入中子态素压缩机的物品或tag。
    "tag": "forge:ingots/bronze"
  },
  "enable": true //是否启用。
}
```

### **CraftTweaker:**
```
mods.avaritia.CompressionCrafting.addRecipe("name",input, inputCount, timeRequierd);//添加中子态素压缩配方。
mods.avaritia.CompressionCrafting.remove(output);//移除中子态素压缩配方。
mods.avaritia.ExtremeTableCrafting.addShaped("name",output, ingredients);//添加无尽工作台有序配方。
mods.avaritia.ExtremeTableCrafting.addShapeless("name",output, ingredients);//添加无尽工作台无序配方。
mods.avaritia.ExtremeTableCrafting.remove(output);//删除无尽工作台配方。
```

### **KubeJs:**
```javascript
//无尽工作台
event.custom({
    type: 'avaritia:shaped_extreme_craft',//无尽工作台有序配方，无序为avaritia:shapeless_extreme_craft。
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
//中子态素压缩机
event.custom({
    type: 'avaritia:compressor',//中子态素压缩机配方。
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
    由于自定义奇点的存在，无尽催化剂的配方是根据加载的奇点动态变化的，你可以自定义添加除奇点以外的物品，且此配方类型只能产出无尽催化剂，更改result无法更改产品。
```json5
{
  "type": "avaritia:infinity_catalyst_craft",//无尽催化剂的配方类型。
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
    "item": "avaritia:infinity_catalyst"//固定的产品。
  }
}
```



