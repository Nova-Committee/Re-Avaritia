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

        // 更改无尽催化剂的配方
        // 由于自定义奇点的存在，无尽催化剂的配方是根据加载的奇点动态变化的，你可以自定义添加除奇点以外的物品，且此配方类型只能产出无尽催化剂，更改 result 无法更改產出物。
        avaritia.infinity_catalyst(
            'default',
            [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment"
            ]
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
            true
        );
        console.log('Hello! The avaritia recipe event has fired!')
    }
)