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
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final LazyRegistrar<EntityType<?>> ENTITIES = LazyRegistrar.create(BuiltInRegistries.ENTITY_TYPE, Static.MOD_ID);
    public static void init() {
        Static.LOGGER.info("Registering Mod Entities...");
        ENTITIES.register();
    }


    public static final RegistryObject<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> FabricEntityTypeBuilder.<ImmortalItemEntity>create(MobCategory.MISC, ImmortalItemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .build()
            );

    public static final RegistryObject<EntityType<EndestPearlEntity>> ENDER_PEARL = ENTITIES.register("enderst_pearl",
            () -> FabricEntityTypeBuilder.<EndestPearlEntity>create(MobCategory.MISC, EndestPearlEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .build()
    );

    public static final RegistryObject<EntityType<GapingVoidEntity>> GAPING_VOID = ENTITIES.register("gaping_void",
            () -> FabricEntityTypeBuilder.<GapingVoidEntity>create(MobCategory.MISC, GapingVoidEntity::new)
                    .build()
    );

    public static final RegistryObject<EntityType<HeavenArrowEntity>> HEAVEN_ARROW = ENTITIES.register("heaven_arrow",
            () -> FabricEntityTypeBuilder.<HeavenArrowEntity>create(MobCategory.MISC, HeavenArrowEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(20)
                    .forceTrackedVelocityUpdates(true)
                    .build()
            );

    public static final RegistryObject<EntityType<HeavenSubArrowEntity>> HEAVEN_SUB_ARROW = ENTITIES.register("heaven_sub_arrow",
            () -> FabricEntityTypeBuilder.<HeavenSubArrowEntity>create(MobCategory.MISC, HeavenSubArrowEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(20)
                    .forceTrackedVelocityUpdates(true)
                    .build()
    );

    public static final RegistryObject<EntityType<TraceArrowEntity>> TRACE_ARROW = ENTITIES.register("trace_arrow",
            () -> FabricEntityTypeBuilder.<TraceArrowEntity>create(MobCategory.MISC, TraceArrowEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(20)
                    .forceTrackedVelocityUpdates(true)
                    .build()
    );

    public static final RegistryObject<EntityType<InfinityGolem>> INFINITY_GOLEM = ENTITIES.register("infinity_golem",
            () -> FabricEntityTypeBuilder.<InfinityGolem>create(MobCategory.MISC, InfinityGolem::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 2.7f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(20)
                    .forceTrackedVelocityUpdates(true)
                    .build()
    );

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
