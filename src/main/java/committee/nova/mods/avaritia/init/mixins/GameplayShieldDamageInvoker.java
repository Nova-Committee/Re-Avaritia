package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：LivingEntity.hurtCurrentlyUsedShield(float) 已删除，
 * 盾牌伤害改由 BlocksAttacks 组件（hurtBlockingItem）处理；
 * 无尽盾的不消耗由 ItemStackMixin 拦截 hurtAndBreak 覆盖，
 * 反射回调迁移至 LivingEntityShieldMixin 对 applyItemBlocking 的 RETURN 注入。
 * 条目已从 avaritia.mixins.json 移除，保留文件仅作记录。
 */
public interface GameplayShieldDamageInvoker {
}
