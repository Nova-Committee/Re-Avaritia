ServerEvents.recipes(

    event => {
        //custom版本
        event.custom({
            type: 'avaritia:shaped_table',//shapeless is avaritia:shapeless_table
            tier: 4,
            pattern: [
                "       IA",
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
                A: [
                    Item.of('minecraft:enchanted_book').enchant('minecraft:silk_touch', 1).strongNBT()
                ],
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
            ingredient: { tag: 'forge:ingots/copper' },
            result: { item: 'avaritia:singularity', count: 2 , nbt: {Id: 'avaritia:copper'}},
            inputCount: 2000,
            timeCost: 300
        })
        event.custom({
            type: 'avaritia:infinity_catalyst',
            group: 'default',
            ingredients: [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment",
            ]
        })
        event.custom({
            ingredients: [
                "minecraft:emerald_block",
                "avaritia:crystal_matrix_ingot",
                "avaritia:neutron_ingot",
                "avaritia:cosmic_meatballs",
                "avaritia:ultimate_stew",
                "avaritia:endest_pearl",
                "avaritia:record_fragment"
            ],
            custom: true//完全自定义
        })
        console.log('Hello! The avaritia recipe event has fired!')
    }
)