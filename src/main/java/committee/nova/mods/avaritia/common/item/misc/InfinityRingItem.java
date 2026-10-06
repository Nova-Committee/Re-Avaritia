package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Personal-dimension key. The dimension is bound to the player UUID, not the stack.
 */
public class InfinityRingItem extends ResourceItem {
    public InfinityRingItem() {
        super(ModRarities.LEGEND, true, new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                InfinityRingDimensions.openControl(serverPlayer);
            } else {
                InfinityRingDimensions.travelOrCreatePrompt(serverPlayer);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
