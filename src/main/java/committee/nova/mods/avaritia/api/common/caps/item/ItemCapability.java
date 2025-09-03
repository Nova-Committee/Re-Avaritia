package committee.nova.mods.avaritia.api.common.caps.item;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.world.item.ItemStack;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/6 13:53
 * @Description:
 */
public abstract class ItemCapability<CAP> {
    private ItemCapabilitiesWrapper wrapper;

    public ItemCapability() {
    }

    public void setWrapper(ItemCapabilitiesWrapper wrapper) {
        if (this.wrapper == null) {
            this.wrapper = wrapper;
        }

    }

    public abstract Capability<CAP> getCapability();

    public abstract LazyOptional<CAP> getLazyCapability();

    protected ItemStack getStack() {
        return this.wrapper.getItemStack();
    }

    protected CAP getItem() {
        return (CAP) this.getStack().getItem();
    }
}

