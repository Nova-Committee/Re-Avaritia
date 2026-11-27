package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：自定义按键改由 Fabric key-binding API 注册
 * （AvaritiaClient.register() → KeyBindingHelper.registerKeyBinding），
 * 不再向 Options.keyMappings 手工追加数组元素。条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientOptionsMixin {
}
