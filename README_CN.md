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
* 可以利用CraftTweaker和KubeJs自定义并修改奇点!
* 使用CraftTweaker修改无尽工作台和中子态素压缩机配方!
* 使用KubeJs修改无尽工作台和中子态素压缩机配方!

## **🔎文档:**
* [Wiki](wiki)

## **⚙️开发:**
### **Singularities**
* [奇点Wiki](wiki/KUBEJS_SINGULARITY_GUIDE_CN.md)

### **CraftTweaker:**
```
mods.avaritia.CraftingTable.addShaped("name", tier, output, ingredients);//添加无尽工作台有序配方。
mods.avaritia.CraftingTable.addShapeless("name", tier, output, ingredients);//添加无尽工作台无序配方。
mods.avaritia.CraftingTable.remove(output);//删除无尽工作台配方。

mods.avaritia.Compressor.addRecipe("name", input, output, inputCount, timeCost);//添加中子态素压缩配方。
mods.avaritia.Compressor.remove(output);//移除中子态素压缩配方。

mods.avaritia.Singularity.register("key", "displayName", overlayColor, underlayColor, count, timeCost, ingredient, enabled, recipeEnable);//添加奇点
mods.avaritia.Singularity.remove("key");//删除指定奇点
mods.avaritia.Singularity.removeAll();//删除所有奇点
mods.avaritia.Singularity.removeRecipe("key");//删除指定奇点配方
mods.avaritia.Singularity.removeAllRecipe();//删除所有奇点配方

mods.avaritia.CraftingTable.addCatalyst("name", ingredients, catalystCount)//添加无尽催化剂配方
mods.avaritia.CraftingTable.addEternal("name", ingredients)//添加永恒奇点配方
```

### **KubeJs:**
```javascript
AvaritiaEvents.singularity(event => {
    event.removeRecipe("avaritia:coal")//删除指定奇点配方
    event.remove("avaritia:coal")//删除指定奇点
    event.removeAllRecipe()//删除所有奇点配方
    event.removeAll()//删除所有奇点
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
        //需要先删除对应奇点配方
        avaritia.compressor(Item.of("minecraft:coal"), Item.of("avaritia:singularity", '{Id:"avaritia:coal"}'))
            .inputCount(10000)
            .timeCost(100)
        ;
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
            2//自定义无尽催化剂数量
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
avaritia_version 请查看这里 [here](https://maven.nova-committee.cn/s3/committee/nova/mods/avaritia-forge/)
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



