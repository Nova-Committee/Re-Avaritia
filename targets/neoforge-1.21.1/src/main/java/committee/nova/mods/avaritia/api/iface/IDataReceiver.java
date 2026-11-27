package committee.nova.mods.avaritia.api.iface;

import net.minecraft.nbt.CompoundTag;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/23 01:46
 * @Description:
 */
public interface IDataReceiver {
    void receive(CompoundTag tag);
}
