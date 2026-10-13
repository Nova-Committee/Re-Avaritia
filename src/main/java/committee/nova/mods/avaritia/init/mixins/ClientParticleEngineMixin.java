package committee.nova.mods.avaritia.init.mixins;

/**
 * 已在 26.3 升级中废弃：ParticleEngine 已拆分出 ParticleResources，其 register/registerProviders
 * 均为私有且 SpriteParticleRegistration 为私有嵌套接口；粒子供应商改经 Fabric
 * ParticleProviderRegistry 注册（AvaritiaModClient.onInitializeClient）。
 * 条目已从 avaritia.mixins.json 移除。
 */
public abstract class ClientParticleEngineMixin {
}
