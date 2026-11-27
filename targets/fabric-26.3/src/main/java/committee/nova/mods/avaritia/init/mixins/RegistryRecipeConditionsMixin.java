package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：RecipeManager 不再是重载监听器，apply(Map, ...) 已删除，
 * 配方改走动态注册表加载（RecipeManager.finalizeRecipeLoading）。
 * 条件配方处理需迁至注册表加载钩子。
 * 条目已从 avaritia.mixins.json 移除，保留文件仅作记录。
 */
public abstract class RegistryRecipeConditionsMixin {
}
