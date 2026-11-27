# Forge 1.20.1 customization reference

This branch targets Minecraft `1.20.1`, Java `17`, and Forge `47.4.20`.

The branch declares optional integrations for CraftTweaker `14.0.12`, KubeJS `2001.6.5-build.20`. Use loader-compatible builds and their own dependencies. [Script reference](scripts.md)

## Data pack

Put `pack.mcmeta` and `data/` next to each other at the root of a folder or ZIP under the world's `datapacks/` directory.

`pack.mcmeta`

```json
{
  "pack": {
    "description": "Re:Avaritia customization",
    "pack_format": 15
  }
}
```

## Singularities

Use `data/<namespace>/singularities/<path>.json`. `name` is required and must equal `<namespace>:<path>`. Paths starting with `_` are skipped. Use an existing translation key or a literal label for `displayName`, which defaults to the singularity ID. Colors are required six-digit hexadecimal RGB strings, without `#`. [Definition loading](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L55) · [SingularityUtils.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L31)

`count` defaults to `1000` when absent. Accepted values are a positive integer. `timeCost` is a positive tick count and defaults to the server’s configured singularity processing time, initially `240` ticks. `ingredient` accepts an optional object such as `{"item":"minecraft:iron_ingot"}` or `{"tag":"minecraft:logs"}`. `enabled` and `recipeEnabled` default to `true`. A disabled definition or recipe is not eligible for a generated compressor recipe. An eligible definition also needs a usable ingredient and satisfied conditions. [Singularity.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L154) · [SingularityUtils.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L31) · [SingularityScriptTransactionFinalizer.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L36) · [InternalRecipeHandler.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L28)

`data/example/singularities/chorus_fruit.json`

```json
{
  "name": "example:chorus_fruit",
  "displayName": "Chorus Fruit Singularity",
  "overlayColor": "e1e1e1",
  "underlayColor": "6c6c6c",
  "count": 8,
  "timeCost": 40,
  "ingredient": {
    "item": "minecraft:chorus_fruit"
  },
  "enabled": true,
  "recipeEnabled": true
}
```

## Compressor recipes

Put recipes under `data/<namespace>/recipes/<path>.json`. Use serializer `avaritia:compressor`, with `ingredient`, a `result` object with `item`, and a positive `timeCost` in ticks. `inputCount` defaults to `1000`. Set it to a positive count appropriate for the machine. For a singularity output, follow an existing branch recipe or let a singularity definition generate the output stack with its ID attached. [CompressorRecipe.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/CompressorRecipe.java#L124) · [Singularity stack](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L90)

`data/example/recipes/compress_rabbit_hide.json`

```json
{
  "type": "avaritia:compressor",
  "ingredient": {
    "item": "minecraft:rabbit_hide"
  },
  "result": {
    "item": "minecraft:leather",
    "count": 1
  },
  "inputCount": 4,
  "timeCost": 40
}
```

## Tiered crafting tables

Use `avaritia:shaped_table` with `pattern`, `key`, `result`, and `tier`. Use `avaritia:shapeless_table` with a flat `ingredients` array, `result`, and `tier`. A space is an empty shaped cell. Keep patterns rectangular and within the table size. Tiers `1`–`4` correspond to the Sculk 3×3, Nether 5×5, End 7×7, and Extreme 9×9 tables. Write an explicit tier for a fixed requirement. For `tier: 0` or an omitted tier, the recipe can match any table with enough space. The reported tier is inferred from the pattern dimensions or the number of shapeless inputs. [ShapedTableCraftingRecipe.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapedTableCraftingRecipe.java#L250) · [ShapelessTableCraftingRecipe.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapelessTableCraftingRecipe.java#L177)

`data/example/recipes/shaped_brick.json`

```json
{
  "type": "avaritia:shaped_table",
  "tier": 2,
  "pattern": [
    "AB",
    "BA"
  ],
  "key": {
    "A": {
      "item": "minecraft:clay_ball"
    },
    "B": {
      "item": "minecraft:redstone"
    }
  },
  "result": {
    "item": "minecraft:brick",
    "count": 1
  }
}
```

`data/example/recipes/shapeless_cookie.json`

```json
{
  "type": "avaritia:shapeless_table",
  "tier": 1,
  "ingredients": [
    {
      "item": "minecraft:cocoa_beans"
    },
    {
      "item": "minecraft:sugar"
    }
  ],
  "result": {
    "item": "minecraft:cookie",
    "count": 1
  }
}
```

The `avaritia:infinity_catalyst` recipe crafts the fixed catalyst output. With `group: "default"`, the recipe adds eligible singularities to the explicit ingredients. Choose another group for an explicit list. `count` is the crafted output quantity. [InfinityCatalystCraftRecipe.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/InfinityCatalystCraftRecipe.java#L45)

`data/example/recipes/catalyst.json`

```json
{
  "type": "avaritia:infinity_catalyst",
  "group": "example",
  "ingredients": [
    {
      "item": "minecraft:emerald_block"
    }
  ],
  "count": 2
}
```

## Reload and precedence

Effective singularity priority is datapack → Java API → CraftTweaker → KubeJS. Later operations within one source take precedence. This order governs the resolved singularity snapshot. Ordinary recipe overrides follow the datapack and script engine's recipe operations for their respective IDs. [Snapshot merge](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L225)

The derived recipe uses `<namespace>:<path>_singularity`. `example:chorus_fruit` generates `example:chorus_fruit_singularity`. The resolved count can be scaled by the server’s ProjectE count boost. The reload transaction resolves script and datapack definitions before regenerating eligible compressor recipes. Use `recipeEnabled: false` and a different ID for a custom replacement. Use an ingredient without another enabled compressor recipe to avoid competing matches. [ModApi.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/ModApi.java#L59) · [SingularityScriptTransactionFinalizer.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L36) · [InternalRecipeHandler.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L28)

Apply edits with `/reload`. When the branch provides a scripting integration, `removeRecipe(id)` suppresses the derived recipe while retaining the definition, and `remove(id)` removes both. Their `removeAllRecipe()` and `removeAll()` variants apply to the full set. Check the resolved ID and machine behavior after reload. For the examples above, verify `example:chorus_fruit_singularity`, `example:compress_rabbit_hide`, `example:shaped_brick`, and `example:shapeless_cookie`. [Snapshot merge](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L225) · [SingularityScriptTransactionFinalizer.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L36) · [InternalRecipeHandler.java](../../../targets/forge/src/main/java/committee/nova/mods/avaritia/init/handler/InternalRecipeHandler.java#L28)
