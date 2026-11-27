package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.*;
import committee.nova.mods.avaritia.common.entity.ball.BurningBallEntity;
import committee.nova.mods.avaritia.common.entity.ball.FireBallEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final RegistryEntries<EntityType<?>> ENTITIES = RegistryEntries.create(BuiltInRegistries.ENTITY_TYPE, Const.MOD_ID);


    public static final RegistryEntry<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            id -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            id -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            id -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            id -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            id -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<NeutronArrowEntity>> NEUTRON_ARROW = ENTITIES.register("neutron_arrow",
            id -> EntityType.Builder.<NeutronArrowEntity>of(NeutronArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<ExplosionsArrowEntity>> EXPLOSIONS_ARROW = ENTITIES.register("explosions_arrow",
            id -> EntityType.Builder.<ExplosionsArrowEntity>of(ExplosionsArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<BurningArrowEntity>> BURNING_ARROW = ENTITIES.register("burning_arrow",
            id -> EntityType.Builder.<BurningArrowEntity>of(BurningArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            id -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<FireBallEntity>> FIRE_BALL = ENTITIES.register("fire_ball",
            id -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<BurningBallEntity>> BURNING_BALL = ENTITIES.register("burning_ball",
            id -> EntityType.Builder.of(BurningBallEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<BladeSlashEntity>> BLADE_SLASH = ENTITIES.register("blade_slash",
            id -> EntityType.Builder.<BladeSlashEntity>of(BladeSlashEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F).updateInterval(10)
                    .fireImmune()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<SunProEntity>> SUN_PRO = ENTITIES.register("sun_pro",
            id -> EntityType.Builder.<SunProEntity>of(SunProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<RainProEntity>> RAIN_PRO = ENTITIES.register("rain_pro",
            id -> EntityType.Builder.<RainProEntity>of(RainProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<StormProEntity>> STORM_PRO = ENTITIES.register("storm_pro",
            id -> EntityType.Builder.<StormProEntity>of(StormProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final RegistryEntry<EntityType<AcceleratorDisplayEntity>> ACCELERATOR_DISPLAY_ENTITY =
            ENTITIES.register("accelerator_display", id -> EntityType.Builder.<AcceleratorDisplayEntity>of(
                            AcceleratorDisplayEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );
    public static final RegistryEntry<EntityType<TNTProEntity>> TNT_PRO_ENTITY =
            ENTITIES.register("tnt_pro", id -> EntityType.Builder.<TNTProEntity>of(
                            TNTProEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );
    public static final RegistryEntry<EntityType<InfinityThrownTrident>> INFINITY_THROWN_TRIDENT =
            ENTITIES.register("infinity_thrown_trident", id -> EntityType.Builder.<InfinityThrownTrident>of(
                            InfinityThrownTrident::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );


}
