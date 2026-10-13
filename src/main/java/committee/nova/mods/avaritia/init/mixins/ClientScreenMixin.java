package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：renderWithTooltip 已删除；AvaritiaForgeClient.inventoryRender 标记随之移除
 * （InfinityArmorModel 改用纯 render state），UiInspector 改经 ScreenEvents（AFTER_INIT 等）自注册。
 * 条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientScreenMixin {
}
