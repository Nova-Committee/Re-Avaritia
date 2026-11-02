package committee.nova.mods.avaritia.init.registry;

/**
 * 26.3 附魔改为 ResourceKey + 数据驱动 BootstrapContext 模式（原版 Enchantments.java 与 Neo 版 ModEnchants 均如此），
 * 不再存在 BuiltInRegistries.ENCHANTMENT 运行时注册表。
 * 本模组自定义附魔已裁剪；如需恢复，参照 Re-Avaritia-neo-26.3 的 ModEnchants.bootstrap(BootstrapContext) 用 datagen 注册。
 */
public final class ModEnchants {
    private ModEnchants() {}
}
