package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.*;
import committee.nova.mods.avaritia.common.entity.ball.BurningBallEntity;
import committee.nova.mods.avaritia.common.entity.ball.FireBallEntity;
import net.minecraft.resources.ResourceLocation;
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
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Const.MOD_ID, "immortal").toString()));

    public static final RegistryEntry<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Const.MOD_ID, "enderst_pearl").toString()));

    public static final RegistryEntry<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(new ResourceLocation(Const.MOD_ID, "gaping_void").toString()));

    public static final RegistryEntry<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "heaven_arrow").toString()));

    public static final RegistryEntry<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "heaven_sub_arrow").toString()));

    public static final RegistryEntry<EntityType<NeutronArrowEntity>> NEUTRON_ARROW = ENTITIES.register("neutron_arrow",
            () -> EntityType.Builder.<NeutronArrowEntity>of(NeutronArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(Const.rl("neutron_arrow").toString()));

    public static final RegistryEntry<EntityType<ExplosionsArrowEntity>> EXPLOSIONS_ARROW = ENTITIES.register("explosions_arrow",
            () -> EntityType.Builder.<ExplosionsArrowEntity>of(ExplosionsArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "explosions_arrow").toString()));

    public static final RegistryEntry<EntityType<BurningArrowEntity>> BURNING_ARROW = ENTITIES.register("burning_arrow",
            () -> EntityType.Builder.<BurningArrowEntity>of(BurningArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "burning_arrow").toString()));

    public static final RegistryEntry<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .build(new ResourceLocation(Const.MOD_ID, "trace_arrow").toString()));

    public static final RegistryEntry<EntityType<FireBallEntity>> FIRE_BALL = ENTITIES.register("fire_ball",
            () -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(10)
                    .build(new ResourceLocation(Const.MOD_ID, "fire_ball").toString()));

    public static final RegistryEntry<EntityType<BurningBallEntity>> BURNING_BALL = ENTITIES.register("burning_ball",
            () -> EntityType.Builder.of(BurningBallEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(10)
                    .build(new ResourceLocation(Const.MOD_ID, "burning_ball").toString()));

    public static final RegistryEntry<EntityType<BladeSlashEntity>> BLADE_SLASH = ENTITIES.register("blade_slash",
            () -> EntityType.Builder.<BladeSlashEntity>of(BladeSlashEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F).updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "blade_slash").toString()));
    public static final RegistryEntry<EntityType<SunProEntity>> SUN_PRO = ENTITIES.register("sun_pro",
            () -> EntityType.Builder.<SunProEntity>of(SunProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "sun_pro").toString()));
    public static final RegistryEntry<EntityType<RainProEntity>> RAIN_PRO = ENTITIES.register("rain_pro",
            () -> EntityType.Builder.<RainProEntity>of(RainProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "rain_pro").toString()));
    public static final RegistryEntry<EntityType<StormProEntity>> STORM_PRO = ENTITIES.register("storm_pro",
            () -> EntityType.Builder.<StormProEntity>of(StormProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "storm_pro").toString()));

    public static final RegistryEntry<EntityType<AcceleratorDisplayEntity>> ACCELERATOR_DISPLAY_ENTITY =
            ENTITIES.register("accelerator_display", () -> EntityType.Builder.<AcceleratorDisplayEntity>of(
                            AcceleratorDisplayEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .build(new ResourceLocation("avaritia", "accelerator_display").toString())
            );
    public static final RegistryEntry<EntityType<TNTProEntity>> TNT_PRO_ENTITY =
            ENTITIES.register("tnt_pro", () -> EntityType.Builder.<TNTProEntity>of(
                            TNTProEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .build(new ResourceLocation("avaritia", "tnt_pro").toString())
            );
    public static final RegistryEntry<EntityType<InfinityThrownTrident>> INFINITY_THROWN_TRIDENT =
            ENTITIES.register("infinity_thrown_trident", () -> EntityType.Builder.<InfinityThrownTrident>of(
                            InfinityThrownTrident::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .build(new ResourceLocation("avaritia", "infinity_thrown_trident").toString())
            );


}
