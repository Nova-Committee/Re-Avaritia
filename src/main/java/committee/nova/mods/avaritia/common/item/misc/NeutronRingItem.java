package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.NeutronRingMenu;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class NeutronRingItem extends ResourceItem{
    public NeutronRingItem() {
        super(ModRarities.EPIC, "neutron_ring", true, new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        if (!worldIn.isClientSide && !playerIn.isCrouching()) {
            int slot = handIn == InteractionHand.MAIN_HAND ? playerIn.getInventory().selected : 40;
            playerIn.openMenu(
                    new SimpleMenuProvider((id, playerInventory, player) -> new NeutronRingMenu(id, playerInventory, slot), Component.translatable("item.avaritia.neutron_ring")),
                    buf -> buf.writeInt(slot));
        }
        return super.use(worldIn, playerIn, handIn);
    }

//    @Override
//    public void attachCapabilities(RegisterCapabilitiesEvent event) {
//        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new ComponentItemHandler(stack, ModDataComponents.NEUTRON_RING_INVENTORY.get(), 81), this);
//    }
}
