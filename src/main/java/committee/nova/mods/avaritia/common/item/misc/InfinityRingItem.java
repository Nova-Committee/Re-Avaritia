package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Personal-dimension key. The dimension is bound to the player UUID, not the stack.
 */
public class InfinityRingItem extends ResourceItem {
    public InfinityRingItem() {
        super(ModRarities.LEGEND.getValue(), true, ModItems.properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player,
                                          @NotNull InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                InfinityRingDimensions.openControl(serverPlayer);
            } else {
                InfinityRingDimensions.travelOrCreatePrompt(serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
