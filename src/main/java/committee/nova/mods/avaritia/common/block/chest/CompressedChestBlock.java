package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
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
    public static final ResourceLocation CONTENTS = ResourceLocation.withDefaultNamespace("contents");
    private static final Component UNKNOWN_CONTENTS = Component.translatable("container.shulkerBox.unknownContents");

    public CompressedChestBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava(), () -> ModTileEntities.compressed_chest_tile.get());
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult trace) {
        if (!level.isClientSide()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof CompressedChestTile chestTile) {
                player.openMenu(chestTile, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CompressedChestTile(pPos, pState);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext pContext) {
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
        if (pStack.get(DataComponents.CUSTOM_DATA) != CustomData.EMPTY && blockentity instanceof CompressedChestTile chestTile) {
            chestTile.setChestTag(pStack.get(DataComponents.CUSTOM_DATA).copyTag());
        }
    }

    @Override
    public void onPlace(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState, boolean pMovedByPiston) {
        if (pLevel.isClientSide()) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        CompoundTag chestTag, nameTag = null, countTag = null, nbtTag = null;
        if (blockentity instanceof CompressedChestTile chestTile && chestTile.getChestTag() != null) {
            chestTag = chestTile.getChestTag();
            if (chestTag.contains("name")) nameTag = chestTag.getCompound("name");
            if (chestTag.contains("count")) countTag = chestTag.getCompound("count");
            if (chestTag.contains("nbt")) nbtTag = chestTag.getCompound("nbt");
        }
        if (nameTag != null && countTag != null) {
            Container container = (Container) blockentity;
            for (String index : nameTag.getAllKeys()) {
                var name = nameTag.getString(index);
                var newItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(name));
                ItemStack is = new ItemStack(newItem);
                is.setCount(countTag.getInt(index));
                if (nbtTag != null && !nbtTag.getCompound(index).isEmpty()) {
                    CustomData.set(DataComponents.CUSTOM_DATA, is, nbtTag.getCompound(index));
                }
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
                CompoundTag nbtTag = new CompoundTag();
                for (int i = 0; i < container.getContainerSize(); ++i) {
                    var item = container.getItem(i);
                    if (item.isEmpty()) continue;
                    stackCount++;
                    nameTag.putString(String.valueOf(i), BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    countTag.putInt(String.valueOf(i), item.getCount());
                    nbtTag.put(String.valueOf(i), item.get(DataComponents.CUSTOM_DATA).copyTag());
                }
                chestTag.put("name", nameTag);
                chestTag.put("count", countTag);
                chestTag.put("nbt", nbtTag);
                chestTag.putInt("stackCount", stackCount);
            }

            if (blockentity instanceof CompressedChestTile chestTile) {
                chestTile.setChestTag(chestTag);
            }
            pLevel.removeBlockEntity(pPos);
        }
    }

    @Override
    public void playerDestroy(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable BlockEntity pBlockEntity, @NotNull ItemStack pTool) {
        if (pLevel instanceof ServerLevel serverLevel && pBlockEntity instanceof CompressedChestTile chestTile) {
            var pStack = new ItemStack(ModBlocks.compressed_chest.get().asItem());
            CustomData.set(DataComponents.CUSTOM_DATA, pStack, chestTile.getChestTag());
            popResource(serverLevel, pPos, pStack);
            pState.spawnAfterBreak(serverLevel, pPos, pTool, false);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.has(DataComponents.CONTAINER_LOOT)) {
            tooltipComponents.add(UNKNOWN_CONTENTS);
        }

        int i = 0;
        int j = 0;

        for (ItemStack itemstack : stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()) {
            j++;
            if (i <= 4) {
                i++;
                tooltipComponents.add(Component.translatable("container.shulkerBox.itemCount", itemstack.getHoverName(), itemstack.getCount()));
            }
        }

        if (j - i > 0) {
            tooltipComponents.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
        }
    }
}
