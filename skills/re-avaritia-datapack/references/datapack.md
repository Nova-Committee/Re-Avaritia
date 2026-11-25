# NeoForge 26.3 customization reference

This branch targets Minecraft `26.3`, Java `25`, and NeoForge `26.3.0.45-beta`.

This build includes the KubeJS bridge and has no CraftTweaker integration. The compile dependency is KubeJS `26.1.2-8.0.1+neoforge`. Running scripts requires KubeJS and Rhino releases compatible with Minecraft 26.3. [Script reference](scripts.md)

## Data pack

Put `pack.mcmeta` and `data/` next to each other at the root of a folder or ZIP under the world's `datapacks/` directory.

`pack.mcmeta`

```json
{
  "pack": {
    "description": "Re:Avaritia customization",
    "min_format": [
      121,
      0
    ],
    "max_format": [
      121,
      0
    ]
  }
}
```

## Singularities

Use `data/<namespace>/singularities/<path>.json`. `name` is required and must equal `<namespace>:<path>`. Paths starting with `_` are skipped. Use an existing translation key or a literal label for `displayName`, which defaults to an empty value. Colors are integer RGB values, default `0x3B2754` for each when omitted. [Definition loading](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L67) · [Singularity.java](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L30)

`count` defaults to `1000` when absent. Accepted values are a positive integer or `-1`, which uses a base count of `1000`. `timeCost` is a positive tick count and defaults to `240`. `ingredient` accepts an optional native item ID, `#tag` or array of alternatives. `enabled` and `recipeEnabled` default to `true`. A disabled definition or recipe is not eligible for a generated compressor recipe. An eligible definition also needs a usable ingredient and satisfied conditions. [Singularity.java](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L225) · [Singularity.java](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L30) · [RecipeManagerMixin.java](../../../src/main/java/committee/nova/mods/avaritia/mixin/RecipeManagerMixin.java#L59) · [InternalRecipeHandler.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L22)

`data/example/singularities/chorus_fruit.json`

```json
{
  "name": "example:chorus_fruit",
  "displayName": "Chorus Fruit Singularity",
  "overlayColor": 14803425,
  "underlayColor": 7105644,
  "count": -1,
  "timeCost": 40,
  "ingredient": "minecraft:chorus_fruit",
  "enabled": true,
  "recipeEnabled": true
}
```

## Compressor recipes

Put recipes under `data/<namespace>/recipe/<path>.json`. Use serializer `avaritia:compressor`, with `ingredient`, a `result` object with `id`, and a positive `timeCost` in ticks. `inputCount` defaults to `1000`. Set it to a positive count appropriate for the machine. For a singularity output, follow an existing branch recipe or let a singularity definition generate the output stack with its ID attached. [CompressorRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/CompressorRecipe.java#L125) · [Singularity stack](../../../src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L24)

`data/example/recipe/compress_rabbit_hide.json`

```json
{
  "type": "avaritia:compressor",
  "ingredient": "minecraft:rabbit_hide",
  "result": {
    "id": "minecraft:leather",
    "count": 1
  },
  "inputCount": 4,
  "timeCost": 40
}
```

## Tiered crafting tables

Use `avaritia:shaped_table` with `pattern`, `key`, `result`, and `tier`. Use `avaritia:shapeless_table` with a flat `ingredients` array, `result`, and `tier`. A space is an empty shaped cell. Keep patterns rectangular and within the table size. Tiers `1`–`4` correspond to the Sculk 3×3, Nether 5×5, End 7×7, and Extreme 9×9 tables. Write an explicit tier for a fixed requirement. For `tier: 0` or an omitted tier, the recipe can match any table with enough space. The reported tier is inferred from the pattern dimensions or the number of shapeless inputs. [ShapedTableCraftingRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapedTableCraftingRecipe.java#L217) · [ShapelessTableCraftingRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapelessTableCraftingRecipe.java#L164)

`data/example/recipe/shaped_brick.json`

```json
{
  "type": "avaritia:shaped_table",
  "tier": 2,
  "pattern": [
    "AB",
    "BA"
  ],
  "key": {
    "A": "minecraft:clay_ball",
    "B": "minecraft:redstone"
  },
  "result": {
    "id": "minecraft:brick",
    "count": 1
  }
}
```

`data/example/recipe/shapeless_cookie.json`

```json
{
  "type": "avaritia:shapeless_table",
  "tier": 1,
  "ingredients": [
    "minecraft:cocoa_beans",
    "minecraft:sugar"
  ],
  "result": {
    "id": "minecraft:cookie",
    "count": 1
  }
}
```

The `avaritia:infinity_catalyst` recipe crafts the fixed catalyst output. With `group: "default"`, the recipe adds eligible singularities to the explicit ingredients. Choose another group for an explicit list. `count` is the crafted output quantity. [InfinityCatalystCraftRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/InfinityCatalystCraftRecipe.java#L51)

`data/example/recipe/catalyst.json`

```json
{
  "type": "avaritia:infinity_catalyst",
  "group": "example",
  "ingredients": [
    "minecraft:emerald_block"
  ],
  "count": 2
}
```

## Reload and precedence

Effective singularity priority is datapack → Java API → KubeJS. There is no CraftTweaker integration. Later operations within one source take precedence. This order governs the resolved singularity snapshot. Ordinary recipe overrides follow the datapack and script engine's recipe operations for their respective IDs. [Snapshot merge](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L284)

The derived recipe uses `<namespace>:<path>_singularity`. `example:chorus_fruit` generates `example:chorus_fruit_singularity`. The resolved count can be scaled by the server’s ProjectE count boost. On this branch Equivox is also recognized. This branch also ships explicit JSON recipes for built-in singularities. Reload removes disabled derived IDs and fills missing eligible recipes. To change a built-in compressor recipe’s count or time, override its `avaritia:<path>_singularity` recipe JSON or replace that recipe through the supported script API as well. [Singularity.java](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L98) · [RecipeManagerMixin.java](../../../src/main/java/committee/nova/mods/avaritia/mixin/RecipeManagerMixin.java#L59) · [InternalRecipeHandler.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L22)

Apply edits with `/reload`. When the branch provides a scripting integration, `removeRecipe(id)` suppresses the derived recipe while retaining the definition, and `remove(id)` removes both. Their `removeAllRecipe()` and `removeAll()` variants apply to the full set. Check the resolved ID and machine behavior after reload. For the examples above, verify `example:chorus_fruit_singularity`, `example:compress_rabbit_hide`, `example:shaped_brick`, and `example:shapeless_cookie`. [Snapshot merge](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L284) · [RecipeManagerMixin.java](../../../src/main/java/committee/nova/mods/avaritia/mixin/RecipeManagerMixin.java#L59) · [InternalRecipeHandler.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L22)
