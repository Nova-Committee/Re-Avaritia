package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：GameRenderer 无 reloadShaders 且 Screen 渲染管线重构，
 * PortableScreenLayers 独立屏幕层体系已删除（可移植屏幕自管理），
 * AvaritiaForgeClient.onRenderTickStart 一并移除；AvaritiaShaders 仅注册期加载。
 * 条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientGameRendererMixin {
}
