# KubeJS奇点创建指南
<p>
    <a href="KUBEJS_SINGULARITY_GUIDE.md">English</a> | 
    <a href="KUBEJS_SINGULARITY_GUIDE_CN.md">简体中文</a>
</p>

## 概述

新的奇点系统完全支持KubeJS脚本化创建！您可以使用JavaScript动态定义奇点，支持复杂的条件逻辑和动态内容生成。

## 📋 目录

- [基础用法](#基础用法)
- [高级功能](#高级功能)
- [条件系统](#条件系统)
- [实际示例](#实际示例)
- [故障排除](#故障排除)

## 🚀 基础用法

### 最简单的奇点

```javascript
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'example',
        name: 'singularity.custom.example',
        colors: ['FF0000', '0000FF'],  // [覆盖色, 底层色]
        ingredient: 'minecraft:iron_ingot',
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    })
})
```

### 使用标签的奇点

```javascript
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'iron',
        name: 'singularity.custom.ingots',
        colors: ['C0C0C0', '808080'],
        tag: 'forge:ingots/iron',  // 所有铁锭
        materialCount: 1000,
        timeRequired: 200,
        enabled: true,
        recipeDisabled: false
    })
})
```

## 🔧 高级功能

### 字段说明

| 字段               | 类型 | 必需 | 默认值 | 说明                           |
|------------------|------|-------|---------|------------------------------|
| `type`           | 字符串 | ✅ | - | 固定为 `'avaritia:singularity'` |
| `id`             | 字符串 | ✅ | - | 奇点显示id                       |
| `name`           | 字符串 | ✅ | - | 奇点显示名称（翻译键）                  |
| `colors`         | 数组 | ✅ | - | `[覆盖色, 底层色]` 十六进制颜色          |
| `ingredient`     | 字符串 | ❌ | - | 物品ID（与tag二选一）                |
| `tag`            | 字符串 | ❌ | - | 物品标签（与ingredient二选一）         |
| `materialCount`  | 整数 | ❌ | 1000 | 所需材料数量                       |
| `timeRequired`   | 整数 | ❌ | 200 | 压缩时间（游戏刻）                    |
| `enabled`        | 布尔值 | ❌ | true | 是否启用此奇点                      |
| `recipeDisabled` | 布尔值 | ❌ | false | 是否禁用配方                       |

### 颜色格式

颜色使用6位十六进制字符串：

```javascript
colors: [
    'FF0000',  // 红色 (覆盖色)
    '0000FF'   // 蓝色 (底层色)
]
```

常用颜色参考：
- 红色: `FF0000`
- 绿色: `00FF00`
- 蓝色: `0000FF`
- 黄色: `FFFF00`
- 紫色: `FF00FF`
- 青色: `00FFFF`
- 白色: `FFFFFF`
- 黑色: `000000`

## 🎯 条件系统

### 基础条件

```javascript
conditions: [
    {
        type: 'forge:mod_loaded',
        modid: 'thermal'
    }
]
```

### 组合条件

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

### 常用条件类型

- `forge:mod_loaded` - 模组已加载
- `forge:not` - 条件取反
- `forge:and` - 所有条件都必须满足
- `forge:or` - 任一条件满足
- `forge:tag_empty` - 标签为空
- `forge:item_exists` - 物品存在

## 💡 实际示例

### 1. 批量创建金属奇点

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

### 2. 基于模组的条件奇点

```javascript
ServerEvents.recipes(event => {
    // 只有在科技模组存在时才创建
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

### 3. 季节性奇点

```javascript
ServerEvents.recipes(event => {
    const month = new Date().getMonth()

    if (month >= 11 || month <= 1) { // 冬季
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

### 4. 高难度奇点

```javascript
ServerEvents.recipes(event => {
    // 龙蛋奇点 - 终极挑战
    event.custom({
        type: 'avaritia:singularity',
        id: 'dragon_egg',
        name: 'singularity.ultimate.dragon_egg',
        colors: ['FF0000', '8B0000'],
        ingredient: 'minecraft:dragon_egg',
        materialCount: 1,  // 只需要一个，但很难获得
        timeRequired: 2400,  // 需要很长时间
        enabled: true,
        recipeDisabled: false
    })
})
```

## 🛠️ 动态内容生成

### 使用全局配置

```javascript
// 在脚本顶部定义配置
const SINGULARITY_CONFIG = {
    difficulty: 'normal',  // 'easy', 'normal', 'hard', 'hardcore'
    enableExperimental: false,
    customMultiplier: 1.0
}

// 应用配置
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

## 🔍 故障排除

### 常见问题

1. **奇点没有显示**
   ```javascript
   // 检查控制台是否有错误
   console.log('开始创建奇点...')

   // 确保正确设置enabled: true
   enabled: true  // 必须显式设置为true
   ```

2. **颜色显示错误**
   ```javascript
   // 正确的6位十六进制格式
   colors: ['FF0000', '0000FF']  // ✅ 正确
   colors: ['#FF0000', '#0000FF']  // ❌ 错误，不要#
   colors: ['F00', '00F']  // ❌ 错误，必须6位
   ```

3. **条件不生效**
   ```javascript
   // 确保条件语法正确
   conditions: [
       {
           type: 'forge:mod_loaded',
           modid: 'thermal'  // 确保模组ID正确
       }
   ]
   ```

4. **标签不工作**
   ```javascript
   // 标签格式示例
   tag: 'forge:ingots/iron'      // ✅ 正确
   tag: '#forge:ingots/iron'     // ❌ 不要#号
   tag: 'forge:ingots/iron/'     // ❌ 不要末尾斜杠
   ```

### 调试技巧

1. **使用console.log输出调试信息**

```javascript
ServerEvents.recipes(event => {
    console.log('开始创建自定义奇点...')

    event.custom({
        type: 'avaritia:singularity',
        // ... 配置
    })

    console.log('奇点创建完成！')
})
```

2. **分阶段验证**

```javascript
// 先创建一个简单的奇点
ServerEvents.recipes(event => {
    event.custom({
        type: 'avaritia:singularity',
        id: 'simple',
        name: 'singularity.debug.simple',
        colors: ['FF0000', '0000FF'],
        ingredient: 'minecraft:stone',  // 最简单的物品
        materialCount: 1,
        timeRequired: 1,
        enabled: true,
        recipeDisabled: false
    })
})
```

3. **检查模组加载状态**

```javascript
console.log('已加载的模组:', Platform.getLoadedMods())
console.log('thermal是否加载:', Platform.isLoaded('thermal'))
```

## 📚 API参考

### KubeJS全局对象

- `ServerEvents.recipes()` - 注册配方事件
- `Platform.isLoaded(modid)` - 检查模组是否加载
- `Platform.getLoadedMods()` - 获取已加载模组列表
- `global.config` - 访问全局配置

### 奇点相关

- `SingularityDataHandler.getInstance()` - 获取数据管理器
- `manager.getSingularities()` - 获取所有奇点
- `manager.getSingularity(id)` - 获取特定奇点

## 🎉 最佳实践

1. **命名约定**
   - 使用描述性名称：`singularity.mod.material`
   - 避免冲突：使用你的模组ID作为前缀

2. **平衡性考虑**
   - 稀有材料通常需要较少数量
   - 普通材料需要更多数量
   - 考虑压缩时间的合理性

3. **兼容性**
   - 使用条件确保模组依赖
   - 提供默认选项供其他模组覆盖

4. **性能考虑**
   - 避免创建过多奇点
   - 合理使用批量操作

---

## 📞 获取帮助

如果遇到问题：

1. 检查游戏日志中的错误信息
2. 验证JSON语法正确性
3. 确认KubeJS脚本语法正确
4. 在相关社区寻求帮助

记住，KubeJS的奇点创建是一个强大的功能，合理使用可以大大扩展游戏体验！