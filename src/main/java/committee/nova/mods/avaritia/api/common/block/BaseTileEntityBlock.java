package committee.nova.mods.avaritia.api.common.block;

import committee.nova.mods.avaritia.api.common.tile.InventoryTileEntity;
import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public abstract class BaseTileEntityBlock extends BaseBlock implements EntityBlock {
    public BaseTileEntityBlock(Function<Properties, Properties> properties) {
        super(properties);
    }

    public BaseTileEntityBlock(MapColor color, SoundType sound, float hardness, float resistance) {
        super(color, sound, hardness, resistance);
    }
    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance) {
        super(sound, hardness, resistance);
    }

    public BaseTileEntityBlock(MapColor color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(color, sound, hardness, resistance, tool);
    }

    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(sound, hardness, resistance, tool);
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
        return typeA == typeB ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? this.getClientTicker(level, state, type)
                : this.getServerTicker(level, state, type);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            var tile = level.getBlockEntity(pos);

            if (tile instanceof InventoryTileEntity tileEntity) {
                dropInventoryItems(level, pos, tileEntity.getInventory());
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }


    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult trace) {
        if (!level.isClientSide) {
            MenuProvider container = this.getMenuProvider(level.getBlockState(pos), level, pos);
            if (container != null && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, container, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    @Deprecated
    public MenuProvider getMenuProvider(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        return be instanceof MenuProvider ? (MenuProvider) be : null;
    }


    protected <T extends BlockEntity> BlockEntityTicker<T> getClientTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    protected void dropInventoryItems(BlockState state, Level worldIn, BlockPos pos, Storage<ItemVariant> inventory) {
        dropInventoryItems(worldIn, pos, inventory);
    }

    public static void dropInventoryItems(Level world, BlockPos pos, Storage<ItemVariant> inventory) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        for(StorageView<ItemVariant> view : inventory) {
            Containers.dropItemStack(world, x, y, z, view.getResource().toStack((int) Math.max(view.getResource().getItem().getMaxStackSize(), view.getAmount())));
        }
    }
}
