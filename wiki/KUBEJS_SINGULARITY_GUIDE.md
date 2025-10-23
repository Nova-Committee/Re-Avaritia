# KubeJS Singularity Creation Guide
<p>
    <a href="KUBEJS_SINGULARITY_GUIDE.md">English</a> | 
    <a href="KUBEJS_SINGULARITY_GUIDE_CN.md">简体中文</a>
</p>

## Overview

The new singularity system fully supports KubeJS scripting! You can dynamically define singularities using JavaScript, supporting complex conditional logic and dynamic content generation.

## 📋 Table of Contents

- [Basic Usage](#-basic-usage)
- [Advanced Features](#-advanced-features)
- [Condition System](#-condition-system)
- [Practical Examples](#-practical-examples)
- [Troubleshooting](#-troubleshooting)

## 🚀 Basic Usage

### Simplest Singularity

```javascript
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'example',
        name: 'singularity.custom.example',
        colors: ['FF0000', '0000FF'],  // [overlay color, underlay color]
        ingredient: 'minecraft:iron_ingot',
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    })
})
```

### Tag-based Singularity

```javascript
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'iron',
        name: 'singularity.custom.ingots',
        colors: ['C0C0C0', '808080'],
        tag: 'forge:ingots/iron',  // All iron ingots
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    })
})
```

## 🔧 Advanced Features

### Field Description

| Field           | Type     | Required | Default | Description                              |
|-----------------|----------|----------|---------|------------------------------------------|
| `type`          | String   | ✅        | -       | Fixed to `'avaritia:singularity'`        |
| `id`            | String   | ✅        | -       | Singularity display ID                   |
| `name`          | String   | ✅        | -       | Singularity display name (translation key) |
| `colors`        | Array    | ✅        | -       | `[overlay color, underlay color]` hex colors |
| `ingredient`    | String   | ❌        | -       | Item ID (choose one with tag)            |
| `tag`           | String   | ❌        | -       | Item tag (choose one with ingredient)    |
| `materialCount` | Integer  | ❌        | 1000    | Required material count                  |
| `timeRequired`  | Integer  | ❌        | 200     | Compression time (game ticks)            |
| `enabled`       | Boolean  | ❌        | true    | Whether to enable this singularity       |
| `recipeDisabled`| Boolean  | ❌        | false   | Whether to disable recipe                |

### Color Format

Colors use 6-digit hexadecimal strings:

```javascript
colors: [
    'FF0000',  // Red (overlay color)
    '0000FF'   // Blue (underlay color)
]
```

Common color references:
- Red: `FF0000`
- Green: `00FF00`
- Blue: `0000FF`
- Yellow: `FFFF00`
- Purple: `FF00FF`
- Cyan: `00FFFF`
- White: `FFFFFF`
- Black: `000000`

## 🎯 Condition System

### Basic Conditions

```javascript
conditions: [
    {
        type: 'forge:mod_loaded',
        modid: 'thermal'
    }
]
```

### Combined Conditions

```javascript
conditions: [
    {
        type: 'forge:and',
        value: [
            {
                type: 'forge:mod_loaded',
                modid: 'thermal'
            },
            {
                type: 'forge:not',
                value: {
                    type: 'forge:tag_empty',
                    tag: 'forge:gears/diamond'
                }
            }
        ]
    }
]
```

### Common Condition Types

- `forge:mod_loaded` - Mod is loaded
- `forge:not` - Negate condition
- `forge:and` - All conditions must be satisfied
- `forge:or` - Any condition is satisfied
- `forge:tag_empty` - Tag is empty
- `forge:item_exists` - Item exists

## 💡 Practical Examples

### 1. Batch Create Metal Singularities

```javascript
ServerEvents.recipes(event => {
    const metals = [
        { item: 'minecraft:iron_ingot', name: 'iron', colors: ['E1E1E1', '6C6C6C'] },
        { item: 'minecraft:gold_ingot', name: 'gold', colors: ['FFD700', 'D98E04'] },
        { item: 'minecraft:copper_ingot', name: 'copper', colors: ['FA977C', 'BC5430'] }
    ]

    metals.forEach(metal => {
        event.custom({
            type: 'avaritia:singularity',
            id: `${metal.name}`,
            name: `singularity.batch.${metal.name}`,
            colors: metal.colors,
            ingredient: metal.item,
            materialCount: 1000,
            timeRequired: 200,
            enabled: true,
            recipeDisabled: false
        })
    })
})
```

### 2. Mod-based Conditional Singularities

```javascript
ServerEvents.recipes(event => {
    // Only create if tech mod exists
    if (Platform.isLoaded('thermal')) {
        event.custom({
            type: 'avaritia:singularity',
            id: 'signalum',
            name: 'singularity.tech.signalum',
            colors: ['00FFFF', '0080FF'],
            tag: 'forge:ingots/signalum',
            materialCount: 500,
            timeRequired: 350,
            enabled: true,
            recipeDisabled: false,
            conditions: [
                {
                    type: 'forge:mod_loaded',
                    modid: 'thermal'
                }
            ]
        })
    }
})
```

### 3. Seasonal Singularities

```javascript
ServerEvents.recipes(event => {
    const month = new Date().getMonth()

    if (month >= 11 || month <= 1) { // Winter
        event.custom({
            type: 'avaritia:singularity',
            id: 'winter',
            name: 'singularity.seasonal.winter',
            colors: ['FFFFFF', 'B0E0E6'],
            ingredient: 'minecraft:snow_block',
            materialCount: 1500,
            timeRequired: 300,
            enabled: true,
            recipeDisabled: false
        })
    }
})
```

### 4. High-Difficulty Singularity

```javascript
ServerEvents.recipes(event => {
    // Dragon Egg Singularity - Ultimate Challenge
    event.custom({
        type: 'avaritia:singularity',
        id: 'dragon_egg',
        name: 'singularity.ultimate.dragon_egg',
        colors: ['FF0000', '8B0000'],
        ingredient: 'minecraft:dragon_egg',
        materialCount: 1,  // Only need one, but very hard to obtain
        timeRequired: 2400,  // Takes a long time
        enabled: true,
        recipeDisabled: false
    })
})
```

## 🛠️ Dynamic Content Generation

### Using Global Configuration

```javascript
// Define configuration at the top of the script
const SINGULARITY_CONFIG = {
    difficulty: 'normal',  // 'easy', 'normal', 'hard', 'hardcore'
    enableExperimental: false,
    customMultiplier: 1.0
}

// Apply configuration
ServerEvents.recipes(event => {
    let multiplier = 1.0

    switch(SINGULARITY_CONFIG.difficulty) {
        case 'easy': multiplier = 0.5; break
        case 'hard': multiplier = 2.0; break
        case 'hardcore': multiplier = 5.0; break
    }

    event.custom({
        type: 'avaritia:singularity',
        id: 'iron_ingot',
        name: 'singularity.adaptive.iron',
        colors: ['E1E1E1', '6C6C6C'],
        ingredient: 'minecraft:iron_ingot',
        materialCount: Math.floor(1000 * multiplier),
        timeRequired: Math.floor(200 * multiplier),
        enabled: true,
        recipeDisabled: false
    })
})
```

## 🔍 Troubleshooting

### Common Issues

1. **Singularity not showing up**
   ```javascript
   // Check console for errors
   console.log('Starting singularity creation...')

   // Ensure enabled: true is properly set
   enabled: true  // Must be explicitly set to true
   ```

2. **Color display incorrect**
   ```javascript
   // Correct 6-digit hexadecimal format
   colors: ['FF0000', '0000FF']  // ✅ Correct
   colors: ['#FF0000', '#0000FF']  // ❌ Wrong, don't use #
   colors: ['F00', '00F']  // ❌ Wrong, must be 6 digits
   ```

3. **Conditions not working**
   ```javascript
   // Ensure condition syntax is correct
   conditions: [
       {
           type: 'forge:mod_loaded',
           modid: 'thermal'  // Ensure mod ID is correct
       }
   ]
   ```

4. **Tags not working**
   ```javascript
   // Tag format examples
   tag: 'forge:ingots/iron'      // ✅ Correct
   tag: '#forge:ingots/iron'     // ❌ Don't use # symbol
   tag: 'forge:ingots/iron/'     // ❌ Don't end with slash
   ```

### Debugging Tips

1. **Use console.log to output debug information**

```javascript
ServerEvents.recipes(event => {
    console.log('Starting custom singularity creation...')

    event.custom({
        type: 'avaritia:singularity',
        // ... configuration
    })

    console.log('Singularity creation complete!')
})
```

2. **Step-by-step validation**

```javascript
// First create a simple singularity
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'simple',
        name: 'singularity.debug.simple',
        colors: ['FF0000', '0000FF'],
        ingredient: 'minecraft:stone',  // Simplest item
        materialCount: 1,
        timeRequired: 1,
        enabled: true,
        recipeDisabled: false
    })
})
```

3. **Check mod loading status**

```javascript
console.log('Loaded mods:', Platform.getLoadedMods())
console.log('Is thermal loaded:', Platform.isLoaded('thermal'))
```

## 📚 API Reference

### KubeJS Global Objects

- `ServerEvents.recipes()` - Register recipe events
- `Platform.isLoaded(modid)` - Check if mod is loaded
- `Platform.getLoadedMods()` - Get list of loaded mods
- `global.config` - Access global configuration

### Singularity Related

- `SingularityDataHandler.getInstance()` - Get data manager
- `manager.getSingularities()` - Get all singularities
- `manager.getSingularity(id)` - Get specific singularity

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