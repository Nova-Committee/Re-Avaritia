# NeoForge 26.3 scripting

## KubeJS

This branch ships the KubeJS bridge. Its compile dependency `26.1.2-8.0.1+neoforge` fails to start on Minecraft 26.3. Run these script examples only with a compatible 26.3 build of KubeJS and Rhino.

Put a `.js` file in `kubejs/server_scripts/`. `AvaritiaEvents.singularity` registers or changes singularity definitions. `ServerEvents.recipes` works on ordinary recipes through `event.recipes.avaritia`. Install the optional KubeJS build and dependencies compatible with the target environment. [AvaritiaEvents.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/event/AvaritiaEvents.java#L9) · [AvaritiaKubeJSPlugin.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/AvaritiaKubeJSPlugin.java#L44)

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
    avaritia.shapeless_table(Item.of("minecraft:blue_dye", 2), 2, ['minecraft:lapis_lazuli', 'minecraft:bone_meal'])
        .id('example:script_shapeless')
})
```

`setColors` takes integer overlay and underlay RGB values in that order. `removeRecipe`, `remove`, `removeAllRecipe`, and `removeAll` on the singularity event affect definitions or derived recipes as described in the [data reference](datapack.md#reload-and-precedence). Set compressor `timeCost` and `inputCount` explicitly for a scripted recipe. [SingularityRegisterEventJS.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/event/SingularityRegisterEventJS.java#L92) · [CompressRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/schema/CompressRecipeSchema.java#L15) · [ShapedTableRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/schema/ShapedTableRecipeSchema.java#L29) · [ShapelessTableRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/schema/ShapelessTableRecipeSchema.java#L22)

`infinity_catalyst(group, ingredients[, count])` and `eternal_singularity(ingredients[, count])` use fixed Re:Avaritia outputs. An explicit eternal recipe group can also be passed as `eternal_singularity(group, ingredients, count)`. [InfinityCatalystRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/schema/InfinityCatalystRecipeSchema.java#L23) · [EternalSingularityRecipeSchema.java](../../../src/main/java/committee/nova/mods/avaritia/compat/kubejs/schema/EternalSingularityRecipeSchema.java#L60)

Reload the server resources after editing and check both the script log and the resulting recipe ID. When testing two script engines together, register the same singularity ID with different counts to see which definition is used. [Reload and precedence](datapack.md#reload-and-precedence)
