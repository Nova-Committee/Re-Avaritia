# KubeJS Singularity Creation Guide
<p>
    <a href="KUBEJS_SINGULARITY_GUIDE.md">English</a> | 
    <a href="KUBEJS_SINGULARITY_GUIDE_CN.md">简体中文</a>
</p>

## Overview

The singularity system supports reload-based KubeJS definitions. Script additions, replacements, and removals are collected while recipes load and become effective after the current `/reload`; they are not live recipe mutations.

## 📋 Table of Contents

- [Basic Usage](#-basic-usage)
- [Advanced Features](#-advanced-features)

## 🚀 Basic Usage

### Simplest Singularity

```javascript
AvaritiaEvents.singularity(event => {
    event.register("avaritia:example", s => {
        s
            .setDisplayName("singularity.avaritia.example")
            .setColors(0xC0C0C0, 0x808080) // [overlay color, underlay color]
            .setCount(1000)
            .setTimeCost(200)
            .setIngredient(Ingredient.of("minecraft:iron_ingot"))
            .setEnabled(true)
            .setRecipeEnabled(true)
    })
})
```

### Tag-based Singularity

```javascript
AvaritiaEvents.singularity(event => {
    event.register("avaritia:iron", s => {
        s
            .setDisplayName("singularity.avaritia.ingots")
            .setColors(0xC0C0C0, 0x808080) // [overlay color, underlay color]
            .setCount(1000)
            .setTimeCost(200)
            .setTag('forge:ingots/iron')
            .setEnabled(true)
            .setRecipeDisabled(false)
    })
})
```

## 🔧 Advanced Features

### Field Description

| Field               | Type       | Required | Default | Description                                  |
|---------------------|------------|----------|---------|----------------------------------------------|
| `setDisplayName`    | String     | ✅        | -       | Singularity display name (translation key)   |
| `setColors`         | Integer    | ✅        | -       | `[overlay color, underlay color]` hex colors |
| `setIngredient`     | Ingredient | ❌        | -       | Item ID (choose one with tag)                |
| `setTag`            | String     | ❌        | -       | Item tag (choose one with ingredient)        |
| `setCount`          | Integer    | ❌        | 1000    | Required material count; must be greater than zero |
| `setTimeCost`       | Integer    | ❌        | 240     | Compression time; must be greater than zero  |
| `setEnabled`        | Boolean    | ❌        | true    | Whether to enable this singularity           |
| `setRecipeEnabled`  | Boolean    | ❌        | true    | Whether to generate its compressor recipe    |
| `setRecipeDisabled` | Boolean    | ❌        | false   | Whether to disable recipe                    |

### Color Format

Colors use JavaScript hexadecimal integers:

```javascript
s.setColors(0xFF0000, 0x0000FF)
```

Common color references:
- Red: `0xFF0000`
- Green: `0x00FF00`
- Blue: `0x0000FF`
- Yellow: `0xFFFF00`
- Purple: `0xFF00FF`
- Cyan: `0x00FFFF`
- White: `0xFFFFFF`
- Black: `0x000000`


## 📚 API Reference

### Singularity Event

- `event.register(id)` / `event.register(id, consumer)` - Add or replace a script singularity
- `event.remove(id)` / `event.removeAll()` - Remove one or all effective singularities
- `event.removeRecipe(id)` / `event.removeAllRecipe()` - Disable generated compressor recipes

`event.register(id)` creates a valid default object whose display name is the id. It has no ingredient, so it will not generate a compressor recipe until configured.

## 🎉 Best Practices

1. **Naming Conventions**
   - Use descriptive names: `singularity.mod.material`
   - Avoid conflicts: Use your mod ID as prefix

2. **Balance Considerations**
   - Rare materials usually require smaller quantities
   - Common materials need larger quantities
   - Consider reasonable compression times

3. **Compatibility**
   - Use conditions to ensure mod dependencies
   - Provide default options for other mods to override
   - Run `/reload` after editing the script; removing a script definition also takes effect on reload

4. **Performance Considerations**
   - Avoid creating too many singularities
   - Use batch operations reasonably

---

## 📞 Getting Help

If you encounter problems:

1. Check error messages in game logs
2. Verify JSON syntax correctness
3. Confirm KubeJS script syntax is correct
4. Seek help in relevant communities

Remember, KubeJS singularity creation is a powerful feature, and proper use can greatly expand the gaming experience!
