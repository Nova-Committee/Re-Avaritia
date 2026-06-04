package com.avaritia.api.common.wrapper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/1 02:48
 * @Description:
 */
@SuppressWarnings("removal")
public interface BaseItemWrapper extends ResourceHandler<ItemResource>, IndexModifier<ItemResource>, ValueIOSerializable {
    @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider);

    void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt);
}
