package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
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
import net.minecraft.stats.Stats;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
                player.awardStat(Stats.OPEN_CHEST);
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
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (pState.hasBlockEntity()) pLevel.removeBlockEntity(pPos);
            if (blockentity instanceof CompressedChestTile) {
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            }
        }
    }

    @Override
    public void playerDestroy(@NotNull Level pLevel, @NotNull Player player, @NotNull BlockPos pPos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        if (pLevel instanceof ServerLevel serverLevel && blockEntity instanceof CompressedChestTile chestTile && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            var pStack = new ItemStack(ModBlocks.compressed_chest.get().asItem());
            pStack.applyComponents(chestTile.collectComponents());
            popResource(serverLevel, pPos, pStack);
            state.spawnAfterBreak(serverLevel, pPos, tool, false);
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

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        level.getBlockEntity(pos, ModTileEntities.compressed_chest_tile.get()).ifPresent(chestTile -> chestTile.saveToItem(itemstack, level.registryAccess()));
        return itemstack;
    }
}
