package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Static.MOD_ID);


    public static final DeferredHolder<EntityType<?>, EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(Static.rl( "immortal").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(Static.rl("enderst_pearl").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(Static.rl("gaping_void").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Static.rl("heaven_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Static.rl("heaven_sub_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Static.rl("trace_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<InfinityGolem>> INFINITY_GOLEM = ENTITIES.register("infinity_golem",
            () -> EntityType.Builder.of(InfinityGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Static.rl("infinity_golem").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FireBallEntity>> FIRE_BALL = ENTITIES.register("fire_ball",
            () -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC)
                    .setTrackingRange(256)
                    .setUpdateInterval(10)
                    .build(Static.rl("fire_ball").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BladeSlashEntity>> BLADE_SLASH = ENTITIES.register("blade_slash",
            () -> EntityType.Builder.<BladeSlashEntity>of(BladeSlashEntity::new, MobCategory.MISC)
                    .sized(3F, 0.5F)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .build(Static.rl("blade_slash").toString()));

    @OnlyIn(Dist.CLIENT)
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
    }

}
