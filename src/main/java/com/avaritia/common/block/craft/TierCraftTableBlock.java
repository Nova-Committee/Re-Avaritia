package com.avaritia.common.block.craft;

import com.avaritia.api.common.block.BaseTileEntityBlock;
import com.avaritia.common.tile.TierCraftTile;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.enums.ModCraftTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 7:38
 * Version: 1.0
 */
public class TierCraftTableBlock extends BaseTileEntityBlock {
    ModCraftTier tier;

    public TierCraftTableBlock(ModCraftTier tier, BlockBehaviour.Properties properties) {
        super(MapColor.METAL, tier.sound, tier.hardness, tier.resistance, true,properties);
        this.tier = tier;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult p_60508_) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var tile = level.getBlockEntity(pos);
            if (tile instanceof TierCraftTile table) {
                serverPlayer.openMenu(table, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TierCraftTile(pos, state);
    }

}
