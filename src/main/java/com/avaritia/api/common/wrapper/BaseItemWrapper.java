package com.avaritia.api.common.wrapper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/1 02:48
 * @Description:
 */
@SuppressWarnings("removal")
public interface BaseItemWrapper extends IItemHandlerModifiable, ValueIOSerializable {
    @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider);

    void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt);
}
