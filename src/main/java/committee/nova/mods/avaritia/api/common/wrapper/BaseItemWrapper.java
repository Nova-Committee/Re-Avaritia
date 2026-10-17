package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/1 02:48
 * @Description:
 */
public interface BaseItemWrapper extends ModifiableItemHandler {
    @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider);

    void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag tag);
}