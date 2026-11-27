package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.nbt.CompoundTag;
import committee.nova.mods.avaritia.api.common.wrapper.ModifiableItemHandler;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/1 02:48
 * @Description:
 */
public interface BaseItemWrapper extends ModifiableItemHandler {
    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
