package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：PlayerRenderer 更名为 AvatarRenderer，鞘翅层由 InfinityArmorRender
 * （InfinityArmorModel 翅膀掩码体系）统一处理，独立 InfinityElytraLayer 已删除。
 * 玩家层注入由 ClientPlayerRendererMixin 承担。条目已从 avaritia.mixins.json 移除。
 */
public abstract class PlayerRendererMixin {
}
