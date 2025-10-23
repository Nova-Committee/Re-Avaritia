<p align="center">
    <img width="690" src="web/logo.png" alt="title">  
</p>
<hr>
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
    <a href="README.md">English</a> | 
    <a href="README_CN.md">简体中文</a>
</p>





## **📕介绍:**
* 此模组是[无尽贪婪](https://www.mcmod.cn/class/505.html)的重铸版
* 此模组是非官方版本!

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

## **🔎文档:**
* [Wiki](wiki)

## **⚙️开发:**
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
mods.avaritia.Compressor.addRecipe("name", input, output, inputCount, timeCost);//添加中子态素压缩配方。
mods.avaritia.Compressor.remove(output);//移除中子态素压缩配方。
mods.avaritia.CraftingTable.addShaped("name", tier, output, ingredients);//添加无尽工作台有序配方。
mods.avaritia.CraftingTable.addShapeless("name", tier, output, ingredients);//添加无尽工作台无序配方。
mods.avaritia.CraftingTable.remove(output);//删除无尽工作台配方。
```

### **KubeJs:**
```javascript
ServerEvents.recipes(
    event => {
        const { avaritia } = event.recipes;
        avaritia.shaped_table(
            // 无序配方是 avaritia.shapeless_table
            4,//工作台等级
            "avaritia:infinity_sword",//产品
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
            }//输入
        );
        //compressor
        avaritia
            .compressor("#forge:ingots/copper", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
            .timeCost(240)//所需时间
            .inputCount(2000);//所需数量
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
```groovy
plugins {
    id 'org.spongepowered.mixin' version '0.7.+'
}

repositories {
    maven {
        url "https://www.cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
    maven {
        url = "https://repo.spongepowered.org/repository/maven-public"
    }
}

dependencies {

    implementation fg.deobf("curse.maven:re-avaritia-623969:${avaritia_version}")
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:[0.4.1,0.5.0)"))
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:[0.4.1,0.5.0)"))
}

```



