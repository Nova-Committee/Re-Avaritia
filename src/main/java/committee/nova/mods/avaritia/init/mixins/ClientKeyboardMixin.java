package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：UiInspector.onKey 改经 ScreenKeyboardEvents（allowKeyPress）自注册，
 * 输入层由 GLFW 切换为 SDL3，无需再钩 KeyboardHandler.keyPress(long,int,int,int,int)。
 * 条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientKeyboardMixin {
}
