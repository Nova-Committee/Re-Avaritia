package nova.committee.avaritia.init.registry;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.client.render.entity.GapingVoidRender;
import nova.committee.avaritia.client.render.entity.HeavenArrowRender;
import nova.committee.avaritia.client.render.entity.HeavenSubArrowRender;
import nova.committee.avaritia.common.entity.*;

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

    public static final RegistryObject<EntityType<EndestPearlEntity>> EnderPearl = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "enderst_pearl").toString()));

    public static final RegistryObject<EntityType<GapingVoidEntity>> GapingVoid = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.<GapingVoidEntity>of(GapingVoidEntity::new, MobCategory.MISC)
                    .build(new ResourceLocation(Static.MOD_ID, "gaping_void").toString()));

    public static final RegistryObject<EntityType<HeavenArrowEntity>> HeavenArrow = ENTITIES.register("heaven_arrow",
            () -> EntityType.Builder.<HeavenArrowEntity>of(HeavenArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_arrow").toString()));

    public static final RegistryObject<EntityType<HeavenSubArrowEntity>> HeavenSubArrow = ENTITIES.register("heaven_sub_arrow",
            () -> EntityType.Builder.<HeavenSubArrowEntity>of(HeavenSubArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Static.MOD_ID, "heaven_sub_arrow").toString()));


    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        EntityRenderers.register(ModEntities.EnderPearl.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.GapingVoid.get(), GapingVoidRender::new);
        EntityRenderers.register(ModEntities.HeavenArrow.get(), HeavenArrowRender::new);
        EntityRenderers.register(ModEntities.HeavenSubArrow.get(), HeavenSubArrowRender::new);
    }

}
