package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：ItemStack.of(CompoundTag)、getOrCreateTag() 与 ForgeCaps
 * 兼容层均已删除，物品序列化走 CODEC / ValueInput / ValueOutput。
 * 中子戒指旧存档数据的迁移需改由 DataFixer 或组件读取逻辑处理。
 * 条目已从 avaritia.mixins.json 移除，保留文件仅作记录。
 */
public abstract class DimensionItemStackMixin {
}
