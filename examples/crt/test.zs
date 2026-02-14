mods.avaritia.CraftingTable.addShaped("diamond_block", 4, <item:minecraft:diamond_block>,
[
    [],
    [],
    [],
    [<item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>],
    [<item:minecraft:coal_block>, <item:minecraft:diamond>, <item:minecraft:coal_block>],
    [<item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>],
    [],
    [],
    [],
]

);
mods.avaritia.CraftingTable.addShapeless("diamond_blocks", 4, <item:minecraft:diamond_block>,
[
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:diamond>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>
]
);

mods.avaritia.CraftingTable.addCatalyst("catalyst2",
[
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:diamond>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>
]
, 1);

mods.avaritia.CraftingTable.addEternal("eternal2",
[
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:diamond>, <item:minecraft:coal_block>,
    <item:minecraft:coal_block>, <item:minecraft:coal_block>, <item:minecraft:coal_block>
]);

mods.avaritia.Compressor.addRecipe("diamond_blocks3", <item:minecraft:coal_block>, <item:minecraft:diamond>, 2000, 240);

mods.avaritia.Singularity.register("avaritia:example1", "singularity.avaritia.example1", 0xC0C0C0, 0x808080, 1000, 200, <item:minecraft:iron_ingot>, true, true);//添加奇点

mods.avaritia.Singularity.remove("avaritia:iron");//删除指定奇点

