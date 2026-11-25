# Forge 1.20.1 scripting

## CraftTweaker

Save a `.zs` file in `scripts/`. Use the static class names below. On Forge, the three-argument `addEnternal` overload is an instance method on `<recipetype:avaritia:crafting_table_recipe>`. For recipe names use unique paths without a namespace prefix, such as `example/compress`, which CraftTweaker registers under `crafttweaker`. The singularity ID is a full identifier. [ModRecipeTypes.java](../../../src/main/java/committee/nova/mods/avaritia/init/registry/ModRecipeTypes.java#L27) · [CraftingTable.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/crafttweaker/CraftingTable.java#L36) · [CompressorCrafting.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/crafttweaker/CompressorCrafting.java#L30)

```zenscript
mods.avaritia.Singularity.register("example:echo_shard", "Echo Shard Singularity", 0x496ACE, 0x24396A,
    8, 40, <item:minecraft:echo_shard>, true, true);
mods.avaritia.Compressor.addRecipe("example/compress", <item:minecraft:clay_ball>, <item:minecraft:brick>, 4, 40);
mods.avaritia.CraftingTable.addShaped("example/table", 2, <item:minecraft:flint>, [[<item:minecraft:gravel>, <item:minecraft:sand>]]);
```

`addShapeless(name, tier, output, inputs)` takes a flat ingredient array. The shaped method takes rows. Use `addCatalyst(name, inputs, count)` for the catalyst and `mods.avaritia.CraftingTable.addEnternal(name, inputs)` for one eternal singularity. The count overload is `<recipetype:avaritia:crafting_table_recipe>.addEnternal(name, inputs, count)`. `remove(output)` removes matching outputs via the corresponding manager or class. Use `mods.avaritia.Singularity.removeRecipe(id)` or `mods.avaritia.Singularity.remove(id)` for the distinct derived-recipe and definition operations. [CraftingTable.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/crafttweaker/CraftingTable.java#L106) · [SingularityCrafting.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/crafttweaker/SingularityCrafting.java#L19)

## KubeJS

Put a `.js` file in `kubejs/server_scripts/`. `AvaritiaEvents.singularity` registers or changes singularity definitions. `ServerEvents.recipes` works on ordinary recipes through `event.recipes.avaritia`. Install the optional KubeJS build and dependencies compatible with the target environment. [AvaritiaEvents.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/event/AvaritiaEvents.java#L12) · [KubeJSAvaritiaPlugin.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/KubeJSAvaritiaPlugin.java#L29)

```javascript
AvaritiaEvents.singularity(event => {
    event.register('example:phantom_membrane', singularity => singularity
        .setDisplayName('Phantom Membrane Singularity')
        .setColors(0x496ACE, 0x24396A)
        .setCount(8)
        .setTimeCost(40)
        .setIngredient(Ingredient.of('minecraft:phantom_membrane'))
        .setEnabled(true)
        .setRecipeEnabled(true))
})

ServerEvents.recipes(event => {
    const avaritia = event.recipes.avaritia
    avaritia.compressor('minecraft:kelp', 'minecraft:dried_kelp').inputCount(4).timeCost(40)
        .id('example:script_compress')
    avaritia.shaped_table(2, 'minecraft:flint', ['GS'], { G: 'minecraft:gravel', S: 'minecraft:sand' })
        .id('example:script_shaped')
    avaritia.shapeless_table(2, Item.of("minecraft:blue_dye", 2), ['minecraft:lapis_lazuli', 'minecraft:bone_meal'])
        .id('example:script_shapeless')
})
```

`setColors` takes integer overlay and underlay RGB values in that order. `removeRecipe`, `remove`, `removeAllRecipe`, and `removeAll` on the singularity event affect definitions or derived recipes as described in the [data reference](datapack.md#reload-and-precedence). Set compressor `timeCost` and `inputCount` explicitly for a scripted recipe. [SingularityRegisterEventJS.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/event/SingularityRegisterEventJS.java#L59) · [CompressRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/CompressRecipeSchema.java#L21) · [ShapedTableRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/ShapedTableRecipeSchema.java#L18) · [ShapelessTableRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/ShapelessTableRecipeSchema.java#L19)

`infinity_catalyst(group, ingredients[, count])` and `eternal_singularity(ingredients[, count])` use fixed Re:Avaritia outputs. [InfinityCatalystRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/InfinityCatalystRecipeSchema.java#L22) · [EternalSingularityRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/init/compat/kubejs/EternalSingularityRecipeSchema.java#L20)

Reload the server resources after editing and check both the script log and the resulting recipe ID. When testing two script engines together, register the same singularity ID with different counts to see which definition is used. [Reload and precedence](datapack.md#reload-and-precedence)
