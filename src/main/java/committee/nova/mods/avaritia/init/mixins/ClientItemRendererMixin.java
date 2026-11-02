package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：net.minecraft.client.renderer.entity.ItemRenderer 已删除。
 * 无尽盾牌/无尽胸甲的物品形态渲染改由 SpecialModelRenderers（InfinityShieldRender.Unbaked /
 * InfinityChestItemRender.Unbaked，经 ItemModels.ID_MAPPER 注册的自定义 item model type）驱动。
 * 条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientItemRendererMixin {
}
