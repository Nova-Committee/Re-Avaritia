package nova.committee.avaritia.common.block.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import nova.committee.avaritia.api.common.block.BaseBlock;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/20 21:42
 * Version: 1.0
 */
public class AbstractCraftingTable extends BaseBlock {
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");


    public AbstractCraftingTable(SoundType sound, float hardness, float resistance, boolean tool, String name) {
        super(sound, hardness, resistance, tool);
    }

    public static MenuProvider getCrafterContainerElement(Player player, Level world, BlockPos pos) {
        return new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(world, pos)) {
            @Override
            public boolean stillValid(@NotNull Player playerIn) {
                return true;
            }
        }, CONTAINER_TITLE);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            pPlayer.openMenu(getCrafterContainerElement(pPlayer, pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }
}
