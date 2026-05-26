package com.avaritia.common.tile;

import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/22 18:56
 * Version: 1.0
 */
public class InfinitatoTile extends BlockEntity {
    public static float jumpTicks;
    public String name;

    public InfinitatoTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinitato_tile.get(), pos, state);
    }
}
