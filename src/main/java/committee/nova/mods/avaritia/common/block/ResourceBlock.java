package committee.nova.mods.avaritia.common.block;

import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Name: Avaritia-forge / ResourceBlock
 * Author: cnlimiter
 * CreateTime: 2023/8/13 13:20
 * Description:
 */

public class ResourceBlock extends BaseBlock {
    public ResourceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 20.0f;
    }


}
