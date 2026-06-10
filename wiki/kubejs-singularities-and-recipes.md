# KubeJS Custom Singularities and Recipes / KubeJS 自定义奇点与配方

Applies to Avaritia for Minecraft 26.1.2 with KubeJS `26.1.2-8.0.1+neoforge` and Rhino `2101.2.7-build.85+Rhino-1.21`.

This guide is bilingual. The Chinese section is first, followed by the English section with the same workflow.

Reference: KubeJS custom datapack recipes are added with `event.custom(...)`; see the official KubeJS recipe guide: <https://kubejs.com/wiki/tutorials/recipes>.

---

## 中文教程

### 1. 前置条件

在整合包或测试环境中安装：

- Avaritia
- KubeJS
- Rhino

脚本放在游戏目录下：

```text
kubejs/server_scripts/
```

自定义奇点和配方都建议放在 `server_scripts` 中。修改脚本后可执行 `/reload`，如果仍未生效，再重启客户端或服务端。

本项目中 KubeJS 是可选联动依赖。开发环境测试时可以把 KubeJS 和 Rhino 的 jar 放到 `run/mods`，但不要提交这些 jar；运行 `runData` 前也建议移除它们，避免 KubeJS 参与数据生成流程。

### 2. 自定义奇点

新建：

```text
kubejs/server_scripts/avaritia_singularities.js
```

示例：

```js
AvaritiaEvents.singularity(event => {
    event.register('avaritia:dragon_egg', singularity => {
        singularity
            .setDisplayName('singularity.avaritia.dragon_egg')
            .setColors(0x7A7B78, 0x4B2A7A)
            .setCount(1)
            .setTimeCost(100)
            .setIngredient(Ingredient.of('minecraft:dragon_egg'))
    })
})
```

字段说明：

| 方法 | 说明 |
| --- | --- |
| `event.register(id, callback)` | 注册一个运行时奇点。 |
| `setDisplayName(key)` | 设置翻译键，不是直接显示文本。 |
| `setColors(overlay, underlay)` | 设置奇点渲染颜色，使用 RGB 十六进制值。 |
| `setCount(count)` | 压缩机需要消耗的输入数量。 |
| `setTimeCost(ticks)` | 压缩机耗时，单位是 tick。 |
| `setIngredient(Ingredient.of(...))` | 设置压缩输入，可以是物品或标签。 |
| `setRecipeEnabled(false)` | 只注册奇点物品，不自动生成压缩机配方。 |
| `setEnabled(false)` | 禁用该奇点。 |

移除默认奇点的压缩机配方时，使用奇点事件里的 `removeRecipe`，传入的是奇点 id，不是配方 id：

```js
AvaritiaEvents.singularity(event => {
    // 移除铁奇点的默认压缩机配方 avaritia:iron_singularity，但保留铁奇点物品。
    event.removeRecipe('avaritia:iron')
})
```

如果要移除全部默认奇点的压缩机配方：

```js
AvaritiaEvents.singularity(event => {
    event.removeAllRecipe()
})
```

如果要连奇点本身也从创造栏、寰宇奇点材料列表等位置移除，才使用 `remove` 或 `removeAll`：

```js
AvaritiaEvents.singularity(event => {
    event.remove('avaritia:iron')
})
```

如果你希望这个奇点显示本地化名称，可以添加资源文件：

```text
kubejs/assets/avaritia/lang/zh_cn.json
kubejs/assets/avaritia/lang/en_us.json
```

示例：

```json
{
  "singularity.avaritia.dragon_egg": "龙蛋奇点"
}
```

```json
{
  "singularity.avaritia.dragon_egg": "Dragon Egg Singularity"
}
```

脚本中引用该奇点物品时，使用物品组件：

```js
Item.of('avaritia:singularity[avaritia:singularity_id="avaritia:dragon_egg"]')
```

### 3. 使用 Avaritia 配方简写

新建：

```text
kubejs/server_scripts/avaritia_recipes.js
```

示例：

```js
ServerEvents.recipes(event => {
    const { avaritia } = event.recipes

    avaritia.shaped_table(
        4,
        'avaritia:infinity_sword',
        [
            '       I ',
            '      III',
            '     III ',
            '    III  ',
            ' C III   ',
            '  CII    ',
            '  NC     ',
            ' N  C    ',
            'X        '
        ],
        {
            C: 'avaritia:crystal_matrix_ingot',
            I: 'avaritia:infinity_ingot',
            N: 'avaritia:neutron_ingot',
            X: 'avaritia:infinity_catalyst'
        }
    )

    avaritia.compressor(
        Ingredient.of('#c:ingots/copper'),
        Item.of('avaritia:singularity[avaritia:singularity_id="avaritia:copper"]')
    )
        .inputCount(2000)
        .timeCost(240)

    avaritia.infinity_catalyst('default', [
        'minecraft:emerald_block',
        'avaritia:crystal_matrix_ingot',
        'avaritia:neutron_ingot',
        'avaritia:cosmic_meatballs',
        'avaritia:ultimate_stew',
        'avaritia:endest_pearl',
        'avaritia:record_fragment'
    ])

    avaritia.eternal_singularity([
        'minecraft:emerald_block',
        'avaritia:crystal_matrix_ingot',
        'avaritia:neutron_ingot',
        'avaritia:cosmic_meatballs',
        'avaritia:ultimate_stew',
        'avaritia:endest_pearl',
        'avaritia:record_fragment'
    ], 1)
})
```

常用配方方法：

| 方法 | 用途 |
| --- | --- |
| `avaritia.shaped_table(tier, result, pattern, key)` | 无尽工作台有序配方。 |
| `avaritia.shapeless_table(result, ingredients)` | 无尽工作台无序配方，默认 tier 为 `0`。 |
| `avaritia.shapeless_table(result, tier, ingredients)` | 指定 tier 的无序配方。 |
| `avaritia.compressor(ingredient, result)` | 中子态素压缩机配方，可链式设置 `inputCount` 和 `timeCost`。 |
| `avaritia.infinity_catalyst(group, ingredients)` | 无尽催化剂配方。输出固定为无尽催化剂。 |
| `avaritia.eternal_singularity(ingredients, count)` | 寰宇奇点配方。输出固定为寰宇奇点。 |
| `avaritia.extreme_smithing(result, template, base, addition)` | 极限锻造配方。 |
| `avaritia.no_consume_catalyst_shaped(pattern, key, result, tier)` | 催化剂不消耗的有序配方。 |

### 4. 使用 `event.custom()` 编写原始 JSON 配方

当你想完全控制配方 JSON，或者从数据包迁移配方时，使用 `event.custom()`。

无尽工作台有序配方：

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:shaped_table',
        tier: 4,
        pattern: [
            '       I ',
            '      III',
            '     III ',
            '    III  ',
            ' C III   ',
            '  CII    ',
            '  NC     ',
            ' N  C    ',
            'X        '
        ],
        key: {
            C: [{ item: 'avaritia:crystal_matrix_ingot' }],
            I: [{ item: 'avaritia:infinity_ingot' }],
            N: [{ item: 'avaritia:neutron_ingot' }],
            X: [{ item: 'avaritia:infinity_catalyst' }]
        },
        result: { item: 'avaritia:infinity_sword' }
    })
})
```

压缩机配方：

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:compressor',
        ingredient: { tag: 'c:ingots/copper' },
        result: {
            item: 'avaritia:singularity[avaritia:singularity_id="avaritia:copper"]',
            count: 2
        },
        inputCount: 2000,
        timeCost: 300
    })
})
```

无尽催化剂配方：

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:infinity_catalyst',
        group: 'default',
        ingredients: [
            'minecraft:emerald_block',
            'avaritia:crystal_matrix_ingot',
            'avaritia:neutron_ingot',
            'avaritia:cosmic_meatballs',
            'avaritia:ultimate_stew',
            'avaritia:endest_pearl',
            'avaritia:record_fragment'
        ]
    })
})
```

寰宇奇点配方：

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:eternal_singularity',
        ingredients: [
            'minecraft:emerald_block',
            'avaritia:crystal_matrix_ingot',
            'avaritia:neutron_ingot',
            'avaritia:cosmic_meatballs',
            'avaritia:ultimate_stew',
            'avaritia:endest_pearl',
            'avaritia:record_fragment'
        ],
        custom: true
    })
})
```

注意：`event.custom()` 必须写 `type`。旧示例中最后一个寰宇奇点配方如果缺少 `type: 'avaritia:eternal_singularity'`，当前版本不会把它识别为 Avaritia 配方。

### 5. 迁移旧脚本时的注意事项

1. KubeJS 26 中不要再使用旧的 `.strongNBT()` 示例写法。本项目已兼容旧的 `{ item: '...' }`、`{ tag: '...' }` 和奇点物品组件字符串，但 `.strongNBT()` 本身不是当前可用 API。
2. 如果使用 `event.custom()`，始终写完整的 `type`。
3. 推荐用 `Item.of('item_id[component=value]')` 表示带组件的物品，例如奇点物品。
4. `avaritia.infinity_catalyst` 和 `avaritia.eternal_singularity` 的输出由配方类型决定，不能通过 `result` 改成其他物品。
5. 自定义奇点注册后，依赖这些奇点的配方需要在同一次服务端脚本加载后重新生成，测试时优先重启或执行 `/reload`。

### 6. 调试清单

1. 确认客户端或服务端已经安装 KubeJS 和 Rhino。
2. 确认脚本位于 `kubejs/server_scripts/`。
3. 查看 `logs/latest.log`，搜索 `KubeJS server scripts`。
4. 如果配方失败，搜索 `Failed to create`、`Couldn't parse data file`、`avaritia:`。
5. 如果奇点未显示，确认 `AvaritiaEvents.singularity(...)` 是否执行，以及 `setEnabled(false)` 是否被设置。
6. 如果 JEI 中看不到配方，先确认游戏内配方是否实际加载，再检查 JEI 和服务端配方同步。

---

## English Guide

### 1. Requirements

Install these mods in your modpack or test instance:

- Avaritia
- KubeJS
- Rhino

Put scripts in:

```text
kubejs/server_scripts/
```

Custom singularities and recipes should both be placed in `server_scripts`. After editing scripts, run `/reload`; if the result still does not change, restart the client or server.

KubeJS is an optional integration in this project. For local development, you may place the KubeJS and Rhino jars in `run/mods`, but do not commit those jars. Remove them before `runData` unless you have a dedicated data-run-safe setup.

### 2. Custom Singularities

Create:

```text
kubejs/server_scripts/avaritia_singularities.js
```

Example:

```js
AvaritiaEvents.singularity(event => {
    event.register('avaritia:dragon_egg', singularity => {
        singularity
            .setDisplayName('singularity.avaritia.dragon_egg')
            .setColors(0x7A7B78, 0x4B2A7A)
            .setCount(1)
            .setTimeCost(100)
            .setIngredient(Ingredient.of('minecraft:dragon_egg'))
    })
})
```

Method reference:

| Method | Meaning |
| --- | --- |
| `event.register(id, callback)` | Registers a runtime singularity. |
| `setDisplayName(key)` | Sets a translation key, not a literal display name. |
| `setColors(overlay, underlay)` | Sets singularity colors as RGB hex values. |
| `setCount(count)` | Sets the compressor input amount. |
| `setTimeCost(ticks)` | Sets compressor time in ticks. |
| `setIngredient(Ingredient.of(...))` | Sets the compressor input item or tag. |
| `setRecipeEnabled(false)` | Registers the singularity item without generating its compressor recipe. |
| `setEnabled(false)` | Disables this singularity. |

To remove a default singularity compressor recipe, use `removeRecipe` in the singularity event. Pass the singularity id, not the recipe id:

```js
AvaritiaEvents.singularity(event => {
    // Removes avaritia:iron_singularity while keeping the Iron Singularity item registered.
    event.removeRecipe('avaritia:iron')
})
```

To remove all default singularity compressor recipes:

```js
AvaritiaEvents.singularity(event => {
    event.removeAllRecipe()
})
```

Only use `remove` or `removeAll` when the singularity itself should also disappear from creative tabs, Eternal Singularity ingredients, and similar singularity lists:

```js
AvaritiaEvents.singularity(event => {
    event.remove('avaritia:iron')
})
```

For localized names, add resource files:

```text
kubejs/assets/avaritia/lang/zh_cn.json
kubejs/assets/avaritia/lang/en_us.json
```

Example:

```json
{
  "singularity.avaritia.dragon_egg": "龙蛋奇点"
}
```

```json
{
  "singularity.avaritia.dragon_egg": "Dragon Egg Singularity"
}
```

To reference that singularity item in scripts, use the item component:

```js
Item.of('avaritia:singularity[avaritia:singularity_id="avaritia:dragon_egg"]')
```

### 3. Avaritia Recipe Helpers

Create:

```text
kubejs/server_scripts/avaritia_recipes.js
```

Example:

```js
ServerEvents.recipes(event => {
    const { avaritia } = event.recipes

    avaritia.shaped_table(
        4,
        'avaritia:infinity_sword',
        [
            '       I ',
            '      III',
            '     III ',
            '    III  ',
            ' C III   ',
            '  CII    ',
            '  NC     ',
            ' N  C    ',
            'X        '
        ],
        {
            C: 'avaritia:crystal_matrix_ingot',
            I: 'avaritia:infinity_ingot',
            N: 'avaritia:neutron_ingot',
            X: 'avaritia:infinity_catalyst'
        }
    )

    avaritia.compressor(
        Ingredient.of('#c:ingots/copper'),
        Item.of('avaritia:singularity[avaritia:singularity_id="avaritia:copper"]')
    )
        .inputCount(2000)
        .timeCost(240)

    avaritia.infinity_catalyst('default', [
        'minecraft:emerald_block',
        'avaritia:crystal_matrix_ingot',
        'avaritia:neutron_ingot',
        'avaritia:cosmic_meatballs',
        'avaritia:ultimate_stew',
        'avaritia:endest_pearl',
        'avaritia:record_fragment'
    ])

    avaritia.eternal_singularity([
        'minecraft:emerald_block',
        'avaritia:crystal_matrix_ingot',
        'avaritia:neutron_ingot',
        'avaritia:cosmic_meatballs',
        'avaritia:ultimate_stew',
        'avaritia:endest_pearl',
        'avaritia:record_fragment'
    ], 1)
})
```

Common recipe helpers:

| Method | Purpose |
| --- | --- |
| `avaritia.shaped_table(tier, result, pattern, key)` | Shaped Dire Crafting Table recipe. |
| `avaritia.shapeless_table(result, ingredients)` | Shapeless Dire Crafting Table recipe with default tier `0`. |
| `avaritia.shapeless_table(result, tier, ingredients)` | Shapeless recipe with an explicit tier. |
| `avaritia.compressor(ingredient, result)` | Neutronium Compressor recipe. Chain `inputCount` and `timeCost` as needed. |
| `avaritia.infinity_catalyst(group, ingredients)` | Infinity Catalyst recipe. The output is fixed to Infinity Catalyst. |
| `avaritia.eternal_singularity(ingredients, count)` | Eternal Singularity recipe. The output is fixed to Eternal Singularity. |
| `avaritia.extreme_smithing(result, template, base, addition)` | Extreme Smithing recipe. |
| `avaritia.no_consume_catalyst_shaped(pattern, key, result, tier)` | Shaped recipe whose catalyst is not consumed. |

### 4. Raw JSON Recipes With `event.custom()`

Use `event.custom()` when you need full control over the recipe JSON or when porting datapack recipes.

Shaped Dire Crafting Table recipe:

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:shaped_table',
        tier: 4,
        pattern: [
            '       I ',
            '      III',
            '     III ',
            '    III  ',
            ' C III   ',
            '  CII    ',
            '  NC     ',
            ' N  C    ',
            'X        '
        ],
        key: {
            C: [{ item: 'avaritia:crystal_matrix_ingot' }],
            I: [{ item: 'avaritia:infinity_ingot' }],
            N: [{ item: 'avaritia:neutron_ingot' }],
            X: [{ item: 'avaritia:infinity_catalyst' }]
        },
        result: { item: 'avaritia:infinity_sword' }
    })
})
```

Compressor recipe:

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:compressor',
        ingredient: { tag: 'c:ingots/copper' },
        result: {
            item: 'avaritia:singularity[avaritia:singularity_id="avaritia:copper"]',
            count: 2
        },
        inputCount: 2000,
        timeCost: 300
    })
})
```

Infinity Catalyst recipe:

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:infinity_catalyst',
        group: 'default',
        ingredients: [
            'minecraft:emerald_block',
            'avaritia:crystal_matrix_ingot',
            'avaritia:neutron_ingot',
            'avaritia:cosmic_meatballs',
            'avaritia:ultimate_stew',
            'avaritia:endest_pearl',
            'avaritia:record_fragment'
        ]
    })
})
```

Eternal Singularity recipe:

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:eternal_singularity',
        ingredients: [
            'minecraft:emerald_block',
            'avaritia:crystal_matrix_ingot',
            'avaritia:neutron_ingot',
            'avaritia:cosmic_meatballs',
            'avaritia:ultimate_stew',
            'avaritia:endest_pearl',
            'avaritia:record_fragment'
        ],
        custom: true
    })
})
```

Important: `event.custom()` recipes must include `type`. If an old Eternal Singularity example omits `type: 'avaritia:eternal_singularity'`, the current version will not recognize it as an Avaritia recipe.

### 5. Notes For Porting Old Scripts

1. Do not use old `.strongNBT()` examples with KubeJS 26. This project supports old `{ item: '...' }`, `{ tag: '...' }`, and singularity item component strings, but `.strongNBT()` itself is not a current API.
2. Always provide a complete `type` when using `event.custom()`.
3. Prefer `Item.of('item_id[component=value]')` for component-bearing items, such as singularity items.
4. `avaritia.infinity_catalyst` and `avaritia.eternal_singularity` have fixed outputs. You cannot change their output by adding `result`.
5. Recipes depending on runtime singularities must be regenerated after the singularity script loads. Use `/reload` or restart the instance when testing.

### 6. Debug Checklist

1. Confirm that KubeJS and Rhino are installed.
2. Confirm that scripts are in `kubejs/server_scripts/`.
3. Open `logs/latest.log` and search for `KubeJS server scripts`.
4. If a recipe fails, search for `Failed to create`, `Couldn't parse data file`, and `avaritia:`.
5. If a singularity is missing, confirm that `AvaritiaEvents.singularity(...)` ran and that `setEnabled(false)` was not set.
6. If JEI does not show a recipe, first confirm that the recipe exists in-game, then check JEI and server recipe sync.
