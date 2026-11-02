package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：renderWithTooltip 已删除；AvaritiaClient.inventoryRender 标记
 * 改由 ClientGuiEntityRendererMixin（GUI 内实体渲染入口）置位/复位，供 InfinityArmorModel 使用；
 * UiInspector 改经 ScreenEvents（AFTER_INIT 等）自注册。条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientScreenMixin {
}
