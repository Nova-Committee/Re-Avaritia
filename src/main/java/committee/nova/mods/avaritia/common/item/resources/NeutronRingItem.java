package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.common.capability.wrappers.RingWrapper;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class NeutronRingItem extends ResourceItem {


    public NeutronRingItem() {
        super(ModRarities.EPIC, "neutron_ring", true, new Properties().stacksTo(1));
    }


    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RingWrapper(stack, nbt);
    }

    private static ItemStackHandler getInventory(ItemStack bag) {
        if (bag.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            return (ItemStackHandler) bag.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElseThrow();
        }
        return null;
    }
}
