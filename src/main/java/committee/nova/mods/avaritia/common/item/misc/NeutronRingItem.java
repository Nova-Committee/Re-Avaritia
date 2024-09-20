package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.capability.wrappers.RingWrapper;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.NeutronRingMenu;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
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
    public @NotNull InteractionResultHolder<ItemStack> use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        if (!worldIn.isClientSide && !playerIn.isCrouching()) {
            int slot = handIn == InteractionHand.MAIN_HAND ? playerIn.getInventory().selected : 40;
            NetworkHooks.openScreen((ServerPlayer) playerIn,
                    new SimpleMenuProvider((id, playerInventory, player) -> NeutronRingMenu.create(id, playerInventory, slot), Component.translatable("item.avaritia.neutron_ring")),
                    buf -> buf.writeInt(slot));
        }
        return super.use(worldIn, playerIn, handIn);
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
