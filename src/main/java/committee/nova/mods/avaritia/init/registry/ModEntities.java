package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.client.render.tile.AcceleratorDisplayRenderer;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import dev.architectury.registry.registries.DeferredRegister;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Const.MOD_ID);


    public static final RegistryObject<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Const.MOD_ID, "immortal").toString()));

    public static final RegistryObject<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Const.MOD_ID, "enderst_pearl").toString()));

    public static final RegistryObject<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(new ResourceLocation(Const.MOD_ID, "gaping_void").toString()));

    public static final RegistryObject<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "heaven_arrow").toString()));

    public static final RegistryObject<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(Const.MOD_ID, "heaven_sub_arrow").toString()));

    public static final RegistryObject<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .build(new ResourceLocation(Const.MOD_ID, "trace_arrow").toString()));

    public static final RegistryObject<EntityType<InfinityGolem>> INFINITY_GOLEM = ENTITIES.register("infinity_golem",
            () -> EntityType.Builder.of(InfinityGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "infinity_golem").toString()));

    public static final RegistryObject<EntityType<FireBallEntity>> FIRE_BALL = ENTITIES.register("fire_ball",
            () -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC)
                    .clientTrackingRange(256)
                    .updateInterval(10)
                    .build(new ResourceLocation(Const.MOD_ID, "fire_ball").toString()));

    public static final RegistryObject<EntityType<BladeSlashEntity>> BLADE_SLASH = ENTITIES.register("blade_slash",
            () -> EntityType.Builder.<BladeSlashEntity>of(BladeSlashEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "blade_slash").toString()));
    public static final RegistryObject<EntityType<SunProEntity>> SUN_PRO = ENTITIES.register("sun_pro",
            () -> EntityType.Builder.of(SunProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f)
                    .updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "sun_pro").toString()));
    public static final RegistryObject<EntityType<RainProEntity>> RAIN_PRO = ENTITIES.register("rain_pro",
            () -> EntityType.Builder.of(RainProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f)
                    .updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "rain_pro").toString()));
    public static final RegistryObject<EntityType<StormProEntity>> STORM_PRO = ENTITIES.register("storm_pro",
            () -> EntityType.Builder.of(StormProEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f)
                    .updateInterval(10)
                    .fireImmune()
                    .build(new ResourceLocation(Const.MOD_ID, "storm_pro").toString()));

    public static final RegistryObject<EntityType<AcceleratorDisplayEntity>> acceleratorDisplayEntity =
            ENTITIES.register("accelerator_display", () -> EntityType.Builder.<AcceleratorDisplayEntity>of(
                            AcceleratorDisplayEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .build(new ResourceLocation("avariita", "accelerator_display").toString())
            );

    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {
        EntityRenderers.register(ModEntities.IMMORTAL.get(), ItemEntityRenderer::new);
        EntityRenderers.register(ModEntities.ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.GAPING_VOID.get(), GapingVoidRender::new);
        EntityRenderers.register(ModEntities.HEAVEN_ARROW.get(), HeavenArrowRender::new);
        EntityRenderers.register(ModEntities.HEAVEN_SUB_ARROW.get(), HeavenSubArrowRender::new);
        EntityRenderers.register(ModEntities.TRACE_ARROW.get(), TracerArrowRender::new);
        EntityRenderers.register(ModEntities.INFINITY_GOLEM.get(), InfinityGolemRenderer::new);
        EntityRenderers.register(ModEntities.FIRE_BALL.get(), FireBallRender::new);
        EntityRenderers.register(ModEntities.BLADE_SLASH.get(), BladeSlashRender::new);
        EntityRenderers.register(ModEntities.SUN_PRO.get(), SunProRender::new);
        EntityRenderers.register(ModEntities.RAIN_PRO.get(), RainProRender::new);
        EntityRenderers.register(ModEntities.STORM_PRO.get(), StormProRender::new);
        EntityRenderers.register(ModEntities.acceleratorDisplayEntity.get(), AcceleratorDisplayRenderer::new);

    }

}
