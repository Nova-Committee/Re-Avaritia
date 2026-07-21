AvaritiaEvents.singularity(
    event => {
        // 修改脚本后执行 /reload；本事件会在内部压缩机配方生成前运行。
        event.removeRecipe("avaritia:coal")//remove recipe
        event.remove("avaritia:iron")//remove singularity
        event.register("avaritia:dragon_egg", s => {
            s.setDisplayName("singularity.avaritia.dragon_egg")
                .setColors(0x7a7b78, 0x7a7b78)
                .setCount(1)
                .setTimeCost(100)
                .setIngredient(Ingredient.of("minecraft:dragon_egg"))
        })
    }
)

ServerEvents.recipes(

    event => {
        const { avaritia } = event.recipes;
        // 无尽工作台
        avaritia.shaped_table(
            // 无序配方是 avaritia.shapeless_table
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

        // 中子态素压缩机
        avaritia.compressor("#forge:ingots/copper", Item.of("avaritia:singularity", '{Id:"avaritia:copper"}'))
            .inputCount(2000)
            .timeCost(240)
        ;
        //需要先删除奇点配方
        avaritia.compressor(Item.of("minecraft:coal"), Item.of("avaritia:singularity", '{Id:"avaritia:coal"}'))
            .inputCount(10000)
            .timeCost(100)
        ;

        // 更改无尽催化剂的配方
        // 由于自定义奇点的存在，无尽催化剂的配方是根据加载的奇点动态变化的，你可以自定义添加除奇点以外的物品，且此配方类型只能产出无尽催化剂，更改 result 无法更改產出物。
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
            ],//自定义输入
            2//自定义产物数量
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
            ],//自定义输入
        );
        console.log('Hello! The avaritia recipe event has fired!')
    }
)
