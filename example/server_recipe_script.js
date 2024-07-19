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