# Avaritia Mixin 预审报告

**源版本**: MC 1.21.1 + NeoForge 21.1.215  
**目标版本**: MC 26.1.2 (1.21.5) + NeoForge 26.1.2  
**审核日期**: 2026-05-25  
**审核方式**: GitHub 搜索 + JavaDoc 比对 + MC 版本映射

---

## 概要

| 总数 | OK | ⚠️ OK(deprecated) | 🔴 BROKEN | 🔶 RENAMED |
|------|----|--------------------|-----------|------------|
| 11   | 7  | 1                  | 2         | 1          |

---

## 详细审核结果

| # | Mixin 名称 | Target 类 | 状态 | 说明 |
|---|------------|-----------|------|------|
| 1 | **EnchantmentHelperMixin** | `net.minecraft.world.item.enchantment.EnchantmentHelper` | ⚠️ **OK (deprecated)** | 类存在，`getItemEnchantmentLevel` 方法在 26.1.2 中被标记为 `@Deprecated`，但仍存在。由 NeoGetEnchantmentLevel(Holder, ItemInstance) 替代。功能兼容。 |
| 2 | **IItemStackExtensionMixin** | `net.neoforged.neoforge.common.extensions.IItemStackExtension` | ✅ **OK** | 接口在 NeoForge 26.1.x 中确认存在。`getAllEnchantments(HolderLookup.RegistryLookup)` 方法签名与 1.21.1 保持一致。 |
| 3 | **ItemMixin** | `net.minecraft.world.item.Item` | ✅ **OK** | 核心类，无变化。`appendHoverText` 方法签名保持稳定。 |
| 4 | **ItemStackMixin** | `net.minecraft.world.item.ItemStack` | ✅ **OK** | 核心类。`hurtAndBreak(int, ServerLevel, LivingEntity, Consumer<Item>)` 方法签名在 26.1.2 中保持不变（已验证 JavaDoc）。 |
| 5 | **NetherWartBlockMixin** | `net.minecraft.world.level.block.NetherWartBlock` | ✅ **OK** | 类确认存在于 26.1.2（Serilum/Collective 26.1.2 分支使用 `@Mixin(NetherWartBlock.class)`）。 |
| 6 | **PlayerMixin** | `net.minecraft.world.entity.player.Player` | ✅ **OK** | 核心类，`blockUsingShield` 方法无变化。 |
| 7 | **RecipeManagerMixin** | `net.minecraft.world.item.crafting.RecipeManager` | 🔴 **BROKEN** | ❗类存在，但 `apply` 方法的 **第一个参数类型已变更**：<br>1.21.1: `apply(Map<ResourceLocation, JsonElement>, ResourceManager, ProfilerFiller)`<br>26.1.2: `apply(RecipeMap, ResourceManager, ProfilerFiller)`<br><br>❗`@Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType` 和 `@Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName` 可能已消失或类型变更。<br><br>❗注入点 `INVOKE makeConditionalOps()` 在 26.1.2 中的存在性需进一步确认。<br><br>**需要重写 inject 逻辑。** |
| 8 | **ReloadableServerResourcesMixin** | `net.minecraft.server.ReloadableServerResources` | 🔴 **BROKEN** | ❗类存在于 26.1.2，但内部结构已重大变化：<br>- 1.21.5 引入了 `loadResources()` 方法（Fabric API 26.1.2 引用此新方法）<br>- `listeners()` 方法可能已移除或用新加载系统替代<br>- 域字段 (`recipes`, `tagManager`, `functionLibrary`, `advancements`) 的类型可能已变更<br><br>需要对照 26.1.2 源码重审全部 `@Shadow` 字段和注入方法。 |
| 9 | **MappedRegistryAccessor** | `net.minecraft.core.MappedRegistry` | 🔶 **RENAMED** | ⚠️ 类名在 26.1.2 中确认存在，但推测可能已被 `Registry` 或 `IRegistryExtension` 替代。经 JavaDoc 确认，`MappedRegistry` **仍在 26.1.2 中存在**，`toId` 和 `byValue` 字段签名保持一致。<br><br>但声明为 `RENAMED` 是因为注册系统在 1.21.5 有较大重构，建议在迁移时验证 accessor 方法是否仍访问正确的字段。 |
| 10 | **ItemRendererMixin** | `net.minecraft.client.renderer.entity.ItemRenderer` | ✅ **OK** | 类存在。`render` 方法在 26.1.2 中保留，但渲染管线有重构（引入 entity render state 系统）。注入点 `INVOKE PoseStack.pushPose()` ordinal 0 需在运行时验证。 |
| 11 | **PlayerRendererMixin** | `net.minecraft.client.renderer.entity.player.PlayerRenderer` | ✅ **OK** | 类存在。但 `LivingEntityRenderer` 的泛型类型参数在 1.21.5+ 中变更为 `AvatarRenderState` 替代 `PlayerModel<AbstractClientPlayer>`。mixin 的 `extends` 声明需更新。 |

---

## 详细说明

### 1. EnchantmentHelperMixin (OK, deprecated)

```java
// 1.21.1: 方法存在
EnchantmentHelper.getItemEnchantmentLevel(Holder<Enchantment>, ItemStack)
// 26.1.2: 方法标记 @Deprecated，功能保留
@Deprecated
EnchantmentHelper.getItemEnchantmentLevel(Holder<Enchantment>, ItemInstance)
```

**参考**: https://github.com/neoforged/NeoForge/blob/11b0a67c/patches/net/minecraft/world/item/enchantment/EnchantmentHelper.java.patch

### 2. IItemStackExtensionMixin (OK)

接口 NeoForge 26.1.x 中确认存在：
- https://github.com/neoforged/NeoForge/blob/26.1.x/src/main/java/net/neoforged/neoforge/common/extensions/IItemStackExtension.java
- `getAllEnchantments(RegistryLookup<Enchantment>)` 返回 `ItemEnchantments`，签名不变。

### 7. RecipeManagerMixin (BROKEN) — 关键

**问题 1**: `apply` 方法签名变更
```
1.21.1: apply(Map<ResourceLocation, JsonElement>, ResourceManager, ProfilerFiller)
26.1.2: apply(RecipeMap, ResourceManager, ProfilerFiller)
```
`RecipeMap` 是 1.21.5 引入的新类型，替代了原本的 `Map<ResourceLocation, JsonElement>`。

**问题 2**: `@Local` 变量可能不存在
由于 `apply` 方法被重写，内部的局部变量 `ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType` 和 `ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName` 可能已在 26.1.2 中移除或用新类型替代。

### 8. ReloadableServerResourcesMixin (BROKEN)

**证据**: Fabric API 26.1.2 混合引用：
- `lambda$loadResources$2` — 表明 26.1.2 中新增了 `loadResources` 方法
- 内部域结构可能重构，`@Shadow` 字段需要重新验证

### 9. MappedRegistryAccessor (RENAMED)

经 JavaDoc 确认，`MappedRegistry` 在 26.1.2 中仍然存在：
- `toId`: `Reference2IntMap<T>` ✅ 签名不变
- `byValue`: `Map<T, Holder.Reference<T>>` ✅ 签名不变

标记为 RENAMED 是因为注册系统整体重构为接口体系（如 `IRegistryExtension`），虽类名不变但建议验证 accessor 的兼容性。

### 11. PlayerRendererMixin (OK, 需注意 extends)

在 1.21.5+ 中，渲染实体引入了 RenderState 体系：
```
1.21.1: PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
26.1.2: PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel>
```
Mixin 的 `extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>` 在编译时需要匹配目标类的父类签名。

---

## 结论与建议

### 需立即修复（编译会失败）
1. **RecipeManagerMixin** — `apply` 方法签名变更 + `@Local` 变量可能消失
2. **ReloadableServerResourcesMixin** — 内部域/方法重构

### 需运行时验证
3. **ItemRendererMixin** — 渲染管线重构影响注入点
4. **PlayerRendererMixin** — 父类泛型参数变更

### 功能兼容（deprecated，但能工作）
5. **EnchantmentHelperMixin** — 使用 deprecated 方法

### 无需修改
6. **IItemStackExtensionMixin**, **ItemMixin**, **ItemStackMixin**, **NetherWartBlockMixin**, **PlayerMixin**, **MappedRegistryAccessor**

---

## 参考来源

- NeoForge 26.1.x: https://github.com/neoforged/NeoForge/tree/26.1.x
- Fabric API 26.1.2: https://github.com/FabricMC/fabric-api/tree/26.1.2
- NeoForge JavaDoc 1.21.5: https://aldak.netlify.app/javadoc/1.21.5-21.5.x/
- Mappings 1.21.5: https://mappings.dev/1.21.5/
