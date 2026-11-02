package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：net.minecraft.client.renderer.entity.ItemRenderer 已删除，
 * 物品渲染改由 item model 体系（ItemModels.ID_MAPPER + SpecialModelRenderers）承载，
 * PerspectiveModel 变换走新的自定义 item model codec。条目已从 avaritia.mixins.json 移除。
 */
public abstract class ItemRendererMixin {
}
