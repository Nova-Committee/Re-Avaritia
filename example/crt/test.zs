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
]
, true);

mods.avaritia.Compressor.addRecipe("diamond_blocks3", <item:minecraft:coal_block>, <item:minecraft:diamond>, 2000, 240);