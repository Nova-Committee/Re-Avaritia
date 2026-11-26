# Fabric 26.3 customization reference

This branch targets Minecraft `26.3`, Java `25`, and Fabric `0.19.5`. Fabric API is required. This branch builds against `0.162.0+26.3`.

Use a datapack or the Java API. The release JAR excludes the CraftTweaker and KubeJS integration classes. [Source-set exclusions](../../../build.gradle#L72)

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

Use `data/<namespace>/singularities/<path>.json`. `name` is required and must equal `<namespace>:<path>`. Paths starting with `_` are skipped. Use an existing translation key or a literal label for `displayName`, which defaults to the singularity ID. Colors are required six-digit hexadecimal RGB strings, without `#`. [Definition loading](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L52) · [SingularityUtils.java](../../../src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L36)

`count` defaults to `1000` when absent. Accepted values are a positive integer. `timeCost` is a positive tick count and defaults to the server’s configured singularity processing time, initially `240` ticks. `ingredient` accepts an optional native item ID, `#tag` or array of alternatives. The loader also normalizes legacy `item` and `tag` objects. `enabled` and `recipeEnabled` default to `true`. A disabled definition or recipe is not eligible for a generated compressor recipe. An eligible definition also needs a usable ingredient and satisfied conditions. [Singularity.java](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/Singularity.java#L187) · [SingularityUtils.java](../../../src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L36) · [SingularityServerResourcesMixin.java](../../../src/main/java/committee/nova/mods/avaritia/init/mixins/SingularityServerResourcesMixin.java#L28) · [SingularityScriptTransactionFinalizer.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L60)

`data/example/singularities/chorus_fruit.json`

```json
{
  "name": "example:chorus_fruit",
  "displayName": "Chorus Fruit Singularity",
  "overlayColor": "e1e1e1",
  "underlayColor": "6c6c6c",
  "count": 8,
  "timeCost": 40,
  "ingredient": "minecraft:chorus_fruit",
  "enabled": true,
  "recipeEnabled": true
}
```

## Compressor recipes

Put recipes under `data/<namespace>/recipe/<path>.json`. Use serializer `avaritia:compressor`, with `ingredient`, a `result` object with `id`, and a positive `timeCost` in ticks. `inputCount` defaults to `1000`. Set it to a positive count appropriate for the machine. For a singularity output, follow an existing branch recipe or let a singularity definition generate the output stack with its ID attached. [CompressorRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/CompressorRecipe.java#L127) · [Singularity stack](../../../src/main/java/committee/nova/mods/avaritia/util/SingularityUtils.java#L137)

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

Use `avaritia:shaped_table` with `pattern`, `key`, `result`, and `tier`. Use `avaritia:shapeless_table` with a flat `ingredients` array, `result`, and `tier`. A space is an empty shaped cell. Keep patterns rectangular and within the table size. Tiers `1`–`4` correspond to the Sculk 3×3, Nether 5×5, End 7×7, and Extreme 9×9 tables. Write an explicit tier for a fixed requirement. For `tier: 0` or an omitted tier, the recipe can match any table with enough space. The reported tier is inferred from the pattern dimensions or the number of shapeless inputs. [ShapedTableCraftingRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapedTableCraftingRecipe.java#L245) · [ShapelessTableCraftingRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/ShapelessTableCraftingRecipe.java#L204)

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

The `avaritia:infinity_catalyst` recipe crafts the fixed catalyst output. With `group: "default"`, the recipe adds eligible singularities to the explicit ingredients. Choose another group for an explicit list. `count` is the crafted output quantity. [InfinityCatalystCraftRecipe.java](../../../src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/InfinityCatalystCraftRecipe.java#L50)

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

The effective layers in the shipped build are datapack → Java API. Later Java API operations take precedence. This order governs the resolved singularity snapshot. Ordinary recipe overrides follow the datapack and script engine's recipe operations for their respective IDs. [Snapshot merge](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L221)

The derived recipe uses `<namespace>:<path>_singularity`. `example:chorus_fruit` generates `example:chorus_fruit_singularity`. The resolved count can be scaled by the server’s ProjectE count boost. The finalizer regenerates eligible singularity recipes and installs them into the server RecipeManager. Use `recipeEnabled: false` and a different ID for a custom replacement. Use an ingredient without another enabled compressor recipe to avoid competing matches. [SingularityScriptTransactionFinalizer.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L76) · [SingularityServerResourcesMixin.java](../../../src/main/java/committee/nova/mods/avaritia/init/mixins/SingularityServerResourcesMixin.java#L28) · [SingularityScriptTransactionFinalizer.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L60)

Apply edits with `/reload`. When the branch provides a scripting integration, `removeRecipe(id)` suppresses the derived recipe while retaining the definition, and `remove(id)` removes both. Their `removeAllRecipe()` and `removeAll()` variants apply to the full set. Check the resolved ID and machine behavior after reload. For the examples above, verify `example:chorus_fruit_singularity`, `example:compress_rabbit_hide`, `example:shaped_brick`, and `example:shapeless_cookie`. [Snapshot merge](../../../src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java#L221) · [SingularityServerResourcesMixin.java](../../../src/main/java/committee/nova/mods/avaritia/init/mixins/SingularityServerResourcesMixin.java#L28) · [SingularityScriptTransactionFinalizer.java](../../../src/main/java/committee/nova/mods/avaritia/init/handler/SingularityScriptTransactionFinalizer.java#L60)
