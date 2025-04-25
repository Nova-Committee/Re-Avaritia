package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Static;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/22 02:10
 * @Description:
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Static.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INFINITY_PICKAXE_HAMMER = DATA_COMPONENTS.register("infinity_pickaxe_hammer",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INFINITY_SHOVEL_DESTROYER = DATA_COMPONENTS.register("infinity_shovel_destroyer",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INFINITY_BOW_TRACER = DATA_COMPONENTS.register("infinity_bow_tracer",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> INFINITY_MODE = DATA_COMPONENTS.register("infinity_mode",
            () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).cacheEncoding().build());
}
