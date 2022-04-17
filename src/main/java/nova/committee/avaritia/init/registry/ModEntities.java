package nova.committee.avaritia.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.entity.EndestPearlEntity;
import nova.committee.avaritia.common.entity.GapingVoidEntity;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:52
 * Version: 1.0
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Static.MOD_ID);


    public static final RegistryObject<EntityType<ImmortalItemEntity>> IMMORTAL = ENTITIES.register("immortal",
            () -> EntityType.Builder.of(ImmortalItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "immortal").toString()));

    public static final RegistryObject<EntityType<EndestPearlEntity>> EnderstPearl = ENTITIES.register("enderst_pearl",
            () -> EntityType.Builder.of(EndestPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .build(new ResourceLocation(Static.MOD_ID, "enderst_pearl").toString()));

    public static final RegistryObject<EntityType<GapingVoidEntity>> GapingVoid = ENTITIES.register("gaping_void",
            () -> EntityType.Builder.of(GapingVoidEntity::new, MobCategory.MISC).sized(0.1f, 0.1f)
                    .build(new ResourceLocation(Static.MOD_ID, "gaping_void").toString()));

}
