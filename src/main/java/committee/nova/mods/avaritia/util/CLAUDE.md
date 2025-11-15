# 工具模块 - 详细文档

## 📍 导航面包屑
**[项目根] [^1]** / **[工具模块] [^2]**

---

## 模块概述

**路径**：`src/main/java/committee/nova/mods/avaritia/util/`
**文件数量**：9个Java文件
**功能定位**：提供项目通用的工具类、常量定义和辅助方法

### 核心职责
- 定义项目全局常量
- 提供数学和几何计算工具
- 实现通用算法和数据结构
- 封装常用操作和辅助方法

## 🔧 核心组件

### 1. 常量定义类

#### Const.java - 项目常量
**功能**：定义项目的全局常量，包括模组ID、版本、路径等

```java
public class Const {
    // 模组标识
    public static final String MODID = "avaritia";
    public static final String MOD_NAME = "Re-Avaritia-forged";
    public static final String VERSION = "1.3.9.3-beta1";

    // 路径常量
    public static final String TEXTURE_PATH = "textures/";
    public static final String MODEL_PATH = "models/";
    public static final String GUI_PATH = "textures/gui/";

    // 资源路径
    public static final ResourceLocation TEXTURE_SHEET = new ResourceLocation(MODID, "textures/atlas/blocks.png");
    public static final ResourceLocation ENTITY_SHEET = new ResourceLocation(MODID, "textures/entity/avaritia_entities.png");
}
```

#### 数学常量
- **π常数**：π、π/2、2π等
- **黄金比例**：φ常数及其变体
- **物理常量**：重力、光速等Minecraft相关物理量

### 2. 数学工具类

#### MathUtils.java - 数学计算工具
**功能**：提供复杂的数学计算和几何运算

```java
public class MathUtils {
    /**
     * 计算点到直线的距离
     */
    public static double distancePointToLine(Vec3 point, Vec3 lineStart, Vec3 lineEnd) {
        // 实现点到直线距离算法
    }

    /**
     * 计算两条直线的交点
     */
    public static Optional<Vec3> lineIntersection(Vec3 line1Start, Vec3 line1End, Vec3 line2Start, Vec3 line2End) {
        // 实现直线交点计算
    }

    /**
     * 贝塞尔曲线插值
     */
    public static Vec3 bezierInterpolation(Vec3 start, Vec3 control, Vec3 end, float t) {
        // 实现贝塞尔曲线计算
    }

    /**
     * 角度插值（考虑360度环绕）
     */
    public static float interpolateAngle(float from, float to, float progress) {
        // 处理角度环绕的插值
    }
}
```

#### 几何工具类
- **向量运算**：向量的加减乘除、标准化等
- **矩阵计算**：4x4矩阵运算、变换矩阵等
- **坐标变换**：坐标系转换、投影计算等

### 3. 文本处理工具

#### TextUtils.java - 文本处理
**功能**：处理本地化文本、格式化显示等

```java
public class TextUtils {
    /**
     * 格式化数字显示
     */
    public static Component formatNumber(long number) {
        if (number >= 1000000000) {
            return Component.translatable("gui.avaritia.billion", number / 1000000000);
        } else if (number >= 1000000) {
            return Component.translatable("gui.avaritia.million", number / 1000000);
        }
        return Component.literal(String.valueOf(number));
    }

    /**
     * 格式化百分比
     */
    public static Component formatPercent(double value) {
        return Component.translatable("gui.avaritia.percent", (int)(value * 100));
    }

    /**
     * 格式化时间显示
     */
    public static Component formatTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return Component.translatable("gui.avaritia.time.hours", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return Component.translatable("gui.avaritia.time.minutes", minutes, seconds % 60);
        } else {
            return Component.translatable("gui.avaritia.time.seconds", seconds);
        }
    }
}
```

### 4. 资源管理工具

#### ResourceUtils.java - 资源管理
**功能**：管理纹理、模型、音频等资源

```java
public class ResourceUtils {
    /**
     * 创建资源位置
     */
    public static ResourceLocation location(String path) {
        return new ResourceLocation(Const.MODID, path);
    }

    /**
     * 获取纹理路径
     */
    public static String texture(String name) {
        return TEXTURE_PATH + name + ".png";
    }

    /**
     * 获取模型路径
     */
    public static String model(String name) {
        return MODEL_PATH + name + ".json";
    }
}
```

### 5. 物品工具

#### ItemUtils.java - 物品处理
**功能**：提供物品相关的便利方法

```java
public class ItemUtils {
    /**
     * 复制物品栈
     */
    public static ItemStack copyStack(ItemStack stack, int count) {
        ItemStack result = stack.copy();
        result.setCount(count);
        return result;
    }

    /**
     * 检查物品是否为空
     */
    public static boolean isEmpty(ItemStack stack) {
        return stack.isEmpty() || stack.getCount() <= 0;
    }

    /**
     * 获取物品的本地化名称
     */
    public static Component getItemName(ItemStack stack) {
        return stack.getDisplayName();
    }

    /**
     * 检查两个物品栈是否相似（忽略数量）
     */
    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        return ItemStack.matches(stack1, stack2);
    }
}
```

### 6. 方块工具

#### BlockUtils.java - 方块处理
**功能**：提供方块相关的便利方法

```java
public class BlockUtils {
    /**
     * 检查方块是否为空
     */
    public static boolean isEmpty(BlockState state) {
        return state.isAir();
    }

    /**
     * 获取方块位置字符串表示
     */
    public static String posToString(BlockPos pos) {
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * 检查两个位置是否相邻
     */
    public static boolean isAdjacent(BlockPos pos1, BlockPos pos2) {
        return pos1.distSqr(pos2) == 1.0;
    }
}
```

## 🧮 算法实现

### 1. 搜索算法
- **深度优先搜索**：DFS实现
- **广度优先搜索**：BFS实现
- **A*路径搜索**：启发式路径查找
- **最近邻搜索**：K-D树实现

### 2. 排序算法
- **快速排序**：高效率排序
- **归并排序**：稳定排序
- **堆排序**：内存友好的排序

### 3. 数据结构
- **优先队列**：支持优先级操作
- **循环缓冲区**：固定大小的环形队列
- **对象池**：复用对象的内存管理

## 📊 性能优化

### 缓存机制
- **静态缓存**：缓存计算结果
- **LRU缓存**：最近最少使用缓存
- **内存池**：对象复用减少GC压力

### 算法优化
- **空间换时间**：使用更多内存换取更快速度
- **分治算法**：将大问题分解为小问题
- **动态规划**：避免重复计算

## 🧪 测试策略

### 单元测试
- **数学工具测试**：验证数学计算正确性
- **文本工具测试**：验证文本处理功能
- **资源工具测试**：验证资源路径生成

### 性能测试
- **算法性能基准**：测试算法执行时间
- **内存使用测试**：监控内存占用
- **并发安全测试**：多线程环境测试

## 🔍 调试支持

### 日志记录
- **调试级别日志**：详细的调试信息
- **性能监控**：记录关键操作的执行时间
- **错误跟踪**：记录异常和错误信息

### 验证工具
- **参数验证**：检查输入参数的有效性
- **状态检查**：验证内部状态的一致性
- **边界测试**：测试极端情况下的行为

## 🚀 使用示例

### 基础使用
```java
// 使用常量类
String modId = Const.MODID;
ResourceLocation texture = new ResourceLocation(Const.MODID, "textures/item/infinity_sword.png");

// 使用数学工具
Vec3 result = MathUtils.lerp(startVec, endVec, 0.5f);
float distance = MathUtils.distance(startVec, endVec);

// 使用文本工具
Component formattedNumber = TextUtils.formatNumber(1000000L);
```

### 高级使用
```java
// 自定义工具类扩展
public class CustomMathUtils extends MathUtils {
    public static Vec3 customCalculation(Vec3 input) {
        // 实现自定义计算
        return MathUtils.normalize(input);
    }
}

// 工具类组合使用
public void processItem(ItemStack stack) {
    if (ItemUtils.isEmpty(stack)) return;

    Component name = ItemUtils.getItemName(stack);
    Component formattedName = TextUtils.formatText(name, TextColor.GREEN);

    // 处理逻辑
}
```

## 📈 扩展指南

### 添加新工具类
1. 创建工具类并继承基础功能
2. 实现核心算法和方法
3. 添加相应的测试用例
4. 更新文档和使用示例

### 性能优化建议
1. **避免重复计算**：缓存计算结果
2. **使用合适的数据结构**：根据使用场景选择
3. **减少对象创建**：使用对象池或静态实例
4. **并行处理**：在适当时机使用多线程

### 最佳实践
1. **方法命名清晰**：使用描述性的方法名
2. **参数验证**：检查输入参数的有效性
3. **异常处理**：妥善处理异常情况
4. **文档完整**：提供详细的Javadoc

## 🔗 相关资源

- **根级文档**：[../../CLAUDE.md](../../CLAUDE.md)
- **API模块**：[../api/CLAUDE.md](../api/CLAUDE.md)
- **通用模块**：[../common/CLAUDE.md](../common/CLAUDE.md)

---

## 更新日志

- **v1.0** (2025-10-26)：初始工具模块文档
  - 完整的工具类说明
  - 算法实现文档
  - 使用示例和最佳实践

---

*注：本文档由AI自动生成，基于代码静态分析。如需最新信息，请查看源代码注释。*