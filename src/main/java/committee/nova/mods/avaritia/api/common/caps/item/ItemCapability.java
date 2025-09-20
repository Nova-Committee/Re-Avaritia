package committee.nova.mods.avaritia.api.common.caps.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

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

