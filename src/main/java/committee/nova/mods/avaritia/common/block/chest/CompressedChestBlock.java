package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 下午12:38
 * @Description:
 */
public class CompressedChestBlock extends ChestBlock {

    public CompressedChestBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava(), () -> ModTileEntities.compressed_chest_tile.get());
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CompressedChestTile(pPos, pState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        ChestType chesttype = ChestType.SINGLE;
        Direction direction = pContext.getHorizontalDirection().getOpposite();
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull LivingEntity pPlacer, @NotNull ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.isClientSide()) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (pStack.getTag() != null && blockentity instanceof CompressedChestTile chestTile) {
            chestTile.setChestTag(pStack.getTag());
        }
    }

    @Override
    public void onPlace(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState, boolean pMovedByPiston) {
        if (pLevel.isClientSide()) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        CompoundTag chestTag,nameTag = null,countTag = null;
        if (blockentity instanceof CompressedChestTile chestTile && chestTile.getChestTag() != null) {
            chestTag = chestTile.getChestTag();
            if (chestTag.contains("name")) nameTag = chestTag.getCompound("name");
            if (chestTag.contains("count")) countTag = chestTag.getCompound("count");
        }
        if (nameTag != null && countTag != null) {
            Container container = (Container) blockentity;
            for (String index : nameTag.getAllKeys()) {
                var name = nameTag.getString(index);
                var count = countTag.getInt(index);
                var newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
                if (newItem == null) continue;
                ItemStack is = new ItemStack(newItem);
                is.setCount(count);
                container.setItem(Integer.parseInt(index), is);
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            CompoundTag chestTag = new CompoundTag();
            int stackCount = 0;
            if (blockentity instanceof Container container) {
                CompoundTag nameTag = new CompoundTag();
                CompoundTag countTag = new CompoundTag();
                for(int i = 0; i < container.getContainerSize(); ++i) {
                    var item = container.getItem(i);
                    if (item.isEmpty()) continue;
                    stackCount++;
                    nameTag.putString(String.valueOf(i),ForgeRegistries.ITEMS.getResourceKey(item.getItem()).get().location().toString());
                    countTag.putInt(String.valueOf(i),item.getCount());
                }
                chestTag.put("name",nameTag);
                chestTag.put("count",countTag);
                chestTag.putInt("stackCount",stackCount);
            }

            if (blockentity instanceof CompressedChestTile chestTile) {
                chestTile.setChestTag(chestTag);
            }
            pLevel.removeBlockEntity(pPos);
        }
    }

    @Override
    public void playerDestroy(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        if (pLevel instanceof ServerLevel serverLevel) {
            var pStack = new ItemStack(ModBlocks.compressed_chest.get().asItem());

            if (pBlockEntity instanceof CompressedChestTile chestTile) {
                pStack.setTag(chestTile.getChestTag());
            }
            popResource(serverLevel, pPos, pStack);
            pState.spawnAfterBreak(serverLevel, pPos, pTool, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        int stackCount = 0;
        if (pStack.getTag() != null && pStack.getTag().contains("stackCount")) {
            stackCount = pStack.getTag().getInt("stackCount");
        }
        pTooltip.add(Component.literal(String.format("%s/243",stackCount)));
    }
}
