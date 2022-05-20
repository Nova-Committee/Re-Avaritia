package nova.committee.avaritia.common.block.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/1 21:44
 * Version: 1.0
 */
public class CompressedCraftingTableBlock extends CraftingTableBlock {
	private static final Component CONTAINER_TITLE = new TranslatableComponent("container.crafting");
	
    public CompressedCraftingTableBlock(float strength) {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(strength).sound(SoundType.WOOD));
        setRegistryName("compressed_crafting_table");
    }
   
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
           return InteractionResult.SUCCESS;
        } else {
           pPlayer.openMenu(getCrafterContainerElement(pPlayer, pLevel, pPos));
           return InteractionResult.CONSUME;
        }
     }
    
    public static MenuProvider getCrafterContainerElement(Player player, Level world, BlockPos pos) {
    	return new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> {
        return new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(world, pos)) {
            @Override
            public boolean stillValid(Player playerIn) {
                return true;
            }
        };
    	}, CONTAINER_TITLE);
    }

}
