package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.*;
import committee.nova.mods.avaritia.common.entity.ball.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册模组中的所有实体类型。
 */
public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Const.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ImmortalItemEntity>> IMMORTAL = ENTITY_TYPES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(key("immortal")));

    public static final DeferredHolder<EntityType<?>, EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITY_TYPES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(key("enderst_pearl")));

    public static final DeferredHolder<EntityType<?>, EntityType<GapingVoidEntity>> GAPING_VOID = ENTITY_TYPES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(key("gaping_void")));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITY_TYPES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("heaven_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<NeutronArrowEntity>> NEUTRON_ARROW = ENTITY_TYPES.register("neutron_arrow",
            () -> EntityType.Builder.<NeutronArrowEntity>of(NeutronArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("neutron_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITY_TYPES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("heaven_sub_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<ExplosionsArrowEntity>> EXPLOSIONS_ARROW = ENTITY_TYPES.register("explosions_arrow",
            () -> EntityType.Builder.<ExplosionsArrowEntity>of(ExplosionsArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("explosions_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<BurningArrowEntity>> BURNING_ARROW = ENTITY_TYPES.register("burning_arrow",
            () -> EntityType.Builder.<BurningArrowEntity>of(BurningArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("burning_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITY_TYPES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .setShouldReceiveVelocityUpdates(true)
                    .build(key("trace_arrow")));

    public static final DeferredHolder<EntityType<?>, EntityType<FireBallEntity>> FIRE_BALL = ENTITY_TYPES.register("fire_ball",
            () -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC)
                    .setTrackingRange(256)
                    .setUpdateInterval(10)
                    .build(key("fire_ball")));

    public static final DeferredHolder<EntityType<?>, EntityType<BurningBallEntity>> BURNING_BALL = ENTITY_TYPES.register("burning_ball",
            () -> EntityType.Builder.of(BurningBallEntity::new, MobCategory.MISC)
                    .setTrackingRange(256)
                    .setUpdateInterval(10)
                    .build(key("burning_ball")));

    public static final DeferredHolder<EntityType<?>, EntityType<BladeSlashEntity>> BLADE_SLASH = ENTITY_TYPES.register("blade_slash",
            () -> EntityType.Builder.<BladeSlashEntity>of(BladeSlashEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .build(key("blade_slash")));

    public static final DeferredHolder<EntityType<?>, EntityType<SunProEntity>> SUN_PRO = ENTITY_TYPES.register("sun_pro",
            () -> EntityType.Builder.<SunProEntity>of(SunProEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .build(key("sun_pro")));

    public static final DeferredHolder<EntityType<?>, EntityType<RainProEntity>> RAIN_PRO = ENTITY_TYPES.register("rain_pro",
            () -> EntityType.Builder.<RainProEntity>of(RainProEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .build(key("rain_pro")));

    public static final DeferredHolder<EntityType<?>, EntityType<StormProEntity>> STORM_PRO = ENTITY_TYPES.register("storm_pro",
            () -> EntityType.Builder.<StormProEntity>of(StormProEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .build(key("storm_pro")));

    public static final DeferredHolder<EntityType<?>, EntityType<AcceleratorDisplayEntity>> ACCELERATOR_DISPLAY = ENTITY_TYPES.register("accelerator_display",
            () -> EntityType.Builder.<AcceleratorDisplayEntity>of(AcceleratorDisplayEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .build(key("accelerator_display")));

    public static final DeferredHolder<EntityType<?>, EntityType<TNTProEntity>> TNT_PRO = ENTITY_TYPES.register("tnt_pro",
            () -> EntityType.Builder.<TNTProEntity>of(TNTProEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .build(key("tnt_pro")));

    public static final DeferredHolder<EntityType<?>, EntityType<InfinityThrownTrident>> INFINITY_THROWN_TRIDENT = ENTITY_TYPES.register("infinity_thrown_trident",
            () -> EntityType.Builder.<InfinityThrownTrident>of(InfinityThrownTrident::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .fireImmune()
                    .build(key("infinity_thrown_trident")));

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Const.MOD_ID, name));
    }
}
