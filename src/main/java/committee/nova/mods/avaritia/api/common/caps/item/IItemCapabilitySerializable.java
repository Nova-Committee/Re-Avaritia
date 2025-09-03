package committee.nova.mods.avaritia.api.common.caps.item;

import io.github.fabricators_of_create.porting_lib.core.util.INBTSerializable;
import net.minecraft.nbt.Tag;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/6 13:55
 * @Description:
 */
public interface IItemCapabilitySerializable extends INBTSerializable<Tag> {
    String getStorageKey();
}
