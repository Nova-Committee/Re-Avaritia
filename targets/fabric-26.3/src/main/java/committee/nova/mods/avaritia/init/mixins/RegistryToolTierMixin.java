package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：Tier 被 ToolMaterial record 取代，DiggerItem 的
 * 工具等级体系改变（isCorrectToolForDrops 逻辑随 ToolMaterial 重写）。
 * 工具等级限制需改由物品属性/组件层实现。
 * 条目已从 avaritia.mixins.json 移除，保留文件仅作记录。
 */
public abstract class RegistryToolTierMixin {
}
