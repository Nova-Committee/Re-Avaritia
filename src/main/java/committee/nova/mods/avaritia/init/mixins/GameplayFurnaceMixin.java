package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：AbstractFurnaceBlockEntity.getFuel() 燃料表已删除，
 * 燃料值改由物品组件 DataComponents.COOKING_FUEL（CookingFuel record）定义，
 * 模组燃料物品应在物品属性中直接挂该组件（参考 NeoForge 26.3 版 ModBlocks）。
 * 条目已从 avaritia.mixins.json 移除，保留文件仅作记录。
 */
public abstract class GameplayFurnaceMixin {
}
