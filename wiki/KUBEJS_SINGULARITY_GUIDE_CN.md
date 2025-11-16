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


## 🚀 基础用法

### 最简单的奇点

```javascript
AvaritiaEvents.singularity(event => {
    event.register("avaritia:example", s => {
        s
            .setDisplayName("singularity.avaritia.example")
            .setColors(0xC0C0C0, 0x808080) // [覆盖色, 底层色]
            .setCount(1000)
            .setTimeCost(200)
            .setIngredient(Ingredient.of("minecraft:iron_ingot"))
            .setEnabled(true)
            .setRecipeDisabled(false)
    })
})
```

### 使用标签的奇点

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

## 🔧 高级功能

### 字段说明

| 字段                  | 类型         | 必需 | 默认值   | 说明                   |
|---------------------|------------|----|-------|----------------------|
| `setDisplayName`    | 字符串        | ✅  | -     | 奇点显示名称（翻译键）          |
| `setColors`         | 十六进制       | ✅  | -     | `[覆盖色, 底层色]` 十六进制颜色  |
| `setIngredient`     | Ingredient | ❌  | -     | 物品ID（与tag二选一）        |
| `setTag`            | 字符串        | ❌  | -     | 物品标签（与ingredient二选一） |
| `setCount`          | 整数         | ❌  | 1000  | 所需材料数量               |
| `setTimeCost`       | 整数         | ❌  | 240   | 压缩时间（游戏刻）            |
| `setEnabled`        | 布尔值        | ❌  | true  | 是否启用此奇点              |
| `setRecipeDisabled` | 布尔值        | ❌  | false | 是否禁用配方               |

### 颜色格式

颜色使用8位十六进制字符串：

```javascript
colors: [
    '0xFF0000',  // 红色 (覆盖色)
    '0x0000FF'   // 蓝色 (底层色)
]
```

常用颜色参考：
- 红色: `0xFF0000`
- 绿色: `0x00FF00`
- 蓝色: `0x0000FF`
- 黄色: `0xFFFF00`
- 紫色: `0xFF00FF`
- 青色: `0x00FFFF`
- 白色: `0xFFFFFF`
- 黑色: `0x000000`

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