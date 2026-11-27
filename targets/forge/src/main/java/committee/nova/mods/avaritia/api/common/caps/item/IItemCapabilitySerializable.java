package committee.nova.mods.avaritia.api.common.caps.item;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/3/6 13:55
 * @Description:
 */
public interface IItemCapabilitySerializable extends INBTSerializable<Tag> {
    String getStorageKey();
}
