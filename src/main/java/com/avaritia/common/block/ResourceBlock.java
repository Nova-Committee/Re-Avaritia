package com.avaritia.common.block;

import com.avaritia.api.common.block.BaseBlock;
import com.avaritia.init.registry.enums.ModResourceBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Name: Avaritia-forge / ResourceBlock
 * Author: cnlimiter
 * CreateTime: 2023/8/13 13:20
 * Description:
 */

public class ResourceBlock extends BaseBlock {
    ModResourceBlocks type;
    public ResourceBlock(ModResourceBlocks type) {
        super(MapColor.METAL, SoundType.METAL, type.hardness, type.resistance, type.lightLevel);
        this.type = type;
    }
    public ResourceBlock(ModResourceBlocks type, BlockBehaviour.Properties properties) {
        super( MapColor.METAL, SoundType.METAL, type.hardness, type.resistance, type.lightLevel);
        this.type = type;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return this.type.enchantPower;
    }
}
