package com.avaritia.common.item.misc;

import com.avaritia.init.registry.ModItems;

import com.avaritia.api.iface.item.IItemCapability;
import com.avaritia.common.item.resources.ResourceItem;
import com.avaritia.common.menu.NeutronRingMenu;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class NeutronRingItem extends ResourceItem implements IItemCapability {
    public NeutronRingItem() {
        super(ModRarities.EPIC, true, ModItems.properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        if (!worldIn.isClientSide() && !playerIn.isCrouching()) {
            int slot = handIn == InteractionHand.MAIN_HAND ? playerIn.getInventory().getSelectedSlot() : 40;
            playerIn.openMenu(
                    new SimpleMenuProvider((id, playerInventory, player) -> new NeutronRingMenu(id, playerInventory, slot), Component.translatable("item.avaritia.neutron_ring")),
                    buf -> buf.writeInt(slot));
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Item.ITEM, (stack, context) -> new ItemAccessItemHandler(context, ModDataComponents.NEUTRON_RING_INVENTORY.get(), 81) {
            @Override
            public boolean isValid(int index, net.neoforged.neoforge.transfer.item.ItemResource resource) {
                return super.isValid(index, resource) && resource.test(ItemStack::canFitInsideContainerItems);
            }
        }, this);
    }
}
