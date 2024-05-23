package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.entity.InfinityGolem;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static void init() {}
    public static final RegistryHolder<EntityType<?>> ENTITIES = FabricRegistry.INSTANCE.createEntitiesRegistryHolder();


    public static final Supplier<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "immortal").toString()));

    public static final Supplier<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.<EndestPearlEntity>of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "enderst_pearl").toString()));

    public static final Supplier<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(new ResourceLocation(Static.MOD_ID, "gaping_void").toString()));

    public static final Supplier<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    //.setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_arrow").toString()));

    public static final Supplier<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    //.setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_sub_arrow").toString()));

    public static final Supplier<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> EntityType.Builder.<TraceArrowEntity>of(TraceArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .fireImmune()
                    .noSummon()
                    //.setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "trace_arrow").toString()));

    public static final Supplier<EntityType<InfinityGolem>> INFINITY_GOLEM = ENTITIES.register("infinity_golem",
            () -> EntityType.Builder.<InfinityGolem>of(InfinityGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    //.setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "infinity_golem").toString()));

    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {
        EntityRendererRegistry.register(ModEntities.IMMORTAL.get(), ItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.GAPING_VOID.get(), GapingVoidRender::new);
        EntityRendererRegistry.register(ModEntities.HEAVEN_ARROW.get(), HeavenArrowRender::new);
        EntityRendererRegistry.register(ModEntities.HEAVEN_SUB_ARROW.get(), HeavenSubArrowRender::new);
        EntityRendererRegistry.register(ModEntities.TRACE_ARROW.get(), TracerArrowRender::new);
        EntityRendererRegistry.register(ModEntities.INFINITY_GOLEM.get(), InfinityGolemRenderer::new);
    }

}
