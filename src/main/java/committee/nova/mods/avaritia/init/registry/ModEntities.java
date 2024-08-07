package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.common.entity.*;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Static.MOD_ID);


    public static final RegistryObject<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "immortal").toString()));

    public static final RegistryObject<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "enderst_pearl").toString()));

    public static final RegistryObject<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(new ResourceLocation(Static.MOD_ID, "gaping_void").toString()));

    public static final RegistryObject<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_arrow").toString()));

    public static final RegistryObject<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_sub_arrow").toString()));

    public static final RegistryObject<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .fireImmune()
                    .noSummon()
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "trace_arrow").toString()));

    public static final RegistryObject<EntityType<InfinityGolem>> INFINITY_GOLEM = ENTITIES.register("infinity_golem",
            () -> EntityType.Builder.of(InfinityGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "infinity_golem").toString()));

    public static final RegistryObject<EntityType<FireBallEntity>> FIRE_BALL = ENTITIES.register("fire_ball",
            () -> EntityType.Builder.of(FireBallEntity::new, MobCategory.MISC)
                    .setTrackingRange(256)
                    .setUpdateInterval(10)
                    .build(new ResourceLocation(Static.MOD_ID, "fire_ball").toString()));

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
    }

}
