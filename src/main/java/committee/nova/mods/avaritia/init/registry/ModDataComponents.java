package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.InfinityContainerContents;
import committee.nova.mods.avaritia.common.component.ClusterContainerContents;
import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.init.registry.modes.InfinityMode;
import committee.nova.mods.avaritia.init.registry.modes.ToolMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/22 02:10
 * @Description:
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Const.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClusterContainerContents>> CLUSTER_CONTAINER = DATA_COMPONENTS.register("cluster_container", () -> DataComponentType.<ClusterContainerContents>builder().persistent(ClusterContainerContents.CODEC).networkSynchronized(ClusterContainerContents.STREAM_CODEC).cacheEncoding().build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityChestReference>> INFINITY_CHEST_REFERENCE = DATA_COMPONENTS.register("infinity_chest_reference",
            () -> DataComponentType.<InfinityChestReference>builder()
                    .persistent(InfinityChestReference.CODEC)
                    .networkSynchronized(InfinityChestReference.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE = registerBoolean("active");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> TOOL_FILTERS = registerTag("tool_filters");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_CREATIVE_TAB_ICON =
            DATA_COMPONENTS.register("is_creative_tab_icon",
                    () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SINGULARITY_ID = DATA_COMPONENTS.register("singularity_id",
            () -> DataComponentType.<ResourceLocation>builder().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityContainerContents>> MATTER_CLUSTER = DATA_COMPONENTS.register("matter_cluster",
            () -> DataComponentType.<InfinityContainerContents>builder().persistent(InfinityContainerContents.CODEC).networkSynchronized(InfinityContainerContents.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityBucketFluids>> INFINITY_BUCKET_FLUIDS = DATA_COMPONENTS.register("infinity_bucket_fluids",
            () -> DataComponentType.<InfinityBucketFluids>builder()
                    .persistent(InfinityBucketFluids.CODEC)
                    .networkSynchronized(InfinityBucketFluids.STREAM_CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityBucketCreatures>> INFINITY_BUCKET_CREATURES = DATA_COMPONENTS.register("infinity_bucket_creatures",
            () -> DataComponentType.<InfinityBucketCreatures>builder()
                    .persistent(InfinityBucketCreatures.CODEC)
                    .networkSynchronized(InfinityBucketCreatures.STREAM_CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityBucketControl>> INFINITY_BUCKET_CONTROL = DATA_COMPONENTS.register("infinity_bucket_control",
            () -> DataComponentType.<InfinityBucketControl>builder()
                    .persistent(InfinityBucketControl.CODEC)
                    .networkSynchronized(InfinityBucketControl.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NeutronRingContents>> NEUTRON_RING = DATA_COMPONENTS.register("neutron_ring",
            () -> DataComponentType.<NeutronRingContents>builder()
                    .persistent(NeutronRingContents.CODEC)
                    .networkSynchronized(NeutronRingContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );



    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolMode>> TOOL_MODE = DATA_COMPONENTS.register("tool_mode",
            () -> DataComponentType.<ToolMode>builder().persistent(ToolMode.CODEC).networkSynchronized(ToolMode.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityMode>> INFINITY_MODE = DATA_COMPONENTS.register("infinity_mode",
            () -> DataComponentType.<InfinityMode>builder().persistent(InfinityMode.CODEC).networkSynchronized(InfinityMode.STREAM_CODEC).build());

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> registerBoolean(String name){
       return DATA_COMPONENTS.register(name,
                () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build());
    }

    public static DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> registerTag(String name){
        return DATA_COMPONENTS.register(name,
                () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).cacheEncoding().build());
    }

//    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TOOL_FILTERS = DATA_COMPONENTS.register("tool_filters",
//            () -> DataComponentType.<CustomData>builder().persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).cacheEncoding().build());
}
