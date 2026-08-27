package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.common.component.ClusterContainerContents;
import committee.nova.mods.avaritia.common.component.CrystalSpearTarget;
import committee.nova.mods.avaritia.common.component.InfinityContainerContents;
import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import committee.nova.mods.avaritia.init.registry.modes.InfinityMode;
import committee.nova.mods.avaritia.init.registry.modes.ToolMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
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
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Const.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClusterContainerContents>> CLUSTER_CONTAINER = DATA_COMPONENTS.register("cluster_container", () -> DataComponentType.<ClusterContainerContents>builder().persistent(ClusterContainerContents.CODEC).networkSynchronized(ClusterContainerContents.STREAM_CODEC).cacheEncoding().build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityChestReference>> INFINITY_CHEST_REFERENCE = DATA_COMPONENTS.register(
            "infinity_chest_reference",
            () -> DataComponentType.<InfinityChestReference>builder()
                    .persistent(InfinityChestReference.CODEC)
                    .networkSynchronized(InfinityChestReference.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE = registerBoolean("active");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> TOOL_FILTERS = registerTag("tool_filters");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CrystalSpearTarget>> CRYSTAL_SPEAR_TARGET =
            DATA_COMPONENTS.register("crystal_spear_target",
                    () -> DataComponentType.<CrystalSpearTarget>builder()
                            .persistent(CrystalSpearTarget.CODEC)
                            .networkSynchronized(CrystalSpearTarget.STREAM_CODEC)
                            .cacheEncoding()
                            .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_CREATIVE_TAB_ICON =
            DATA_COMPONENTS.register("is_creative_tab_icon",
                    () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> SINGULARITY_ID = DATA_COMPONENTS.register("singularity_id",
            () -> DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfinityContainerContents>> MATTER_CLUSTER = DATA_COMPONENTS.register("matter_cluster",
            () -> DataComponentType.<InfinityContainerContents>builder().persistent(InfinityContainerContents.CODEC).networkSynchronized(InfinityContainerContents.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> NEUTRON_RING_INVENTORY = DATA_COMPONENTS.register("neutron_ring_inventory",
            () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC
                            .validate(contents -> contents.getSlots() > 81 ? DataResult.error(() -> "The neutron ring cannot have more than two items stored") : DataResult.success(contents))
                    )
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
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
