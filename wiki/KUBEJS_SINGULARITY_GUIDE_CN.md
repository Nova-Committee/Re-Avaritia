# KubeJS奇点创建指南
<p>
    <a href="KUBEJS_SINGULARITY_GUIDE.md">English</a> | 
    <a href="KUBEJS_SINGULARITY_GUIDE_CN.md">简体中文</a>
</p>

## 概述

奇点系统支持基于资源重载的 KubeJS 定义。脚本中的新增、替换和删除会在配方加载期间收集，并在本次 `/reload` 后生效；不会即时修改正在使用的配方管理器。

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
            .setRecipeEnabled(true)
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
            .setTag('c:ingots/iron')
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
| `setCount`          | 整数         | ❌  | 1000  | 所需材料数量，必须大于 0       |
| `setTimeCost`       | 整数         | ❌  | 240   | 压缩时间（游戏刻），必须大于 0 |
| `setEnabled`        | 布尔值        | ❌  | true  | 是否启用此奇点              |
| `setRecipeEnabled`  | 布尔值        | ❌  | true  | 是否生成对应压缩机配方        |
| `setRecipeDisabled` | 布尔值        | ❌  | false | 是否禁用配方               |

### 颜色格式

颜色使用 JavaScript 十六进制整数：

```javascript
s.setColors(0xFF0000, 0x0000FF)
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

### 奇点事件

- `event.register(id)` / `event.register(id, consumer)`：新增或替换脚本奇点
- `event.remove(id)` / `event.removeAll()`：删除一个或全部有效奇点
- `event.removeRecipe(id)` / `event.removeAllRecipe()`：禁用生成的压缩机配方

`event.register(id)` 会创建显示名默认为 id 的合法对象。默认对象没有原料，因此配置原料前不会生成压缩机配方。

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
   - 修改或删除脚本定义后执行 `/reload`，旧脚本状态不会残留

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
