# Re-Avaritia (Forge 1.20.1) - 项目AI上下文索引

## 项目愿景与概述

### 🎯 项目定位
**Re-Avaritia** 是对经典Minecraft模组Avaritia的非官方重构版本，专为Minecraft 1.20.1和Forge 47.3.0设计。该模组专注于提供**极限 endgame 内容**，通过引入极其强大但制作困难的工具、盔甲和物品，为玩家提供长期目标和挑战。

### 🚀 核心理念
- **挑战性设计**：避免"银盘托物"的简单获得方式
- **长期目标**：为modded Minecraft玩家提供持久的游戏体验
- **极限装备**：提供远超常规mod的高等级装备和工具
- **技术深度**：结合GregTech等深度模组，提供复杂合成链

## 🏗️ 项目架构总览

### 技术栈
- **游戏版本**：Minecraft 1.20.1
- **Forge版本**：47.3.0
- **Java版本**：17
- **构建工具**：Gradle + NeoForge
- **映射系统**：Parchment 2023.09.03

### 依赖生态
```
核心依赖：
├── Forge (47.3.0) - 模组框架
├── Architectury (9.1.12) - 跨平台兼容
└── Parchment (2023.09.03) - 映射系统

内容模组整合：
├── CraftTweaker (14.0.12) - 配方修改
├── KubeJS (2001.6.5-build.20) - 脚本系统
├── ProjectE (4901949) - 能量系统
├── AE2 (5565729) - 存储系统
└── MEK (5395221) - 自动化系统

配方查看器：
├── JEI (15.20.0.112) - 主要支持
├── REI (12.1.785) - 替代选项
└── EMI (1.1.20+1.20.1) - 现代替代

开发工具：
├── JUnit (5.10.0-M1) - 测试框架
├── Lombok (1.18.+) - 代码简化
├── Mixin (0.8.5) - 字节码注入
└── MixinExtras (0.4.1) - Mixin扩展
```

## 📦 模块架构索引

### 🗂️ 主要模块分布 (共514个Java文件)

#### 1. API模块 (175文件) - `src/main/java/committee/nova/mods/avaritia/api/`
**功能定位**：提供跨模块的通用接口和工具类
**核心子模块**：
- `client/` - 客户端API (渲染、模型、UI)
- `common/` - 通用游戏逻辑API
- `iface/` - 核心接口定义
- `init/` - 初始化相关API
- `util/` - 通用工具API

**关键特征**：
- 客户端渲染系统 (IVertexConsumer, ModelRegistryHelper)
- 光照模型系统 (LightMatrix, PlanarLightModel)
- 物品模型包装 (WrappedItemModel)
- 过滤器接口 (IFilterItem)

#### 2. 客户端模块 (66文件) - `src/main/java/committee/nova/mods/avaritia/client/`
**功能定位**：处理所有客户端专属功能
**核心子模块**：
- `hud/` - 游戏内界面元素
- `model/` - 客户端模型处理
- `particle/` - 粒子效果系统
- `render/` - 渲染引擎
- `screen/` - GUI界面
- `shader/` - 着色器系统

**入口类**：`AvaritiaForgeClient.java` - 客户端初始化入口

#### 3. 通用模块 (135文件) - `src/main/java/committee/nova/mods/avaritia/common/`
**功能定位**：包含游戏核心逻辑和内容定义
**核心子模块**：
- `block/` - 方块系统
- `item/` - 物品系统 (工具、武器、资源)
- `entity/` - 生物系统
- `tile/` - 方块实体
- `container/` - 容器系统
- `crafting/` - 合成系统
- `net/` - 网络通信
- `wrappers/` - 包装器类

**特色内容**：
- 无限工具套 (Infinity Tools)
- 虚空生物群系 (Void Biome)
- 奇点系统 (Singularity System)
- 特殊武器 (如Infinity Sword)

#### 4. 核心模块 (20文件) - `src/main/java/committee/nova/mods/avaritia/core/`
**功能定位**：项目的核心业务逻辑和数据管理
**核心子模块**：
- `singularity/` - 奇点数据管理
- `chest/` - 无限箱子系统
- `channel/` - 通信管道

**关键组件**：
- `SingularityDataManager.java` - 奇点数据管理器
- 使用标准Minecraft数据包系统
- 支持动态重载和条件加载

#### 5. 初始化模块 (109文件) - `src/main/java/committee/nova/mods/avaritia/init/`
**功能定位**：项目初始化、注册和配置
**核心子模块**：
- `registry/` - 所有游戏对象注册
- `config/` - 配置文件管理
- `data/` - 数据生成系统
- `handler/` - 事件处理器
- `mixins/` - Mixin注入
- `test/` - 测试系统

**注册系统**：
- `ModItems.java` - 物品注册 (175+物品)
- `ModBlocks.java` - 方块注册
- `ModEntities.java` - 生物注册
- `ModTileEntities.java` - 方块实体注册
- `ModCreativeModeTabs.java` - 创造模式标签页

#### 6. 工具模块 (9文件) - `src/main/java/committee/nova/mods/avaritia/util/`
**功能定位**：通用工具类和常量定义

## 🎯 项目入口点

### 主入口
- **类名**：`committee.nova.mods.avaritia.Avaritia`
- **文件**：`src/main/java/committee/nova/mods/avaritia/Avaritia.java`
- **功能**：模组主入口，处理通用初始化

### 客户端入口
- **类名**：`committee.nova.mods.avaritia.client.AvaritiaForgeClient`
- **文件**：`src/main/java/committee/nova/mods/avaritia/client/AvaritiaForgeClient.java`
- **功能**：客户端专属初始化

## 🔗 模块间依赖关系

```
Avaritia (主入口)
├── API模块 (提供接口)
├── 客户端模块 (依赖API)
├── 通用模块 (依赖API)
├── 核心模块 (依赖通用模块)
└── 初始化模块 (协调所有模块)
    └── 工具模块 (被所有模块依赖)
```

## 📋 全局开发规范

### 代码规范
- **包命名**：`committee.nova.mods.avaritia.{模块名}`
- **编码标准**：UTF-8
- **代码风格**：遵循Minecraft/Forge约定
- **文档要求**：类和方法必须有Javadoc注释

### 文件结构规范
- **资源文件**：`src/main/resources/assets/avaritia/`
- **数据包**：`src/main/resources/data/avaritia/`
- **测试代码**：`src/test/java/`

### 注册模式
- **DeferredRegister模式**：所有游戏对象使用延迟注册
- **RegistryObject包装**：统一使用RegistryObject包装器
- **命名规范**：使用ResourceLocation统一命名

### 配置系统
- **配置文件**：`config/avaritia.toml`
- **ModConfig类**：`committee.nova.mods.avaritia.init.config.ModConfig`
- **同步机制**：支持客户端-服务端配置同步

### 网络通信
- **网络包**：使用Forge网络系统
- **频道管理**：`core/channel/`目录
- **数据包**：支持JSON数据包系统

## 📚 相关文档

- **README**：[README.md](./README.md) - 项目介绍和下载
- **中文说明**：[README_CN.md](./README_CN.md) - 中文文档
- **更新日志**：[CHANGELOG.md](./CHANGELOG.md) - 版本更新记录
- **Wiki**：[wiki/](./wiki/) - 详细使用指南

## 🔧 开发工具配置

### 构建配置
- **主构建文件**：`build.gradle`
- **Gradle属性**：`gradle.properties`
- **设置文件**：`settings.gradle`

### IDE配置
- **.idea/** - IntelliJ IDEA配置
- **.gradle/** - Gradle缓存目录
- **out/** - 编译输出目录

## 📊 项目统计信息

- **总代码行数**：约 25,000+ 行Java代码
- **Java文件总数**：514个
- **包结构深度**：4层 (committee.nova.mods.avaritia.*)
- **支持的游戏版本**：Minecraft 1.20.1
- **支持的模组加载器**：Forge 47.3.0+

## 🎮 游戏内容预览

### 核心物品
- **无限系列**：Infinity Sword, Infinity Pickaxe, Infinity Axe等
- **终极装备**：Infinity Armor套
- **特殊物品**：Endest Pearl, Infinity Clock
- **合成材料**：Cosmic Meatballs, Neutronium等

### 游戏机制
- **奇点系统**：通过JSON自定义奇点物品
- **无限合成**：使用9x9工作台进行大型合成
- **虚空维度**：独特的生物群系和环境
- **大型机械**：压缩器、收集器等自动化设备

---

*最后更新时间：2025-10-26*
*AI上下文索引版本：v1.0*